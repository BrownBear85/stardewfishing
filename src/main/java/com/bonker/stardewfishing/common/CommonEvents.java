package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.RodTooltipHandler;
import com.bonker.stardewfishing.common.init.SFItems;
import com.bonker.stardewfishing.common.networking.S2CSyncModifiersPacket;
import com.bonker.stardewfishing.common.networking.SFNetworking;
import com.bonker.stardewfishing.proxy.ClientProxy;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.server.data.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.data.MinigameModifiersReloadListener;
import com.bonker.stardewfishing.server.SFCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class CommonEvents {
    @Mod.EventBusSubscriber(modid = StardewFishing.MODID)
    public static class ForgeBus {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onFishCaught(final ItemFishedEvent event) {
            event.getDrops().forEach(stack -> {
                if (ItemUtils.isLegendaryFish(stack)) {
                    ItemUtils.recordLegendaryCatch(stack, event.getEntity());
                }
            });
        }

        @SubscribeEvent
        public static void onRegisterCommands(final RegisterCommandsEvent event) {
            SFCommands.register(event.getDispatcher(), event.getBuildContext());
        }

        @SubscribeEvent
        public static void onItemStackedOnOther(final ItemStackedOnOtherEvent event) {
            if (!SFConfig.isInventoryEquippingEnabled()) {
                return;
            }

            if (event.getClickAction() != ClickAction.SECONDARY) {
                return;
            }

            ItemStack slotItem = event.getSlot().getItem();
            if (!ItemUtils.isFishingRod(slotItem)) {
                return;
            }

            ItemStack carried = event.getStackedOnItem();
            ItemStack currentBobber = ItemUtils.getBobber(slotItem);

            if (ItemUtils.isBobber(carried)) {
                ItemUtils.setBobber(slotItem, carried.copy());
                event.getCarriedSlotAccess().set(currentBobber.copy());
                event.setCanceled(true);

                if (event.getPlayer().level().isClientSide && FMLEnvironment.dist.isClient()) {
                    RodTooltipHandler.addShake(event.getSlot(), true);
                }
            } else if (!currentBobber.isEmpty() && carried.isEmpty()) {
                ItemUtils.setBobber(slotItem, ItemStack.EMPTY);
                event.getCarriedSlotAccess().set(currentBobber.copy());
                event.setCanceled(true);

                if (event.getPlayer().level().isClientSide && FMLEnvironment.dist.isClient()) {
                    RodTooltipHandler.addShake(event.getSlot(), false);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onTooltipHighPriority(final ItemTooltipEvent event) {
            if (ItemUtils.isLegendaryFish(event.getItemStack())) {
                event.getToolTip().add(1, SFItems.LEGENDARY_FISH_TOOLTIP.copy().withStyle(ChatFormatting.BOLD));
                ItemUtils.addCatchTooltip(event.getItemStack(), event.getToolTip());
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onTooltipLowPriority(final ItemTooltipEvent event) {
            if (ItemUtils.isFishingRod(event.getItemStack())) {
                if (!StardewFishing.TIDE_INSTALLED) {
                    ItemStack bobber = ItemUtils.getBobber(event.getItemStack());

                    if (bobber.isEmpty()) {
                        event.getToolTip().add(Component.translatable("tooltip.stardew_fishing." + (SFConfig.isInventoryEquippingEnabled() ? "no_bobber" : "no_bobber_attach_disabled"))
                                .withStyle(StardewFishing.LIGHT_COLOR));
                    } else {
                        event.getToolTip().add(Component.translatable("tooltip.stardew_fishing.bobber", bobber.getDisplayName().copy().withStyle(StardewFishing.LIGHT_COLOR))
                                .withStyle(StardewFishing.DARK_COLOR));
                    }
                }
            }

            StardewFishing.getModifiers(event.getItemStack()).ifPresent(modifiers -> {
                if (!event.getToolTip().get(event.getToolTip().size() - 1).getString().isEmpty()) {
                    event.getToolTip().add(Component.empty());
                }

                if (ClientProxy.isShiftDown()) {
                    event.getToolTip().add(Component.translatable("tooltip.stardew_fishing.rod_modifier").withStyle(StardewFishing.LIGHT_COLOR));
                    modifiers.appendTooltip(event.getToolTip());
                } else {
                    event.getToolTip().add(Component.translatable("tooltip.stardew_fishing.rod_modifier_shift").withStyle(StardewFishing.LIGHT_COLOR));
                }
            });
        }

        @SubscribeEvent
        public static void onItemDestroyed(final PlayerDestroyItemEvent event) {
            if (ItemUtils.isFishingRod(event.getOriginal())) {
                ItemStack bobber = ItemUtils.getBobber(event.getOriginal());
                if (!bobber.isEmpty()) {
                    event.getEntity().spawnAtLocation(bobber);
                }
            }
        }

        @SubscribeEvent
        public static void onAttachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof FishingHook) {
                FishingHookLogic.attachCap(event);
            }
        }

        @SubscribeEvent
        public static void onAddReloadListeners(final AddReloadListenerEvent event) {
            event.addListener(FishBehaviorReloadListener.create());
            event.addListener(MinigameModifiersReloadListener.getOrCreate());
        }

        @SubscribeEvent
        public static void onPlayerJoin(final OnDatapackSyncEvent event) {
            for (ServerPlayer player : event.getPlayers()) {
                SFNetworking.sendToPlayer(player, new S2CSyncModifiersPacket(MinigameModifiersReloadListener.getOrCreate().getData()));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = StardewFishing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void onCommonSetup(final FMLCommonSetupEvent event) {
            SFNetworking.register();
        }
    }
}
