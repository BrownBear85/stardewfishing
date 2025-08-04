package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFAttachmentTypes;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.proxy.QualityFoodProxy;
import com.bonker.stardewfishing.server.AttributeCache;
import com.bonker.stardewfishing.server.LockableList;
import com.bonker.stardewfishing.server.data.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import com.bonker.stardewfishing.server.data.MinigameModifiersReloadListener;
import com.bonker.stardewfishing.server.event.StardewMinigameEndedEvent;
import com.bonker.stardewfishing.server.event.StardewMinigameModifyRewardsEvent;
import com.bonker.stardewfishing.server.event.StardewMinigameStartedEvent;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FishingHookLogic {
    // todo: remove this, exists for backward compatibility with tide
    @Deprecated(forRemoval = true, since = "3.0")
    public static Optional<ArrayList<ItemStack>> getStoredRewards(FishingHook entity) {
        return Optional.of(entity.getData(SFAttachmentTypes.HOOK).getRewards());
    }

    public static boolean startStardewMinigame(ServerPlayer player) {
        if (player.fishing == null || player instanceof FakePlayer) return false;

        FishingHookAttachment data = FishingHookAttachment.get(player.fishing);

        // A minigame is already in progress
        if (data.getEvent() != null) {
            StardewFishing.LOGGER.warn("{} tried to start a minigame while playing one", player.getScoreboardName());
            return true;
        }

        data.getRewards().lock();

        ItemStack fish = data.getRewards().stream()
                .filter(stack -> stack.is(StardewFishing.STARTS_MINIGAME))
                .findFirst()
                .orElseThrow();

        InteractionHand rodHand = getRodHand(player);
        if (rodHand == null) {
            StardewFishing.LOGGER.warn("{} tried to start a minigame without a fishing rod", player.getScoreboardName());
            return false;
        }

        BlockPos pos = BlockPos.containing(player.fishing.position());
        FluidState fluid = player.level().getBlockState(pos).getFluidState();
        if (fluid.isEmpty()) {
            fluid = player.level().getBlockState(pos.below()).getFluidState();
        }

        AttributeCache.add(player);

        ItemStack fishingRod = player.getItemInHand(rodHand);
        StardewMinigameStartedEvent startEvent = new StardewMinigameStartedEvent(player, player.fishing, fishingRod, fish, FishBehaviorReloadListener.getBehavior(fish), fluid.is(FluidTags.LAVA));

        ItemUtils.getAllModifierItems(fishingRod, player.registryAccess()).forEach(stack ->
                MinigameModifiersReloadListener.getModifiers(stack)
                        .ifPresent(modifiers -> modifiers.apply(startEvent)));

        data.setEvent(startEvent);
        NeoForge.EVENT_BUS.post(startEvent);

        double chestChance = SFConfig.getTreasureChestChance() + startEvent.getTreasureChanceBonus();
        if (startEvent.isForcedTreasureChest() || player.getRandom().nextFloat() < chestChance) {
            data.setTreasureChest(true);
            if (startEvent.isForcedGoldenChest() || player.getRandom().nextFloat() < SFConfig.getGoldenChestChance()) {
                data.setGoldenChest(true);
            }
        }

        PacketDistributor.sendToPlayer(player, new S2CStartMinigamePacket(data));
        return true;
    }

    public static void endMinigame(ServerPlayer player, boolean success, double accuracy, boolean gotChest, int qualityBoost, @Nullable ItemStack fishingRod) {
        if (player.fishing == null) {
            return;
        }

        StardewMinigameEndedEvent endEvent = new StardewMinigameEndedEvent(player, player.fishing, fishingRod, success, accuracy, gotChest);
        if (fishingRod != null) {
            NeoForge.EVENT_BUS.post(endEvent);
        }

        if (endEvent.wasSuccessful() && !player.level().isClientSide) {
            modifyRewards(player, endEvent.getAccuracy(), qualityBoost);
            giveRewards(player, endEvent.getAccuracy(), endEvent.gotChest(), fishingRod);
        }

        if (player.fishing != null) {
            player.fishing.discard();
        }

        AttributeCache.remove(player);
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
        modifyRewards(FishingHookAttachment.get(player.fishing).getRewards(), accuracy, qualityBoost);
    }

    public static void modifyRewards(List<ItemStack> rewards, double accuracy, int qualityBoost) {
        if (StardewFishing.QUALITY_FOOD_INSTALLED) {
            int quality = Mth.clamp(SFConfig.getQuality(accuracy) + qualityBoost, 0, 3);
            for (ItemStack reward : rewards) {
                if (reward.is(StardewFishing.STARTS_MINIGAME)) {
                    QualityFoodProxy.applyQuality(reward, quality);
                }
            }
        }
    }

    public static void giveRewards(ServerPlayer player, double accuracy, boolean gotChest, ItemStack fishingRod) {
        if (player.fishing == null) return;

        FishingHook hook = player.fishing;

        FishingHookAttachment data = FishingHookAttachment.get(hook);
        LockableList<ItemStack> rewards = data.getRewards();
        rewards.unlock();

        if (data.hasTreasureChest() && gotChest) {
            rewards.addAll(getTreasureChestLoot(player.serverLevel(), data.hasGoldenChest()));
        }

        StardewMinigameModifyRewardsEvent modifyRewardsEvent = new StardewMinigameModifyRewardsEvent(player, hook, fishingRod, rewards);
        NeoForge.EVENT_BUS.post(modifyRewardsEvent);

        if (rewards.isEmpty()) {
            hook.discard();
        }

        if (NeoForge.EVENT_BUS.post(new ItemFishedEvent(rewards, 1, hook)).isCanceled()) {
            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            hook.discard();
            return;
        }

        ServerLevel level = player.serverLevel();
        for (ItemStack reward : rewards) {
            if (reward.is(ItemTags.FISHES)) {
                player.awardStat(Stats.FISH_CAUGHT);
            }

            ItemEntity itementity;
            if (data.getEvent().isLavaFishing()) {
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

            int exp = (int) ((player.getRandom().nextInt(6) + 1) * SFConfig.getMultiplier(accuracy, player, data.getEvent().getExpMultiplier()));
            level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, exp));

            InteractionHand hand = getRodHand(player);
            ItemStack handItem = hand != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, handItem, hook, rewards);
        }

        player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static List<ItemStack> getTreasureChestLoot(ServerLevel level, boolean isGolden) {
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(level.dimension() == Level.NETHER ? StardewFishing.TREASURE_CHEST_NETHER_LOOT : StardewFishing.TREASURE_CHEST_LOOT);
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

    public static InteractionHand getRodHand(Player player) {
        boolean mainHand = player.getItemInHand(InteractionHand.MAIN_HAND).canPerformAction(ItemAbilities.FISHING_ROD_CAST);
        if (mainHand) return InteractionHand.MAIN_HAND;

        boolean offHand = player.getItemInHand(InteractionHand.OFF_HAND).canPerformAction(ItemAbilities.FISHING_ROD_CAST);
        if (offHand) return InteractionHand.OFF_HAND;

        return null;
    }

    public static Optional<ItemStack> damageBobber(ItemStack bobber, ServerPlayer player) {
        if (!bobber.isDamageableItem()) {
            return Optional.empty();
        }
        bobber.hurtAndBreak(1, player.serverLevel(), player, p -> {
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS);
            Vec3 particlePos = player.getEyePosition().add(player.getLookAngle());
            player.serverLevel().sendParticles(new ItemParticleOption(ParticleTypes.ITEM, bobber), particlePos.x(), particlePos.y(), particlePos.z(), 15, 0.1, 0.1, 0.1, 0.1);
            player.displayClientMessage(Component.translatable("stardew_fishing.bobber_broke", bobber.getDisplayName()), true);
        });
        return Optional.of(bobber);
    }
}
