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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = StardewFishing.MODID, value = Dist.CLIENT)
    public static class ForgeBus {
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
        public static void onSoundPlayed(final PlaySoundEvent event) {
            if (event.getSound() == null) {
                return;
            }

            try {
                SoundInstance instance = event.getSound();
                SoundEvent newEvent = null;
                if (instance instanceof SimpleSoundInstance && event.getSound().getLocation().getNamespace().equals("minecraft")) {
                    switch (event.getSound().getLocation().getPath()) {
                        case "entity.fishing_bobber.throw" -> newEvent = SFSoundEvents.CAST.get();
                        case "entity.fishing_bobber.retrieve" -> {
                            if (Minecraft.getInstance().level == null) break;
                            Player player = Minecraft.getInstance().level.getNearestPlayer(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ(), 1, false);
                            newEvent = player == null || player.fishing == null ? SFSoundEvents.PULL_ITEM.get() : SFSoundEvents.FISH_HIT.get();
                        }
                        case "entity.fishing_bobber.splash" -> newEvent = SFSoundEvents.FISH_BITE.get();
                    }
                }

                if (newEvent != null) {
                    event.setSound(new SimpleSoundInstance(
                            newEvent,
                            SoundSource.MASTER,
                            1.0F,
                            1.0F,
                            SoundInstance.createUnseededRandom(),
                            instance.getX(),
                            instance.getY(),
                            instance.getZ()));
                } else if (SFConfig.isolateAudioCues() && !event.getSound().getLocation().getNamespace().equals(StardewFishing.MODID) && Minecraft.getInstance().screen instanceof FishingScreen) {
                    event.setSound(null);
                }
            } catch (Exception e) {
                StardewFishing.LOGGER.error("An exception occurred while trying to replace a sound event.", e);
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

        @SubscribeEvent
        public static void onRegisterKeyBindings(final RegisterKeyMappingsEvent event) {
            event.register(StardewFishingClient.MINIGAME_BUTTON.get());
        }

        @SubscribeEvent
        public static void onAddClientReloadListeners(final RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(DimensionTextureManager.getOrCreate());
        }
    }
}
