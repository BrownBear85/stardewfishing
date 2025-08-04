package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.proxy.ClientProxy;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CStartMinigamePacket(int idleTime, float topSpeed, float upAcceleration, float downAcceleration,
                                     int avgDistance, int moveVariation, ItemStack fish, boolean treasureChest,
                                     boolean goldenChest, float lineStrength, int barSize, boolean lava)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CStartMinigamePacket> TYPE =
            new CustomPacketPayload.Type<>(StardewFishing.resource("s2c_start_minigame"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CStartMinigamePacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> value.encode(buf), S2CStartMinigamePacket::new);

    public S2CStartMinigamePacket(FishingHookAttachment data) {
        this(
                data.getEvent().getIdleTime(), data.getEvent().getTopSpeed(), data.getEvent().getUpAcceleration(),
                data.getEvent().getDownAcceleration(), data.getEvent().getAvgDistance(),
                data.getEvent().getMoveVariation(), data.getEvent().getFish(), data.hasTreasureChest(),
                data.hasGoldenChest(), (float) data.getEvent().getLineStrength(), data.getEvent().getBarSize(),
                data.getEvent().isLavaFishing()
        );
    }

    public S2CStartMinigamePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readShort(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readShort(), buf.readShort(),
                ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readShort(), buf.readBoolean());
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeShort(idleTime);
        buf.writeFloat(topSpeed);
        buf.writeFloat(upAcceleration);
        buf.writeFloat(downAcceleration);
        buf.writeShort(avgDistance);
        buf.writeShort(moveVariation);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, fish);
        buf.writeBoolean(treasureChest);
        buf.writeBoolean(goldenChest);
        buf.writeFloat(lineStrength);
        buf.writeShort(barSize);
        buf.writeBoolean(lava);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ClientProxy.openFishingScreen(this, context);
    }
}
