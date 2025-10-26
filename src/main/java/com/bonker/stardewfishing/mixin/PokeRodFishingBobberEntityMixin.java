package com.bonker.stardewfishing.mixin;



import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.common.init.SFComponentTypes;
import com.bonker.stardewfishing.common.init.SFItems;
import com.bonker.stardewfishing.common.init.SFSoundEvents;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
//import com.cobblemon.mod.common.api.spawning.SpawnBucket;
//import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity")
public abstract class PokeRodFishingBobberEntityMixin extends FishingHook {
    @Shadow
    private int waitCountdown;

    @Shadow
    private ItemStack rodStack;

    @Shadow
    private ItemStack bobberBait;

//    @Shadow
//    private SpawnBucket chosenBucket;

    @Shadow
    public abstract boolean checkReduceBiteTime(ItemStack stack);

    @Shadow
    public abstract int alterBiteTimeAttempt(int waitCountdown, ItemStack stack);

    private PokeRodFishingBobberEntityMixin(EntityType<? extends FishingHook> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

//    @Inject(method = "tickFishingLogic(Lnet/minecraft/core/BlockPos;)V", at = @At(value = "HEAD"), cancellable = true, remap = false)
//    private void tickFishingLogic(BlockPos pPos, CallbackInfo ci) {
//        if (!FishingHookAttachment.get(this).getRewards().isEmpty()) {
//            ci.cancel();
//        }
//    }
//
//    @Redirect(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/entity/fishing/PokeRodFishingBobberEntity;checkReduceBiteTime(Lnet/minecraft/world/item/ItemStack;)Z"), remap = false)
//    private boolean checkReduceBiteTime(PokeRodFishingBobberEntity instance, ItemStack stack) {
//        if (checkReduceBiteTime(stack)) {
//            waitCountdown = alterBiteTimeAttempt(waitCountdown, rodStack == null ? rodStack : bobberBait);
//        }
//
//        waitCountdown = Math.max(10, (int) (waitCountdown * SFConfig.getBiteTimeMultiplier()));
//
//        return false;
//    }
//
//    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
//            at = @At(value = "INVOKE",
//                    ordinal = 1,
//                    target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V"),
//            cancellable = true)
//    public void retrieveItem(ItemStack pStack, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> items) {
//        ServerPlayer player = (ServerPlayer) getPlayerOwner();
//        if (player == null) return;
//
//        if (items.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
//            FishingHookAttachment.get(this).getRewards().addAll(items);
//            if (FishingHookLogic.startStardewMinigame(player)) {
//                cir.cancel();
//            }
//        } else {
//            FishingHookLogic.modifyRewards(items, 0, 0);
//            player.level().playSound(null, player, SFSoundEvents.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
//        }
//    }
//
//    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
//            at = @At(value = "INVOKE",
//                    target = "Lcom/cobblemon/mod/common/entity/fishing/PokeRodFishingBobberEntity;spawnPokemonFromFishing(Lnet/minecraft/world/entity/player/Player;Lcom/cobblemon/mod/common/api/spawning/SpawnBucket;Lnet/minecraft/world/item/ItemStack;)Z"),
//            cancellable = true)
//    public void retrievePokemon(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
//        ServerPlayer player = (ServerPlayer) getPlayerOwner();
//        if (player == null) return;
//
//        ItemStack placeholderStack = new ItemStack(SFItems.POKEMON_PLACEHOLDER.get());
//        placeholderStack.set(SFComponentTypes.POKEMON_TYPE.get(), chosenBucket);
//        FishingHookAttachment.get(this).getRewards().add(placeholderStack);
//
//        if (FishingHookLogic.startStardewMinigame(player)) {
//            cir.cancel();
//        }
//    }
}
