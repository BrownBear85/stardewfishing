package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.proxy.ClientProxy;
import com.bonker.stardewfishing.server.event.StardewMinigameStartedEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CStartMinigamePacket(int idleTime, float topSpeed, float upAcceleration, float downAcceleration,
                                     int avgDistance, int moveVariation, ItemStack fish, boolean treasureChest,
                                     boolean goldenChest, float lineStrength, int barSize) {
    public S2CStartMinigamePacket(StardewMinigameStartedEvent event, boolean treasureChest, boolean goldenChest) {
        this(event.getIdleTime(), event.getTopSpeed(), event.getUpAcceleration(), event.getDownAcceleration(),
                event.getAvgDistance(), event.getMoveVariation(), event.getFish(), treasureChest, goldenChest,
                (float) event.getLineStrength(), event.getBarSize());
    }

    public S2CStartMinigamePacket(FriendlyByteBuf buf) {
        this(buf.readShort(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readShort(), buf.readShort(),
                buf.readItem(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readShort());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(idleTime);
        buf.writeFloat(topSpeed);
        buf.writeFloat(upAcceleration);
        buf.writeFloat(downAcceleration);
        buf.writeShort(avgDistance);
        buf.writeShort(moveVariation);
        buf.writeItem(fish);
        buf.writeBoolean(treasureChest);
        buf.writeBoolean(goldenChest);
        buf.writeFloat(lineStrength);
        buf.writeShort(barSize);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> ClientProxy.openFishingScreen(this));
    }
}
