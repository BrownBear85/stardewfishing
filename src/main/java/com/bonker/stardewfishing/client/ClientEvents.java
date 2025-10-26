package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFBlockEntities;
import com.bonker.stardewfishing.common.init.SFParticles;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = StardewFishing.MODID, value = Dist.CLIENT)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onRenderTooltip(final RenderTooltipEvent.Pre event) {
            event.getGraphics().pose().translate(0, 0, 500);
        }

        @SubscribeEvent
        public static void onClientTick(final TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START) {
                return;
            }

            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> containerScreen) {
                RodTooltipHandler.tick(containerScreen.hoveredSlot, containerScreen.getMenu().getCarried());
            } else {
                RodTooltipHandler.clear();
            }
        }

        @SubscribeEvent
        public static void onScreenRendered(final ContainerScreenEvent.Render.Foreground event) {
            if (SFConfig.isInventoryEquippingEnabled()) {
                RodTooltipHandler.render(event.getGuiGraphics(), Minecraft.getInstance().getPartialTick(), event.getMouseX() - event.getContainerScreen().getGuiLeft(), event.getMouseY() - event.getContainerScreen().getGuiTop());
            }
        }

        @SubscribeEvent
        public static void onSoundPlayed(final PlaySoundSourceEvent event) {
            try {
                if (event.getSound() instanceof SimpleSoundInstance instance) {
                    if (event.getSound().getLocation().getNamespace().equals("minecraft")) {
                        SoundEvent newEvent = switch (event.getSound().getLocation().getPath()) {
                            case "entity.fishing_bobber.throw" -> SFSoundEvents.CAST.get();
                            case "entity.fishing_bobber.retrieve" -> {
                                if (Minecraft.getInstance().level == null) yield null;
                                Player player = Minecraft.getInstance().level.getNearestPlayer(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ(), 1, false);
                                yield player == null || player.fishing == null ? SFSoundEvents.PULL_ITEM.get() : SFSoundEvents.FISH_HIT.get();
                            }
                            case "entity.fishing_bobber.splash" -> SFSoundEvents.FISH_BITE.get();
                            default -> null;
                        };

                        if (newEvent != null) {
                            event.getEngine().stop(instance);
                            event.getEngine().play(new SimpleSoundInstance(
                                    newEvent,
                                    instance.getSource(),
                                    1.0F,
                                    1.0F,
                                    SoundInstance.createUnseededRandom(),
                                    instance.getX(),
                                    instance.getY(),
                                    instance.getZ()));
                        }
                    }
                }
            } catch (Exception e) {
                StardewFishing.LOGGER.error("An exception occurred while trying to replace a sound event. I think this happens when you try to use a fishing rod in extremely laggy conditions.", e);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = StardewFishing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(SFBlockEntities.FISH_DISPLAY.get(), FishDisplayBER::new);
        }

        @SubscribeEvent
        public static void onParticleRegistration(final RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(SFParticles.SPARKLE.get(), SparkleParticle.Provider::new);
        }
    }
}
