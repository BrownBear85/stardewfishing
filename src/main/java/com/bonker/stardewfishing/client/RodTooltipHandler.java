package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.util.Animation;
import com.bonker.stardewfishing.client.util.RenderUtil;
import com.bonker.stardewfishing.client.util.Shake;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class RodTooltipHandler {
    private static final ResourceLocation TEXTURE = StardewFishing.resource("textures/gui/tooltip.png");
    private static final Multimap<Slot, Tooltip> MAP = HashMultimap.create();
    private static int soundTimer = 0;

    public static void tick(Slot hovered, ItemStack carried) {
        if (soundTimer > 0) {
            soundTimer--;
        }

        if (SFConfig.isInventoryEquippingEnabled() && hovered != null && !MAP.containsKey(hovered)) {
            ItemStack stack = hovered.getItem();
            if (ItemUtils.isFishingRod(stack)) {
                MAP.put(hovered, new Tooltip(hovered));
            }
        }

        MAP.entries().removeIf(entry -> entry.getValue().tick(hovered, carried));
    }

    public static void clear() {
        MAP.clear();
    }

    public static void render(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        MAP.values().forEach(tooltip -> tooltip.render(guiGraphics, partialTick, mouseX, mouseY));
    }

    public static void addShake(Slot slot, boolean equip) {
        for (Map.Entry<Slot, Tooltip> entry : MAP.entries()) {
            if (entry.getKey().getSlotIndex() == slot.getSlotIndex()) {
                entry.getValue().setShake(equip ? 6 : 2);
            }
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(equip ? SFSoundEvents.EQUIP.get() : SFSoundEvents.UNEQUIP.get(), 1.0F));
    }

    public static class Tooltip {
        private final Animation slotAnim = new Animation(0);
        private final Animation mouseAnim = new Animation(0);
        private final Shake shake = new Shake(2F, 1);
        private final Slot slot;
        private ItemStack stack;
        private int shakeDuration = 1;
        private int shakeTicks = 0;
        private boolean hoveredLastTick = false;

        public Tooltip(Slot slot) {
            this.slot = slot;
            stack = slot.getItem().copy();
        }

        public boolean tick(Slot hovered, ItemStack carried) {
            boolean isRod = true;
            if (!slot.getItem().equals(stack)) {
                if (ItemUtils.isFishingRod(slot.getItem())) {
                    stack = slot.getItem().copy();
                } else {
                    isRod = false;
                }
            }

            boolean showMouse = shakeTicks == 0;
            if (isRod && hovered == slot) {
                slotAnim.addValue(1/3F, 0, 1);
                showMouse = showMouse && ItemUtils.isBobber(carried);

                if (!hoveredLastTick) {
                    if (soundTimer == 0) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SFSoundEvents.DWOP.get(), 1.0F));
                        soundTimer = 4;
                    }
                    hoveredLastTick = true;
                }
            } else {
                slotAnim.addValue(-1/3F, 0, 1);
                showMouse = false;
                if (slotAnim.getInterpolated(0) == 0) {
                    return true;
                }

                if (hoveredLastTick) {
                    if (soundTimer == 0) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SFSoundEvents.DWOP_REVERSE.get(), 1.0F));
                    }
                    hoveredLastTick = false;
                }
            }

            shake.setValues(2.5F * shakeTicks / shakeDuration, 1);
            shakeTicks = Math.max(0, shakeTicks - 1);
            shake.tick();

            if (showMouse) {
                mouseAnim.addValue(1 / 3F, 0, 1);
            } else {
                mouseAnim.addValue(-1 / 3F, 0, 1);
            }

            return false;
        }

        public void setShake(int duration) {
            shakeDuration = duration;
            shakeTicks = duration;
        }

        public void render(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 370);

            RenderUtil.drawWithShake(guiGraphics.pose(), shake, partialTick, true, () -> {
                RenderUtil.drawWithBlend(() -> renderSlot(guiGraphics, partialTick));
            });
            RenderUtil.drawWithBlend(() -> renderMouse(guiGraphics, partialTick, mouseX, mouseY));

            guiGraphics.pose().popPose();
        }

        private void renderSlot(GuiGraphics guiGraphics, float partialTick) {
            if (Minecraft.getInstance().level == null) return;

            float anim = slotAnim.getInterpolated(partialTick);
            float x = (1 / anim) * (slot.x + 8);
            float y = (1 / anim) * (slot.y + 8);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(anim, anim, 1);

            RenderUtil.blitF(guiGraphics, TEXTURE, x - 35, y - 12, 0, 0, 29, 27);
            RenderUtil.renderItemF(guiGraphics, ItemUtils.getBobber(stack, Minecraft.getInstance().level.registryAccess()), x - 30, y - 8);

            guiGraphics.pose().popPose();
        }

        private void renderMouse(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            float anim = mouseAnim.getInterpolated(partialTick);
            float x = (1 / anim) * (mouseX);
            float y = (1 / anim) * (mouseY);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(anim, anim, 1);

            RenderUtil.blitF(guiGraphics, TEXTURE, x + 3, y + 3, 33, 0, 11, 19);

            guiGraphics.pose().popPose();
        }
    }
}
