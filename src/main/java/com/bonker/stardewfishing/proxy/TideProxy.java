package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.common.FishingHookLogic;
import com.li64.tide.data.TideTags;
import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.TideEntityTypes;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TideProxy {
    public static void damageEquippedBobber(ItemStack fishingRod, ServerPlayer player) {
        if (!CustomRodManager.hasBobber(fishingRod, player.registryAccess())) {
            return;
        }
        FishingHookLogic.damageBobber(getBobber(fishingRod, player.registryAccess()), player)
                .ifPresent(b -> CustomRodManager.setBobber(fishingRod, b, player.registryAccess()));
    }

    public static ItemStack getBobber(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        return CustomRodManager.hasBobber(fishingRod, registryAccess) ? CustomRodManager.getBobber(fishingRod, registryAccess) : ItemStack.EMPTY;
    }

    public static boolean isTideRod(ItemStack fishingRod) {
        return fishingRod.getItem() instanceof TideFishingRodItem;
    }

    public static boolean isTideBobber(ItemStack stack) {
        return stack.is(TideTags.Items.BOBBERS);
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber, HolderLookup.Provider registryAccess) {
        CustomRodManager.setBobber(fishingRod, bobber, registryAccess);
    }

    public static FishingHook spawnHook(Player player, ItemStack fishingRod, Vec3 pos) {
        TideFishingHook hook = new TideFishingHook(TideEntityTypes.FISHING_BOBBER, player, player.level(), 0, 0, 1, fishingRod) {
            @Override
            public void tick() {
                baseTick();
            }
        };
        player.level().addFreshEntity(hook);
        hook.setPos(pos);
        HookAccessor accessor = new HookAccessor(hook, player.level());
        player.level().addFreshEntity(accessor);
        return accessor;
    }

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        List<ItemStack> modifiers = new ArrayList<>();
        modifiers.add(fishingRod);
        if (CustomRodManager.hasBobber(fishingRod, registryAccess)) {
            modifiers.add(CustomRodManager.getBobber(fishingRod, registryAccess));
        }
        if (CustomRodManager.hasHook(fishingRod, registryAccess)) {
            modifiers.add(CustomRodManager.getHook(fishingRod, registryAccess));
        }
        if (CustomRodManager.hasLine(fishingRod, registryAccess)) {
            modifiers.add(CustomRodManager.getLine(fishingRod, registryAccess));
        }
        return modifiers;
    }

    public static boolean isTideHookEntity(FishingHook hook) {
        return hook instanceof HookAccessor;
    }

    public static int getLuck(FishingHook hook) {
        if (hook instanceof HookAccessor accessor) {
            Player owner = accessor.getPlayerOwner();
            return owner == null ? 0 : HookAccessor.getHook(owner).getLuck();
        }
        return 0;
    }
}
