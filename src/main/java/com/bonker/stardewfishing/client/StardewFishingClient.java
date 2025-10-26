package com.bonker.stardewfishing.client;

import com.mojang.blaze3d.platform.InputConstants;
import cpw.mods.util.Lazy;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class StardewFishingClient {
    public static final Lazy<KeyMapping> MINIGAME_BUTTON = Lazy.of(() -> new KeyMapping(
            "key.stardew_fishing.minigame_button",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_1),
            "key.categories.stardew_fishing"));
}