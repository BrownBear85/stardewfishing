package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.StardewFishing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MinigameModifiersReloadListener extends SimplePreparableReloadListener<Map<String, JsonObject>> {
    private static final Gson GSON_INSTANCE = new Gson();
    private static final ResourceLocation LOCATION = StardewFishing.resource("minigame_modifiers.json");
    @Nullable
    private static MinigameModifiersReloadListener INSTANCE;

    private final Map<Item, MinigameModifiers> modifiers = new HashMap<>();

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<String, JsonObject> objects = new HashMap<>();
        for (Resource resource : pResourceManager.getResourceStack(LOCATION)) {
            try (InputStream inputstream = resource.open();
                 Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            ) {
                objects.put(resource.sourcePackId(), GsonHelper.fromJson(GSON_INSTANCE, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid json in minigame modifiers list {} in data pack {}", LOCATION, resource.sourcePackId(), exception);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> jsonObjects, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        for (Map.Entry<String, JsonObject> entry : jsonObjects.entrySet()) {
            ModifiersList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(errorMsg -> StardewFishing.LOGGER.warn("Failed to decode minigame modifiers list {} in data pack {} - {}", LOCATION, entry.getKey(), errorMsg))
                    .ifPresent(behaviorList -> behaviorList.modifiers.forEach((loc, minigameModifiers) -> {
                        Item item = BuiltInRegistries.ITEM.get(loc);
                        if (item == Items.AIR) {
                            if (ModList.get().isLoaded(loc.getNamespace())) {
                                throw new RuntimeException("Mod '" + loc.getNamespace() + "' present but item not registered: " + loc.getPath());
                            }
                        } else {
                            if (behaviorList.replace || !modifiers.containsKey(item)) {
                                modifiers.put(item, minigameModifiers);
                            } else {
                                modifiers.computeIfPresent(item, (i, m) -> m.merge(minigameModifiers));
                            }
                        }
                    }));
        }
    }

    public static MinigameModifiersReloadListener create() {
        INSTANCE = new MinigameModifiersReloadListener();
        return INSTANCE;
    }

    public static Optional<MinigameModifiers> getModifiers(ItemStack stack) {
        if (INSTANCE != null && INSTANCE.modifiers.containsKey(stack.getItem())) {
            return Optional.of(INSTANCE.modifiers.get(stack.getItem()));
        }
        return Optional.empty();
    }

    private record ModifiersList(boolean replace, Map<ResourceLocation, MinigameModifiers> modifiers) {
        private static final Codec<ModifiersList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(ModifiersList::replace),
                Codec.unboundedMap(ResourceLocation.CODEC, MinigameModifiers.CODEC).fieldOf("modifiers").forGetter(ModifiersList::modifiers)
        ).apply(inst, ModifiersList::new));
    }
}