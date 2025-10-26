package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.llamalad7.mixinextras.sugar.Local;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.init.AquaSounds;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity")
public abstract class AquaFishingBobberEntityMixin extends FishingHook implements FishingHookAccessor {
    @Shadow protected abstract List<ItemStack> getLoot(LootParams lootParams, ServerLevel serverLevel);

    @Accessor protected abstract ItemStack getFishingRod();

    private AquaFishingBobberEntityMixin(EntityType<? extends FishingHook> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "catchingFish(Lnet/minecraft/core/BlockPos;)V", at = @At(value = "HEAD"), cancellable = true)
    private void cancel_catchingFish(BlockPos pPos, CallbackInfo ci) {
        if (getNibble() <= 0 && getTimeUntilHooked() <= 0 && getTimeUntilLured() <= 0) {
            // replicate vanilla
            int time = Mth.nextInt(random, 100, 600);
            time -= getLureSpeed() * 20 * 5;

            // apply configurable reduction
            time = Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier()));

            setTimeUntilLured(time);
        }

        if (FishingHookLogic.getStoredRewards(this).map(list -> !list.isEmpty()).orElse(false)) {
            ci.cancel();
        }
    }

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z",
                    ordinal = 0),
            cancellable = true)
    public void retrieve(ItemStack pStack, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> items, @Local LootParams lootParams, @Local ServerLevel serverLevel) {
        AquaFishingBobberEntity hook = (AquaFishingBobberEntity) (Object) this;
        ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
        if (player == null) return;

        if (items.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
            FishingHookLogic.getStoredRewards(hook).ifPresent(rewards -> rewards.addAll(items));
            if (hook.hasHook() && hook.getHook().getDoubleCatchChance() > 0.0 && this.random.nextDouble() <= hook.getHook().getDoubleCatchChance()) {
                List<ItemStack> doubleLoot = getLoot(lootParams, serverLevel);
                if (!doubleLoot.isEmpty()) {
                    MinecraftForge.EVENT_BUS.post(new ItemFishedEvent(doubleLoot, 0, this));
                    FishingHookLogic.getStoredRewards(hook).ifPresent(rewards -> rewards.addAll(doubleLoot));
                    playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                }
            }

            if (!player.isCreative()) {
                ItemStackHandler rodHandler = AquaFishingRodItem.getHandler(getFishingRod());
                ItemStack bait = rodHandler.getStackInSlot(1);
                if (!bait.isEmpty()) {
                    if (bait.hurt(1, hook.level().random, null)) {
                        bait.shrink(1);
                        playSound(AquaSounds.BOBBER_BAIT_BREAK.get(), 0.7F, 0.2F);
                    }

                    rodHandler.setStackInSlot(1, bait);
                }
            }

            if (FishingHookLogic.startStardewMinigame(player)) {
                cir.cancel();
            }
        } else {
            FishingHookLogic.modifyRewards(items, 0, 0);
            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
