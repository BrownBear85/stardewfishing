package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFComponentTypes;
import com.bonker.stardewfishing.common.items.LegendaryCatch;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ItemUtils {
    public static ItemStack getBobber(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaRod(fishingRod)) {
            return AquacultureProxy.getBobber(fishingRod);
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideRod(fishingRod)) {
            return TideProxy.getBobber(fishingRod, registryAccess);
        } else {
            return fishingRod.has(SFComponentTypes.BOBBER) ?
                    ItemStack.parseOptional(registryAccess, Objects.requireNonNull(fishingRod.get(SFComponentTypes.BOBBER))) :
                    ItemStack.EMPTY;
        }
    }

    public static boolean isFishingRod(ItemStack stack) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaRod(stack)) {
            return true;
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideRod(stack)) {
            return true;
        } else {
            return stack.is(StardewFishing.MODIFIABLE_RODS);
        }
    }

    public static boolean isBobber(ItemStack stack) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaBobber(stack)) {
            return true;
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideBobber(stack)) {
            return true;
        } else {
            return stack.is(StardewFishing.BOBBERS);
        }
    }

    public static void setBobber(ItemStack fishingRod, ItemStack bobber, HolderLookup.Provider registryAccess) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaRod(fishingRod)) {
            AquacultureProxy.setBobber(fishingRod, bobber);
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideRod(fishingRod)) {
            TideProxy.setBobber(fishingRod, bobber, registryAccess);
        } else {
            if (bobber.isEmpty()) {
                fishingRod.remove(SFComponentTypes.BOBBER);
            } else {
                fishingRod.set(SFComponentTypes.BOBBER, (CompoundTag) bobber.save(registryAccess, new CompoundTag()));
            }
        }
    }

    public static void damageBobber(ItemStack fishingRod, ServerPlayer player) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaRod(fishingRod)) {
            AquacultureProxy.damageEquippedBobber(fishingRod, player);
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideRod(fishingRod)) {
            TideProxy.damageEquippedBobber(fishingRod, player);
        }
    }

    public static FishingHook spawnHook(ServerPlayer player, ItemStack fishingRod, Vec3 pos) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaRod(fishingRod)) {
            return AquacultureProxy.spawnHook(player, fishingRod, pos);
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideRod(fishingRod)) {
            return TideProxy.spawnHook(player, fishingRod, pos);
        } else {
            FishingHook hook = new FishingHook(player, player.level(), 0, 0) {
                @Override
                public void tick() {
                    baseTick();
                }
            };
            hook.setPos(pos);
            player.level().addFreshEntity(hook);
            return hook;
        }
    }

    public static List<ItemStack> getAllModifierItems(ItemStack fishingRod, HolderLookup.Provider registryAccess) {
        if (StardewFishing.AQUACULTURE_INSTALLED && AquacultureProxy.isAquaRod(fishingRod)) {
            return AquacultureProxy.getAllModifierItems(fishingRod);
        } else if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideRod(fishingRod)) {
            return TideProxy.getAllModifierItems(fishingRod, registryAccess);
        } else {
            List<ItemStack> modifiers = new ArrayList<>();
            modifiers.add(fishingRod);
            ItemStack bobber = ItemUtils.getBobber(fishingRod, registryAccess);
            if (!bobber.isEmpty()) {
                modifiers.add(bobber);
            }
            return modifiers;
        }
    }

    public static int getLuck(FishingHook hook) {
        if (StardewFishing.TIDE_INSTALLED && TideProxy.isTideHookEntity(hook)) {
            return TideProxy.getLuck(hook);
        } else {
            return hook.luck;
        }
    }

    public static boolean isLegendaryFish(ItemStack stack) {
        return stack.is(StardewFishing.LEGENDARY_FISH);
    }

    public static void addCatchTooltip(ItemStack stack, List<Component> tooltip) {
        if (!stack.has(SFComponentTypes.LEGENDARY_CATCH)) {
            return;
        }
        LegendaryCatch data = Objects.requireNonNull(stack.get(SFComponentTypes.LEGENDARY_CATCH));
        String time = DateFormat.getDateTimeInstance().format(new Date(data.time()));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.stardew_fishing.legendary_data", data.player(), time)
                .withStyle(StardewFishing.LIGHTER_COLOR));
    }
}
