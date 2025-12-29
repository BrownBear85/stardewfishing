package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import javax.annotation.Nullable;
import java.util.*;

public class DimensionTextureManager extends SimplePreparableReloadListener<Set<ResourceLocation>> {
    @Nullable
    private static DimensionTextureManager INSTANCE;

    private final Map<ResourceLocation, ResourceLocation> dimensionTextureMap = new HashMap<>();

    public static DimensionTextureManager getOrCreate() {
        if (INSTANCE == null) {
            INSTANCE = new DimensionTextureManager();
        }
        return INSTANCE;
    }

    @Override
    protected Set<ResourceLocation> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return pResourceManager.listResources("textures/gui/minigame_backgrounds",
                        loc -> loc.getNamespace().equals(StardewFishing.MODID) && loc.getPath().endsWith(".png"))
                .keySet();
    }

    @Override
    protected void apply(Set<ResourceLocation> candidates, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.dimensionTextureMap.clear();

        for (ResourceLocation texture : candidates) {
            String[] parts = texture.getPath().substring(0, texture.getPath().length() - 4).split("/");
            if (parts.length != 5) {
                StardewFishing.LOGGER.warn("Invalid location for a minigame dimension texture: {}", texture);
                continue;
            }

            ResourceLocation dimensionId = ResourceLocation.tryBuild(parts[3], parts[4]);
            if (dimensionId == null) {
                StardewFishing.LOGGER.warn("Minigame dimension texture path does not represent a valid dimension id: {}", texture);
                continue;
            }

            StardewFishing.LOGGER.info("Found minigame texture for dimension: {}", dimensionId);
            dimensionTextureMap.put(dimensionId, texture);
        }
    }

    public ResourceLocation getMinigameTexture(@Nullable ClientLevel level) {
        if (level != null) {
            ResourceLocation dimensionId = level.registryAccess().registry(Registries.DIMENSION_TYPE).map(registry -> registry.getKey(level.dimensionType())).orElse(null);
            if (dimensionId != null && dimensionTextureMap.containsKey(dimensionId)) {
                return dimensionTextureMap.get(dimensionId);
            }
        }
        return dimensionTextureMap.get(BuiltinDimensionTypes.OVERWORLD.location());
    }
}