package com.bonker.stardewfishing.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {
    @ModifyArg(method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
            index = 2)
    private Consumer<LivingEntity> e(Consumer<LivingEntity> onBroken, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        return e -> {
            ForgeEventFactory.onPlayerDestroyItem(player, player.getItemInHand(hand), hand);
            onBroken.accept(e);
        };
    }
}
