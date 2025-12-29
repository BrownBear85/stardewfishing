package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.proxy.MinigameModifiersSupplier;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

@Mod(value = StardewFishing.MODID, dist = Dist.CLIENT)
public class StardewFishingClient {
    public static final Lazy<KeyMapping> MINIGAME_BUTTON = Lazy.of(() -> new KeyMapping(
            "key.stardew_fishing.minigame_button",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_1),
            "key.categories.stardew_fishing"));

    public StardewFishingClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SFConfig.CLIENT_SPEC);
    }

    @Nullable
    public static MinigameModifiersSupplier modifiersSupplier;
}
