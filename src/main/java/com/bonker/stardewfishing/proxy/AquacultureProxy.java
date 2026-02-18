package com.bonker.stardewfishing.proxy;

import com.teammetallurgy.aquaculture.api.AquacultureAPI;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import com.teammetallurgy.aquaculture.entity.AquaFishingHookEntity;
import com.teammetallurgy.aquaculture.init.AquaDataComponents;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AquacultureProxy {
    public static void damageEquippedBobber(ItemStack fishingRod, ServerPlayer player) {
        ItemUtils.tryDamageBobber(getStackInSlot(fishingRod, 3), player)
                .ifPresent(bobber -> setStackInSlot(fishingRod, 3, bobber));
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
        setStackInSlot(fishingRod, 3, bobber);
    }

    public static ItemStack getStackInSlot(ItemStack fishingRod, int slot) {
        ItemContainerContents contents = AquaFishingRodItem.getHandler(fishingRod);
        if (contents.getSlots() <= slot) {
            return ItemStack.EMPTY;
        }
        return contents.getStackInSlot(slot);
    }

    public static void setStackInSlot(ItemStack fishingRod, int slot, ItemStack stack) {
        if (!fishingRod.has(AquaDataComponents.ROD_INVENTORY)) {
            fishingRod.set(AquaDataComponents.ROD_INVENTORY, ItemContainerContents.fromItems(NonNullList.withSize(4, ItemStack.EMPTY)));
        }

        ItemContainerContents contents = AquaFishingRodItem.getHandler(fishingRod);
        NonNullList<ItemStack> itemList = NonNullList.withSize(4, ItemStack.EMPTY);
        contents.copyInto(itemList);
        itemList.set(slot, stack);
        fishingRod.set(AquaDataComponents.ROD_INVENTORY, ItemContainerContents.fromItems(itemList));
    }

    public static FishingHook spawnHook(ServerPlayer player, ItemStack fishingRod, Vec3 pos) {
        AquaFishingHookEntity hook = new AquaFishingHookEntity(player, player.level(), 0, 0, Hooks.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, fishingRod) {
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
