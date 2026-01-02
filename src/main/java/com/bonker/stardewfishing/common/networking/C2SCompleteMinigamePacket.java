package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.proxy.ItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SCompleteMinigamePacket(boolean success, double accuracy, boolean gotChest) {
    public static C2SCompleteMinigamePacket decode(FriendlyByteBuf buf) {
        boolean success = buf.readBoolean();
        return new C2SCompleteMinigamePacket(success, success ? buf.readDouble() : -1, buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(success);
        if (success) buf.writeDouble(accuracy);
        buf.writeBoolean(gotChest);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null) {
            return;
        }

        FishingHook hook = player.fishing;
        if (hook == null || FishingHookLogic.getStoredRewards(hook).isEmpty()) {
            StardewFishing.LOGGER.warn("{} tried to complete a fishing minigame that doesn't exist", player.getScoreboardName());
            return;
        }

        contextSupplier.get().enqueueWork(() -> {
            InteractionHand hand = ItemUtils.getRodHand(player);
            if (hand == null) {
                FishingHookLogic.endMinigame(player, false, 0, gotChest, 0, null);
                StardewFishing.LOGGER.warn("{} tried to complete a fishing minigame without a fishing rod", player.getScoreboardName());
            } else {
                ItemStack fishingRod = player.getItemInHand(hand);

                ItemUtils.damageAttachedBobber(fishingRod, player);

                int[] qualityBoost = {0};
                hook.getCapability(FishingHookLogic.CapProvider.CAP).ifPresent(cap -> qualityBoost[0] = cap.event.getQualityBoost());

                FishingHookLogic.endMinigame(player, success, accuracy, gotChest, qualityBoost[0], fishingRod);
                ItemStack rodCache = fishingRod.copy();
                fishingRod.hurtAndBreak(1, player, p -> {
                    ForgeEventFactory.onPlayerDestroyItem(player, rodCache, hand);
                    p.broadcastBreakEvent(hand);
                });
            }
        });
    }
}
