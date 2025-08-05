package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.common.FishingHookLogic;
import com.teammetallurgy.aquaculture.api.AquacultureAPI;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.init.AquaDataComponents;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class AquacultureProxy {
    public static void damageEquippedBobber(ItemStack fishingRod, ServerPlayer player) {
        ItemStackHandler handler = AquaFishingRodItem.getHandler(fishingRod);
        FishingHookLogic.damageBobber(handler.getStackInSlot(3), player)
                .ifPresent(b -> handler.setStackInSlot(3, b));
    }

    public static ItemStack getBobber(ItemStack fishingRod) {
        return AquaFishingRodItem.getBobber(fishingRod);
    }

    public static boolean isAquaRod(ItemStack fishingRod) {
        return fishingRod.getItem() instanceof AquaFishingRodItem;
    }

    public static boolean isAquaBobber(ItemStack stack) {
        return stack.is(AquacultureAPI.Tags.BOBBER);
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber) {
        if (!fishingRod.has(AquaDataComponents.ROD_INVENTORY)) {
            fishingRod.set(AquaDataComponents.ROD_INVENTORY, ItemContainerContents.EMPTY);
        }
        AquaFishingRodItem.getHandler(fishingRod).setStackInSlot(3, bobber);
    }

    public static FishingHook spawnHook(ServerPlayer player, ItemStack fishingRod, Vec3 pos) {
        AquaFishingBobberEntity hook = new AquaFishingBobberEntity(player, player.level(), 0, 0, Hooks.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, fishingRod) {
            @Override
            public void tick() {
                baseTick();
            }
        };
        hook.setPos(pos);
        player.level().addFreshEntity(hook);
        return hook;
    }

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod) {
        List<ItemStack> modifiers = new ArrayList<>();
        modifiers.add(fishingRod);
        ItemStack bobber = AquaFishingRodItem.getBobber(fishingRod);
        ItemStack line = AquaFishingRodItem.getFishingLine(fishingRod);
        if (!bobber.isEmpty()) {
            modifiers.add(bobber);
        }
        if (!line.isEmpty()) {
            modifiers.add(line);
        }
        return modifiers;
    }
}
