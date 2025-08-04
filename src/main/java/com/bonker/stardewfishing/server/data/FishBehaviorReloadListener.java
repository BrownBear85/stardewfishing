package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishBehavior;
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
import java.util.*;

public class FishBehaviorReloadListener extends SimplePreparableReloadListener<Map<String, JsonObject>> {
    private static final Gson GSON_INSTANCE = new Gson();
    private static final ResourceLocation LOCATION = StardewFishing.resource("fish_behaviors.json");
    private static final ResourceLocation OLD_LOCATION = StardewFishing.resource("data.json");
    private static FishBehaviorReloadListener INSTANCE;

    private final Map<Item, FishBehavior> fishBehaviors = new HashMap<>();
    private final List<ResourceLocation> keys = new ArrayList<>();
    private FishBehavior defaultBehavior;

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<String, JsonObject> objects = new HashMap<>();
        for (Resource resource : pResourceManager.getResourceStack(LOCATION)) {
            try (InputStream inputstream = resource.open();
                 Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            ) {
                objects.put(resource.sourcePackId(), GsonHelper.fromJson(GSON_INSTANCE, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid json in fish behavior list {} in data pack {}", LOCATION, resource.sourcePackId(), exception);
            }
        }

        for (Resource resource : pResourceManager.getResourceStack(OLD_LOCATION)) {
            try (InputStream inputstream = resource.open();
                 Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            ) {
                StardewFishing.LOGGER.error("Error in datapack {}: Fish behavior list found at stardew_fishing/data.json. As of 3.0, fish behavior has been moved to stardew_fishing/fish_behaviors.json. This file has been loaded, but it will not be in the future.", resource.sourcePackId());
                objects.put(resource.sourcePackId(), GsonHelper.fromJson(GSON_INSTANCE, reader, JsonObject.class));
            } catch (RuntimeException | IOException exception) {
                StardewFishing.LOGGER.error("Invalid json in fish behavior list {} in data pack {}", LOCATION, resource.sourcePackId(), exception);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> jsonObjects, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        for (Map.Entry<String, JsonObject> entry : jsonObjects.entrySet()) {
            FishBehaviorList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(errorMsg -> StardewFishing.LOGGER.warn(makeError(entry.getKey(), errorMsg)))
                    .ifPresent(behaviorList -> {
                        behaviorList.behaviors.forEach((loc, fishBehavior) -> {
                            Item item = BuiltInRegistries.ITEM.get(loc);
                            if (item == Items.AIR) {
                                if (ModList.get().isLoaded(loc.getNamespace())) {
                                    throw new RuntimeException(makeError(entry.getKey(), "Mod '" + loc.getNamespace() + "' present but item not registered: " + loc.getPath()));
                                }
                            } else {
                                if (behaviorList.replace || !fishBehaviors.containsKey(item)) {
                                    fishBehaviors.put(item, fishBehavior);
                                    keys.add(loc);
                                }

                                if (behaviorList.replace || defaultBehavior == null) {
                                    behaviorList.defaultBehavior.ifPresent(behavior -> defaultBehavior = behavior);
                                }
                            }
                        });
                    });
        }

        Collections.sort(keys);
    }

    private static String makeError(String datapackID, String description) {
        return "Failed to decode fish behavior list " + LOCATION + " in data pack " + datapackID + " - " + description;
    }

    public static FishBehaviorReloadListener create() {
        INSTANCE = new FishBehaviorReloadListener();
        return INSTANCE;
    }

    public static FishBehavior getBehavior(@Nullable ItemStack stack) {
        if (stack == null) return INSTANCE.defaultBehavior;
        return INSTANCE.fishBehaviors.getOrDefault(stack.getItem(), INSTANCE.defaultBehavior);
    }

    public static List<ResourceLocation> getKeys() {
        return INSTANCE.keys;
    }

    private record FishBehaviorList(boolean replace, Map<ResourceLocation, FishBehavior> behaviors, Optional<FishBehavior> defaultBehavior) {
        private static final Codec<FishBehaviorList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(FishBehaviorList::replace),
                Codec.unboundedMap(ResourceLocation.CODEC, FishBehavior.CODEC).fieldOf("behaviors").forGetter(FishBehaviorList::behaviors),
                FishBehavior.CODEC.optionalFieldOf("defaultBehavior").forGetter(FishBehaviorList::defaultBehavior)
        ).apply(inst, FishBehaviorList::new));
    }
}
