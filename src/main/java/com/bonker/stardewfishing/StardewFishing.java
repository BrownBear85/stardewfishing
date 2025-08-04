package com.bonker.stardewfishing;

import com.bonker.stardewfishing.common.init.*;
import com.bonker.stardewfishing.server.SFCommands;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;

@Mod(StardewFishing.MODID)
public class StardewFishing {
    public static final String MODID = "stardew_fishing";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final boolean QUALITY_FOOD_INSTALLED = ModList.get().isLoaded("quality_food");
    public static final boolean AQUACULTURE_INSTALLED = ModList.get().isLoaded("aquaculture");
    public static final boolean TIDE_INSTALLED = ModList.get().isLoaded("tide");

    public static final TagKey<Item> STARTS_MINIGAME = TagKey.create(Registries.ITEM, resource("starts_minigame"));
    public static final TagKey<Item> MODIFIABLE_RODS = TagKey.create(Registries.ITEM, resource("modifiable_rods"));
    public static final TagKey<Item> BOBBERS = TagKey.create(Registries.ITEM, resource("bobbers"));
    public static final TagKey<Item> LEGENDARY_FISH = TagKey.create(Registries.ITEM, resource("legendary_fish"));
    public static final TagKey<Item> IN_FISH_DISPLAY = TagKey.create(Registries.ITEM, resource("in_fish_display"));

    public static final TagKey<Biome> HAS_NORMAL_OCEAN_FISH = TagKey.create(Registries.BIOME, resource("has_normal_ocean_fish"));
    public static final TagKey<Biome> HAS_WARM_OCEAN_FISH = TagKey.create(Registries.BIOME, resource("has_warm_ocean_fish"));
    public static final TagKey<Biome> HAS_RIVER_FISH = TagKey.create(Registries.BIOME, resource("has_river_fish"));
    public static final TagKey<Biome> HAS_ARID_FISH = TagKey.create(Registries.BIOME, resource("has_arid_fish"));
    public static final TagKey<Biome> HAS_JUNGLE_FISH = TagKey.create(Registries.BIOME, resource("has_jungle_fish"));

    public static final ResourceKey<LootTable> TREASURE_CHEST_LOOT = resource(Registries.LOOT_TABLE, "treasure_chest");
    public static final ResourceKey<LootTable> TREASURE_CHEST_NETHER_LOOT = resource(Registries.LOOT_TABLE, "treasure_chest_nether");

    public static String MOD_NAME;

    public static final Style GREEN = Style.EMPTY.withColor(0xb4ce99);
    public static final Style RED = Style.EMPTY.withColor(0xca7d6c);
    public static final Style LIGHTER_COLOR = Style.EMPTY.withColor(0xcca06d);
    public static final Style LIGHT_COLOR = Style.EMPTY.withColor(0xaf7a3e);
    public static final Style DARK_COLOR = Style.EMPTY.withColor(0x7e582c);
    public static final Style LEGENDARY = Style.EMPTY.withColor(SFItems.LEGENDARY_FISH_COLOR);

    public StardewFishing(IEventBus bus, ModContainer container) {
        IModInfo info = container.getModInfo();
        MOD_NAME = info.getDisplayName() + " " + info.getVersion();

        SFItems.ITEMS.register(bus);
        SFBlocks.BLOCKS.register(bus);
        SFAttributes.ATTRIBUTES.register(bus);
        SFCommands.ARGUMENT_TYPES.register(bus);
        SFItems.CREATIVE_MODE_TABS.register(bus);
        SFParticles.PARTICLE_TYPES.register(bus);
        SFSoundEvents.SOUND_EVENTS.register(bus);
        SFLootModifiers.LOOT_MODIFIERS.register(bus);
        SFBlockEntities.BLOCK_ENTITY_TYPES.register(bus);
        SFAttachmentTypes.ATTACHMENT_TYPES.register(bus);
        SFComponentTypes.DATA_COMPONENT_TYPES.register(bus);
        SFLootPoolEntryTypes.LOOT_POOL_ENTRY_TYPES.register(bus);

        container.registerConfig(ModConfig.Type.SERVER, SFConfig.SERVER_SPEC);
    }
    
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static <T> ResourceKey<T> resource(ResourceKey<Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, resource(path));
    }
}
