package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.client.FishingScreen;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientProxy {
    public static void openFishingScreen(S2CStartMinigamePacket packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new FishingScreen(packet));
    }

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    public static float getPartialTick() {
        if (Minecraft.getInstance().getTimer() instanceof DeltaTracker.Timer timer) {
            return timer.deltaTickResidual;
        } else {
            return 1;
        }
    }
}
