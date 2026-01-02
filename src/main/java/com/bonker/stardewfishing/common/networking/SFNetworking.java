package com.bonker.stardewfishing.common.networking;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class SFNetworking {
    private static final String PROTOCOL_VERSION = "3";

    private static SimpleChannel CHANNEL;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(StardewFishing.resource("packets"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        CHANNEL.registerMessage(id(),
                S2CStartMinigamePacket.class,
                S2CStartMinigamePacket::encode,
                S2CStartMinigamePacket::new,
                S2CStartMinigamePacket::handle);

        CHANNEL.registerMessage(id(),
                S2CSyncModifiersPacket.class,
                S2CSyncModifiersPacket::encode,
                S2CSyncModifiersPacket::fromBytes,
                S2CSyncModifiersPacket::handle);

        CHANNEL.registerMessage(id(),
                C2SCompleteMinigamePacket.class,
                C2SCompleteMinigamePacket::encode,
                C2SCompleteMinigamePacket::decode,
                C2SCompleteMinigamePacket::handle);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <MSG> void sendToServer(MSG packet) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), packet);
    }
}
