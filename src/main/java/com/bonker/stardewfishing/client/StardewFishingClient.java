package com.bonker.stardewfishing.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class StardewFishingClient {
    public static final Supplier<KeyMapping> MINIGAME_BUTTON = Suppliers.memoize(() -> new KeyMapping(
            "key.stardew_fishing.minigame_button",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_1),
            "key.categories.stardew_fishing"));
}