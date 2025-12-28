package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.client.StardewFishingClient;
import com.bonker.stardewfishing.server.data.MinigameModifiers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record S2CSyncModifiersPacket(Map<Item, MinigameModifiers> data) {
    public static S2CSyncModifiersPacket fromBytes(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Item, MinigameModifiers> data = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Item item = buf.readRegistryId();
            MinigameModifiers modifiers = MinigameModifiers.read(buf);
            data.put(item, modifiers);
        }
        return new S2CSyncModifiersPacket(data);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(data.size());
        for (Map.Entry<Item, MinigameModifiers> entry : data.entrySet()) {
            buf.writeRegistryId(ForgeRegistries.ITEMS, entry.getKey());
            entry.getValue().write(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> StardewFishingClient.modifiersSupplier = () -> data);
    }
}
