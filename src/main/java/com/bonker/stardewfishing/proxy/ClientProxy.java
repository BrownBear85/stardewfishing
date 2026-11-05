package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.client.FishingScreen;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientProxy {
    public static void openFishingScreen(S2CStartMinigamePacket packet, IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(new FishingScreen(packet));
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
