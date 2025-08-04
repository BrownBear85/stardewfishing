package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FishingHook.class)
public abstract class FishingHookMixin extends Entity implements FishingHookAccessor {
    private FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "catchingFish", at = @At(value = "HEAD"), cancellable = true)
    private void cancel_catchingFish(BlockPos pPos, CallbackInfo ci) {
        if (getNibble() <= 0 && getTimeUntilHooked() <= 0 && getTimeUntilLured() <= 0) {
            // replicate vanilla
            int time = Mth.nextInt(random, 100, 600);
            time -= getLureSpeed() * 20 * 5;

            // apply configurable reduction
            time = Math.max(10, (int) (time * SFConfig.getBiteTimeMultiplier()));

            setTimeUntilLured(time);
        }

        if (!FishingHookAttachment.get((FishingHook) (Object) this).getRewards().isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"),
            cancellable = true)
    public void retrieve(ItemStack pStack, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> items) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
        if (player == null) return;

        if (items.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
            FishingHookAttachment.get(hook).getRewards().addAll(items);
            if (FishingHookLogic.startStardewMinigame(player)) {
                cir.cancel();
            }
        } else {
            FishingHookLogic.modifyRewards(items, 0, 0);
            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
