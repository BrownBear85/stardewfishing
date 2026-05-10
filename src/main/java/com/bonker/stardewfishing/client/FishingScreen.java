package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.util.Animation;
import com.bonker.stardewfishing.client.util.RenderUtil;
import com.bonker.stardewfishing.client.util.Shake;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.common.networking.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.proxy.ClientProxy;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.Objects;

public class FishingScreen extends Screen {
    private static final Component TITLE = Component.literal("Fishing Minigame");
    private static final Identifier CHEST_TEXTURE = StardewFishing.identifier("textures/gui/chest.png");
    private static final Identifier GOLDEN_CHEST_TEXTURE = StardewFishing.identifier("textures/gui/golden_chest.png");

    private static final int GUI_WIDTH = 38;
    private static final int GUI_HEIGHT = 152;
    private static final int HIT_WIDTH = 73;
    private static final int HIT_HEIGHT = 29;
    private static final int PERFECT_WIDTH = 41;
    private static final int PERFECT_HEIGHT = 12;

    private static final float ALPHA_PER_TICK = 1F / 10;
    private static final float HANDLE_ROT_FAST = Mth.PI / 3;
    private static final float HANDLE_ROT_SLOW = Mth.PI / -7F;

    private static final int REEL_FAST_LENGTH = 30;
    private static final int REEL_SLOW_LENGTH = 20;
    private static final int CREAK_LENGTH = 6;

    private final FishingMinigame minigame;
    private final ItemStack fish;

    private int leftPos, topPos;
    private Status status = Status.HIT_TEXT;
    private double accuracy = -1;
    private boolean inputDown = false;
    private int animationTimer = 0;
    private boolean gotChest = false;
    private boolean goldenChest = false;

    private final Animation textSize = new Animation(0);
    private final Animation progressBar;
    private final Animation bobberPos = new Animation(0);
    private final Animation bobberAlpha = new Animation(1);
    private final Animation fishPos = new Animation(0);
    private final Animation handleRot = new Animation(0);
    private final Animation chestProgress = new Animation(0);
    private final Animation chestAppear = new Animation(0);

    private final Shake shake = new Shake(0.75F, 1);
    private final Shake chestShake = new Shake(0.75F, 1);

    public int reelSoundTimer = -1;
    private int creakSoundTimer = 0;

    private float partialTick = 0;

