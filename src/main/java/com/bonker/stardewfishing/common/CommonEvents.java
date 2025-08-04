package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.RodTooltipHandler;
import com.bonker.stardewfishing.common.init.SFAttributes;
import com.bonker.stardewfishing.common.init.SFComponentTypes;
import com.bonker.stardewfishing.common.init.SFItems;
import com.bonker.stardewfishing.common.items.LegendaryCatch;
import com.bonker.stardewfishing.common.networking.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.proxy.ClientProxy;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.server.data.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.data.MinigameModifiersReloadListener;
import com.bonker.stardewfishing.server.SFCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = StardewFishing.MODID)
public class CommonEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFishCaught(final ItemFishedEvent event) {
        event.getDrops().forEach(stack -> {
            if (ItemUtils.isLegendaryFish(stack)) {
                stack.set(SFComponentTypes.LEGENDARY_CATCH, new LegendaryCatch(event.getEntity()));
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
        ItemStack currentBobber = ItemUtils.getBobber(slotItem, event.getPlayer().registryAccess());

        if (ItemUtils.isBobber(carried)) {
            ItemUtils.setBobber(slotItem, carried.copy(), event.getPlayer().registryAccess());
            event.getCarriedSlotAccess().set(currentBobber.copy());
            event.setCanceled(true);

            if (event.getPlayer().level().isClientSide && FMLEnvironment.dist.isClient()) {
                RodTooltipHandler.addShake(event.getSlot(), true);
            }
        } else if (!currentBobber.isEmpty() && carried.isEmpty()) {
            ItemUtils.setBobber(slotItem, ItemStack.EMPTY, event.getPlayer().registryAccess());
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
            if (!StardewFishing.TIDE_INSTALLED && event.getEntity() != null) {
                ItemStack bobber = ItemUtils.getBobber(event.getItemStack(), event.getEntity().registryAccess());

                if (bobber.isEmpty()) {
                    event.getToolTip().add(Component.translatable("tooltip.stardew_fishing." + (SFConfig.isInventoryEquippingEnabled() ? "no_bobber" : "no_bobber_attach_disabled"))
                            .withStyle(StardewFishing.LIGHT_COLOR));
                } else {
                    event.getToolTip().add(Component.translatable("tooltip.stardew_fishing.bobber", bobber.getDisplayName().copy().withStyle(StardewFishing.LIGHT_COLOR))
                            .withStyle(StardewFishing.DARK_COLOR));
                }
            }
        }

        MinigameModifiersReloadListener.getModifiers(event.getItemStack()).ifPresent(modifiers -> {
            if (!event.getToolTip().getLast().getString().isEmpty()) {
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
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(FishBehaviorReloadListener.create());
        event.addListener(MinigameModifiersReloadListener.create());
    }

    @SubscribeEvent
    public static void onRegistryPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                S2CStartMinigamePacket.TYPE,
                S2CStartMinigamePacket.STREAM_CODEC,
                S2CStartMinigamePacket::handle
        );

        registrar.playToServer(
                C2SCompleteMinigamePacket.TYPE,
                C2SCompleteMinigamePacket.STREAM_CODEC,
                C2SCompleteMinigamePacket::handle
        );
    }

    @SubscribeEvent
    public static void onAttributeCreation(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SFAttributes.LINE_STRENGTH);
        event.add(EntityType.PLAYER, SFAttributes.BAR_SIZE);
        event.add(EntityType.PLAYER, SFAttributes.TREASURE_CHANCE_BONUS);
        event.add(EntityType.PLAYER, SFAttributes.EXPERIENCE_MULTIPLIER);
    }
}
