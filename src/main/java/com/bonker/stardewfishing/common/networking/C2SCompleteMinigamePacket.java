package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SCompleteMinigamePacket(boolean success, double accuracy, boolean gotChest) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SCompleteMinigamePacket> TYPE =
            new CustomPacketPayload.Type<>(StardewFishing.resource("c2s_complete_minigame"));

    public static final StreamCodec<ByteBuf, C2SCompleteMinigamePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, C2SCompleteMinigamePacket::success,
            ByteBufCodecs.DOUBLE, C2SCompleteMinigamePacket::accuracy,
            ByteBufCodecs.BOOL, C2SCompleteMinigamePacket::gotChest,
            C2SCompleteMinigamePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        FishingHook hook = player.fishing;
        if (hook == null || FishingHookAttachment.get(hook).getRewards().isEmpty()) {
            StardewFishing.LOGGER.warn("{} tried to complete a fishing minigame that doesn't exist", player.getScoreboardName());
            return;
        }

        InteractionHand hand = FishingHookLogic.getRodHand(player);
        if (hand == null) {
            FishingHookLogic.endMinigame(player, false, 0, gotChest, 0, null);
            StardewFishing.LOGGER.warn("{} tried to complete a fishing minigame without a fishing rod", player.getScoreboardName());
        } else {
            ItemStack fishingRod = player.getItemInHand(hand);

            int qualityBoost = FishingHookAttachment.get(hook).getEvent().getQualityBoost();
            FishingHookLogic.endMinigame(player, success, accuracy, gotChest, qualityBoost, fishingRod);
            fishingRod.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

            ItemUtils.damageBobber(fishingRod, player);
        }
    }
}
