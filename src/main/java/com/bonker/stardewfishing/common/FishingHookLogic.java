package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.common.networking.SFNetworking;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.proxy.QualityFoodProxy;
import com.bonker.stardewfishing.server.LockableList;
import com.bonker.stardewfishing.server.data.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.data.MinigameDisabledPlayers;
import com.bonker.stardewfishing.server.event.StardewMinigameEndedEvent;
import com.bonker.stardewfishing.server.event.StardewMinigameModifyRewardsEvent;
import com.bonker.stardewfishing.server.event.StardewMinigameStartedEvent;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FishingHookLogic {
    // Lockable list because I don't want to have to tell the Tide dev to make more changes
    public final LockableList<ItemStack> rewards = new LockableList<>();
    private boolean treasureChest = false;
    private boolean goldenChest = false;
    public StardewMinigameStartedEvent event = null;

    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (!event.getObject().getCapability(FishingHookLogic.CapProvider.CAP).isPresent()) {
            event.addCapability(CapProvider.NAME, new CapProvider());
        }
    }

    public static Optional<ArrayList<ItemStack>> getStoredRewards(FishingHook entity) {
        return entity.getCapability(CapProvider.CAP).map(cap -> cap.rewards);
    }

    public static boolean startStardewMinigame(ServerPlayer player) {
        if (player.fishing == null || player instanceof FakePlayer ||
                MinigameDisabledPlayers.get(player.server).isMinigameDisabled(player)) return false;

        return player.fishing.getCapability(CapProvider.CAP).resolve().map(cap -> {
            // A minigame is already in progress
            if (cap.event != null) {
                StardewFishing.LOGGER.warn("{} tried to start a minigame while playing one", player.getScoreboardName());
                return true;
            }

            cap.rewards.lock();

            ItemStack fish = cap.rewards.stream()
                    .filter(stack -> stack.is(StardewFishing.STARTS_MINIGAME))
                    .findFirst()
                    .orElseThrow();

            InteractionHand rodHand = ItemUtils.getRodHand(player);
            if (rodHand == null) {
                StardewFishing.LOGGER.warn("{} tried to start a minigame without a fishing rod", player.getScoreboardName());
                return false;
            }

            BlockPos pos = BlockPos.containing(player.fishing.position());
            FluidState fluid = player.level().getBlockState(pos).getFluidState();
            if (fluid.isEmpty()) {
                fluid = player.level().getBlockState(pos.below()).getFluidState();
            }

            ItemStack fishingRod = player.getItemInHand(rodHand);
            StardewMinigameStartedEvent startEvent = new StardewMinigameStartedEvent(player, player.fishing, fishingRod, fish, FishBehaviorReloadListener.getBehavior(fish), fluid.is(FluidTags.LAVA));

            ItemUtils.getAllModifierItems(fishingRod).forEach(stack ->
                    StardewFishing.getModifiers(stack)
                            .ifPresent(modifiers -> modifiers.apply(startEvent)));


            MinecraftForge.EVENT_BUS.post(startEvent);

            cap.event = startEvent;

            double chestChance = SFConfig.getTreasureChestChance() + startEvent.getTreasureChanceBonus();
            double goldenChance = SFConfig.getGoldenChestChance() + startEvent.getGoldenChanceBonus();
            if (startEvent.isForcedTreasureChest() || player.getRandom().nextFloat() < chestChance) {
                cap.treasureChest = true;
                if (startEvent.isForcedGoldenChest() || player.getRandom().nextFloat() < goldenChance) {
                    cap.goldenChest = true;
                }
            }

            SFNetworking.sendToPlayer(player, new S2CStartMinigamePacket(startEvent, cap.treasureChest, cap.goldenChest));
            return true;
        }).orElse(false);
    }

    public static void endMinigame(ServerPlayer player, boolean success, double accuracy, boolean gotChest, int qualityBoost, @Nullable ItemStack fishingRod) {
        if (player.fishing == null) {
            return;
        }

        StardewMinigameEndedEvent endEvent = new StardewMinigameEndedEvent(player, player.fishing, fishingRod, success, accuracy, gotChest);
        if (fishingRod != null) {
            MinecraftForge.EVENT_BUS.post(endEvent);
        }

        if (endEvent.wasSuccessful() && !player.level().isClientSide) {
            modifyRewards(player, endEvent.getAccuracy(), qualityBoost);
            giveRewards(player, endEvent.getAccuracy(), endEvent.gotChest(), fishingRod);
        }

        if (player.fishing != null) {
            player.fishing.discard();
        }
    }

    // todo: remove this, exists for backward compatibility with tide
    @Deprecated(forRemoval = true, since = "3.0")
    public static void modifyRewards(ServerPlayer player, double accuracy, @Nullable ItemStack fishingRod) {
        modifyRewards(player, accuracy, 0);
    }

    // todo: remove this, exists for backward compatibility with tide
    @Deprecated(forRemoval = true, since = "3.0")
    public static void modifyRewards(List<ItemStack> rewards, double accuracy, @Nullable ItemStack fishingRod) {
        modifyRewards(rewards, accuracy, 0);
    }

    public static void modifyRewards(ServerPlayer player, double accuracy, int qualityBoost) {
        if (player.fishing == null) return;
        getStoredRewards(player.fishing).ifPresent(rewards -> modifyRewards(rewards, accuracy, qualityBoost));
    }

    public static void modifyRewards(List<ItemStack> rewards, double accuracy, int qualityBoost) {
        if (StardewFishing.QUALITY_FOOD_INSTALLED) {
            int quality = Mth.clamp(SFConfig.getQuality(accuracy) + qualityBoost, 0, 3);
            for (ItemStack reward : rewards) {
                if (reward.is(StardewFishing.STARTS_MINIGAME)) {
                    if (quality == 0 && reward.hasTag() && reward.getOrCreateTag().contains("quality_food")) {
                        if (reward.getOrCreateTag().size() > 1) {
                            reward.getOrCreateTag().remove("quality_food");
                        } else {
                            reward.setTag(null);
                        }
                    } else if (quality > 0) {
                        QualityFoodProxy.applyQuality(reward, quality);
                    }
                }
            }
        }
    }

    public static void giveRewards(ServerPlayer player, double accuracy, boolean gotChest, ItemStack fishingRod) {
        if (player.fishing == null) return;

        FishingHook hook = player.fishing;

        hook.getCapability(CapProvider.CAP).ifPresent(cap -> {
            cap.rewards.unlock();

            if (cap.treasureChest && gotChest) {
                cap.rewards.addAll(getTreasureChestLoot(player.serverLevel(), cap.goldenChest));
            }

            StardewMinigameModifyRewardsEvent modifyRewardsEvent = new StardewMinigameModifyRewardsEvent(player, hook, fishingRod, cap.rewards);
            MinecraftForge.EVENT_BUS.post(modifyRewardsEvent);

            if (cap.rewards.isEmpty()) {
                hook.discard();
            }

            if (MinecraftForge.EVENT_BUS.post(new ItemFishedEvent(cap.rewards, 1, hook))) {
                player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                hook.discard();
                return;
            }

            ServerLevel level = player.serverLevel();
            for (ItemStack reward : cap.rewards) {
                if (reward.is(ItemTags.FISHES)) {
                    player.awardStat(Stats.FISH_CAUGHT);
                }

                ItemEntity itementity;
                if (cap.event.isLavaFishing()) {
                    itementity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward) {
                        public boolean displayFireAnimation() {
                            return false;
                        }

                        public void lavaHurt() {
                        }
                    };
                } else {
                    itementity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward);
                }
                double scale = 0.1;
                double dx = player.getX() - hook.getX();
                double dy = player.getY() - hook.getY();
                double dz = player.getZ() - hook.getZ();
                itementity.setDeltaMovement(dx * scale, dy * scale + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * scale);
                level.addFreshEntity(itementity);

                int exp = (int) ((player.getRandom().nextInt(6) + 1) * SFConfig.getMultiplier(accuracy, cap.event.getExpMultiplier()));
                level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, exp));

                InteractionHand hand = ItemUtils.getRodHand(player);
                ItemStack handItem = hand != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, handItem, hook, cap.rewards);
            }

            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        });
    }

    private static List<ItemStack> getTreasureChestLoot(ServerLevel level, boolean isGolden) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(level.dimension() == Level.NETHER ? StardewFishing.TREASURE_CHEST_NETHER_LOOT : StardewFishing.TREASURE_CHEST_LOOT);
        List<ItemStack> items = new ArrayList<>();

        int rolls;
        if (isGolden) {
            rolls = 2; // 100% for at least 2
            if (level.random.nextFloat() < 0.25F) {
                rolls++; // 1 in 4 chance to get 3

                if (level.random.nextFloat() < 0.5F) {
                    rolls++; // 1 in 8 chance to get 4
                }
            }
        } else {
            rolls = 1;
        }

        for (int i = 0; i < rolls; i++) {
            items.addAll(lootTable.getRandomItems((new LootParams.Builder(level)).create(LootContextParamSets.EMPTY)));
        }

        return items;
    }

    public static class CapProvider implements ICapabilityProvider {
        public static final Capability<FishingHookLogic> CAP = CapabilityManager.get(new CapabilityToken<>() {});
        private static final ResourceLocation NAME = StardewFishing.resource("hook");

        private final LazyOptional<FishingHookLogic> optional = LazyOptional.of(FishingHookLogic::new);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return getCapability(cap);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
            return cap == CAP ? optional.cast() : LazyOptional.empty();
        }
    }
}
