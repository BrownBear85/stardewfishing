package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.StardewFishingClient;
import com.bonker.stardewfishing.server.data.MinigameModifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncModifiersPacket(Map<Item, MinigameModifiers> data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncModifiersPacket> TYPE =
            new CustomPacketPayload.Type<>(StardewFishing.resource("s2c_sync_modifiers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncModifiersPacket> STREAM_CODEC =
            StreamCodec.of((buf, value) -> value.encode(buf), S2CSyncModifiersPacket::fromBytes);

    public static S2CSyncModifiersPacket fromBytes(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Item, MinigameModifiers> data = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
            MinigameModifiers modifiers = MinigameModifiers.read(buf);
            data.put(item, modifiers);
        }
        return new S2CSyncModifiersPacket(data);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(data.size());
        for (Map.Entry<Item, MinigameModifiers> entry : data.entrySet()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
            entry.getValue().write(buf);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> StardewFishingClient.modifiersSupplier = () -> data);
    }
}