    public FishingScreen(S2CStartMinigamePacket packet) {
        super(TITLE);
        this.minigame = new FishingMinigame(this, packet, Objects.requireNonNull(Minecraft.getInstance().player), packet.lineStrength(), packet.barSize());
        this.fish = packet.fish();
        this.progressBar = new Animation(minigame.getProgress());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        partialTick = ClientProxy.getPartialTick();

        Matrix3x2fStack poseStack = pGuiGraphicsExtractor.pose();
        Identifier texture = DimensionTextureManager.getOrCreate().getMinigameTexture(minecraft.level);

        if (status == Status.HIT_TEXT) {
            // render HIT!
            float scale = textSize.getInterpolated(partialTick) * 1.5F;
            float x = (width - HIT_WIDTH * scale) / 2;
            float y = (height - HIT_HEIGHT * scale) / 3;

            poseStack.pushMatrix();
            poseStack.scale(scale, scale);
            RenderUtil.blitF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, x * (1 / scale), y * (1 / scale), 71, 0, HIT_WIDTH, HIT_HEIGHT);
            poseStack.popMatrix();
        } else if (status == Status.CHEST_OPENING) {
            int frame = Math.min(30 - animationTimer, 19) / 2;
            pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, goldenChest ? GOLDEN_CHEST_TEXTURE : CHEST_TEXTURE, leftPos + 38 / 2 - 64, topPos, 0, frame * 128, 128, 128, 128, 1280);
        } else {
            RenderUtil.drawWithShake(poseStack, shake, partialTick, status == Status.SUCCESS || status == Status.FAILURE, () -> {
                RenderUtil.drawWithBlend(() -> {
                    // draw fishing gui
                    pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);

                    // draw bobber
                    RenderUtil.drawWithAlpha(bobberAlpha.getInterpolated(partialTick), () -> {
                        int size = minigame.getBarSize();
                        float bobberY = 4 - size + (142 - bobberPos.getInterpolated(partialTick));
                        // clamp decimal part to multiples of 0.1 to prevent floating point visual artifacts
                        bobberY = (int) (bobberY * 10) / 10F;

                        RenderUtil.blitF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, leftPos + 18, topPos + bobberY, 38, 0, 9, 2);
                        RenderUtil.blitRepeatingF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, leftPos + 18, topPos + bobberY + 2, 38, 2, 9, size - 4, 9, 1);
                        RenderUtil.blitF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, leftPos + 18, topPos + bobberY + size - 2, 38, 3, 9, 2);
                    });
                });

                // draw sonar bobber
                if (minigame.hasSonarBobber()) {
                    pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 38, topPos + 2, 185, 0, 26, 25, 256, 256);

                    pGuiGraphicsExtractor.item(fish, leftPos + 45, topPos + 8);
                    if (pMouseX >= leftPos + 38 && pMouseY >= topPos + 5 && pMouseX <= leftPos + 64 && pMouseY <= topPos + 27) {
                        pGuiGraphicsExtractor.setTooltipForNextFrame(font, Screen.getTooltipFromItem(minecraft, fish).subList(0, 1), fish.getTooltipImage(), fish, pMouseX, pMouseY);
                    }
                }

                RenderUtil.drawWithShake(poseStack, shake, partialTick, minigame.isBobberOnFish() && status == Status.MINIGAME, () -> {
                    float pos = fishPos.getInterpolated(partialTick);
                    int offset = 0;
                    if (ItemUtils.isLegendaryFish(fish)) {
                        offset += 15;
                        if (SFConfig.isLegendaryFlashingEnabled() && (int) (pos / 8) % 2 == 1) {
                            offset += 15;
                        }
                    }
                    // draw fish
                    float fishY = 4 - 16 + (142 - pos);
                    RenderUtil.blitF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, leftPos + 14, topPos + fishY, 55, offset, 16, 15);
                });

                if (minigame.isChestVisible() || animationTimer < 0) {
                    float scale = chestAppear.getInterpolated(partialTick);
                    if (scale != 0) {
                        poseStack.pushMatrix();
                        poseStack.scale(scale, scale);

                        float chestX = (leftPos + 24 - 8 * scale) / scale;
                        float chestY = (topPos + 4 - 13 + (142 + 8 - 8 * scale - minigame.getChestPos())) / scale;

                        RenderUtil.drawWithShake(poseStack, chestShake, partialTick, minigame.isBobberOnChest() && status == Status.MINIGAME, () -> {
                            // draw treasure chest
                            RenderUtil.blitF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, chestX, chestY, 211, minigame.isGoldenChest() ? 13 : 0, 13, 13);
                        });

                        // bar bg
                        RenderUtil.fillF(pGuiGraphicsExtractor, chestX + 1, chestY + 12, chestX + 12, chestY + 14, 0x55000000);

                        // bar color
                        float progress = chestProgress.getInterpolated(partialTick);
                        int color = Mth.hsvToRgb(progress / 3.0F, 1.0F, 1.0F) | 0xFF000000;
                        RenderUtil.fillF(pGuiGraphicsExtractor, chestX + 1, chestY + 12, chestX + 1 + progress * 11, chestY + 14, color);

                        poseStack.popMatrix();
                    }
                }

                // draw progress bar
                float progress = progressBar.getInterpolated(partialTick);
                int color = Mth.hsvToRgb(progress / 3.0F, 1.0F, 1.0F) | 0xFF000000;
                RenderUtil.fillF(pGuiGraphicsExtractor, leftPos + 33, topPos + 148, leftPos + 37, topPos + 148 - progress * 145, color);

                // draw handle
                RenderUtil.drawRotatedAround(poseStack, handleRot.getInterpolated(partialTick), leftPos + 6.5F, topPos + 130.5F, () ->
                        pGuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 5, topPos + 129, 47, 0, 8, 3, 256, 256));

                // render PERFECT!
                if (status == Status.SUCCESS && accuracy == 1) {
                    float scale = textSize.getInterpolated(partialTick);
                    float x = leftPos + 2 + (PERFECT_WIDTH - PERFECT_WIDTH * scale) / 2;
                    float y = topPos - PERFECT_HEIGHT * scale;

                    poseStack.pushMatrix();
                    poseStack.scale(scale, scale);
                    RenderUtil.blitF(pGuiGraphicsExtractor, RenderPipelines.GUI_TEXTURED, texture, x / scale, y / scale, 144, 0, PERFECT_WIDTH, PERFECT_HEIGHT);
                    poseStack.popMatrix();
                }
            });
        }

        if (status != Status.HIT_TEXT) {
            pGuiGraphicsExtractor.text(font, StardewFishing.MOD_NAME, 2, height - 2 - font.lineHeight, 0x6969697F, false);
        }
    }

    @Override
    protected void init() {
        leftPos = (width - GUI_WIDTH) / 2;
        topPos = (height - GUI_HEIGHT) / 2;

        if (SFConfig.isolateAudioCues()) {
            for (SoundSource category : SoundSource.values()) {
                if (category == SoundSource.MASTER) continue;
                minecraft.getSoundManager().stop(null, category);
            }
        }
    }

    @Override
    public void tick() {
        shake.tick();
        if (minigame.isChestVisible()) {
            chestShake.tick();
        }

        switch (status) {
            case HIT_TEXT -> {
                if (animationTimer < 20) {
                    if (++animationTimer == 20) {
                        status = Status.MINIGAME;
                        animationTimer = Integer.MAX_VALUE;
                    } else if (animationTimer <= 5) {
                        textSize.addValue(0.2F);
                    } else if (animationTimer <= 15) {
                        textSize.addValue(-0.013F);
                    } else {
                        textSize.addValue(-0.16F);
                    }
                }
            }
            case MINIGAME -> {
                minigame.tick(inputDown);

                boolean onFish = minigame.isBobberOnFish();

                progressBar.setValue(minigame.getProgress());
                bobberPos.setValue(minigame.getBobberPos());
                bobberAlpha.addValue((onFish || minigame.isBobberOnChest()) ? ALPHA_PER_TICK : -ALPHA_PER_TICK, 0.4F, 1);
                fishPos.setValue(minigame.getFishPos());
                handleRot.addValue(onFish ? HANDLE_ROT_FAST : HANDLE_ROT_SLOW);

                if (status != Status.MINIGAME) {
                    break;
                }

                if (minigame.isChestVisible()) {
                    if (animationTimer == Integer.MAX_VALUE) {
                        animationTimer = 5;
                    }

                    if (animationTimer > 0) {
                        animationTimer--;
                        chestAppear.addValue(0.2F);

                        if (animationTimer == 0) {
                            animationTimer = Integer.MIN_VALUE;
                            chestAppear.setValue(1);
                        }
                    }

                    chestProgress.setValue(minigame.getChestProgress());
                } else {
                    if (animationTimer == Integer.MIN_VALUE) {
                        animationTimer = -5;

                        playSound(SFSoundEvents.CHEST_GET.get());
                    }

                    if (animationTimer < 0) {
                        animationTimer++;
                        chestAppear.addValue(-0.2F);

                        if (animationTimer == 0) {
                            chestAppear.setValue(0);
                        }
                    }
                }

                if (reelSoundTimer == -1 || --reelSoundTimer == 0) {
                    reelSoundTimer = onFish ? REEL_FAST_LENGTH : REEL_SLOW_LENGTH;
                     if (!SFConfig.isolateAudioCues()) {
                         playSound(onFish ? SFSoundEvents.REEL_FAST.get() : SFSoundEvents.REEL_SLOW.get());
                     }
                }

                if (creakSoundTimer > 0) {
                    creakSoundTimer--;
                }
                if (inputDown && creakSoundTimer == 0) {
                    creakSoundTimer = CREAK_LENGTH;
                    if (!SFConfig.isolateAudioCues()) {
                        playSound(SFSoundEvents.REEL_CREAK.get());
                    }
                }
            }
            case SUCCESS, FAILURE -> {
                if (--animationTimer == 0) {
                    if (gotChest) {
                        status = Status.CHEST_OPENING;
                        animationTimer = 30;

                        playSound(goldenChest ? SFSoundEvents.OPEN_CHEST_GOLDEN.get() : SFSoundEvents.OPEN_CHEST.get());
                    } else {
                        onClose();
                    }
                } else if (animationTimer >= 15) {
                    textSize.addValue(0.2F);
                } else if (animationTimer >= 5) {
                    textSize.addValue(-0.013F);
                } else {
                    textSize.addValue(-0.16F);
                }
            }
            case CHEST_OPENING -> {
                if (--animationTimer == 0) {
                    onClose();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        if (StardewFishingClient.MINIGAME_BUTTON.get().getKey().getType() == InputConstants.Type.MOUSE &&
                event.button() == StardewFishingClient.MINIGAME_BUTTON.get().getKey().getValue()) {
            if (status == Status.MINIGAME) {
                setInputDown(true);
                return true;
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent event) {
        if (StardewFishingClient.MINIGAME_BUTTON.get().getKey().getType() == InputConstants.Type.MOUSE &&
                event.button() == StardewFishingClient.MINIGAME_BUTTON.get().getKey().getValue()) {
            setInputDown(false);
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if (StardewFishingClient.MINIGAME_BUTTON.get().getKey().getType() == InputConstants.Type.KEYSYM &&
                event.key() == StardewFishingClient.MINIGAME_BUTTON.get().getKey().getValue()) {
            if (status == Status.MINIGAME) {
                setInputDown(true);
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(@NotNull KeyEvent event) {
        if (StardewFishingClient.MINIGAME_BUTTON.get().getKey().getType() == InputConstants.Type.KEYSYM &&
                event.key() == StardewFishingClient.MINIGAME_BUTTON.get().getKey().getValue()) {
            setInputDown(false);
        }

        return super.keyReleased(event);
    }

    public void setInputDown(boolean down) {
        if (down && !inputDown && !SFConfig.isolateAudioCues()) {
            playSound(SFSoundEvents.REEL_CREAK.get());
        }

        inputDown = down;
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientPacketDistributor.sendToServer(new C2SCompleteMinigamePacket(status == Status.SUCCESS || status == Status.CHEST_OPENING, accuracy, gotChest));

        stopReelingSounds();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return status == Status.MINIGAME;
    }

    @Override
    public boolean isPauseScreen() {
        return status != Status.HIT_TEXT && SFConfig.pauseDuringMinigame();
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (status != Status.HIT_TEXT) {
            super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public void setResult(boolean success, double accuracy, boolean gotChest, boolean goldenChest) {
        status = success ? Status.SUCCESS : Status.FAILURE;
        this.accuracy = accuracy;
        this.gotChest = gotChest;
        this.goldenChest = goldenChest;

        animationTimer = 20;
        textSize.reset(0.0F);

        progressBar.freeze(partialTick);
        bobberPos.freeze(partialTick);
        bobberAlpha.freeze(partialTick);
        fishPos.freeze(partialTick);
        handleRot.freeze(partialTick);
        chestProgress.freeze(partialTick);
        chestAppear.freeze(partialTick);

        playSound(success ? SFSoundEvents.COMPLETE.get() : SFSoundEvents.FISH_ESCAPE.get());
        stopReelingSounds();
        reelSoundTimer = -2;
        shake.setValues(2.0F, 1);
    }

    public void playSound(SoundEvent soundEvent) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F));
    }

    public void stopReelingSounds() {
        reelSoundTimer = 1;

        minecraft.getSoundManager().stop(SFSoundEvents.REEL_FAST.getId(), null);
        minecraft.getSoundManager().stop(SFSoundEvents.REEL_SLOW.getId(), null);
    }

    public enum Status {
        HIT_TEXT, MINIGAME, SUCCESS, FAILURE, CHEST_OPENING
    }
}
