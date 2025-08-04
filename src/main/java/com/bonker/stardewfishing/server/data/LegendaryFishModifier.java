package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFItems;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

public class LegendaryFishModifier implements IGlobalLootModifier {
    public static final MapCodec<LegendaryFishModifier> CODEC = MapCodec.unit(LegendaryFishModifier::new);

    private static final List<ResourceLocation> FISHING_LOOT_TABLES = List.of(
            BuiltInLootTables.FISHING.location(),
            ResourceLocation.fromNamespaceAndPath("aquaculture", "gameplay/fishing/fish"),
            ResourceLocation.fromNamespaceAndPath("aquaculture", "gameplay/fishing/lava/fish"),
            ResourceLocation.fromNamespaceAndPath("aquaculture", "gameplay/fishing/nether/fish"),
            ResourceLocation.fromNamespaceAndPath("tide", "gameplay/fishing/climates/"),
            ResourceLocation.fromNamespaceAndPath("tide", "gameplay/fishing/special/"),
            ResourceLocation.fromNamespaceAndPath("tide", "gameplay/fishing/special")
    );

    private static final List<Vector2i> BIOME_CHECK_OFFSETS = List.of(
            new Vector2i(16, 0), new Vector2i(-16, 0), new Vector2i(0, 16), new Vector2i(0, -16),
            new Vector2i(16, 16), new Vector2i(16, -16), new Vector2i(-16, -16), new Vector2i(-16, 16),
            new Vector2i(32, 0), new Vector2i(-32, 0), new Vector2i(0, 32), new Vector2i(0, -32),
            new Vector2i(32, 32), new Vector2i(32, -32), new Vector2i(-32, -32), new Vector2i(-32, 32));

    @Override
    public @NotNull ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (generatedLoot.isEmpty()) {
            // definitely no fish to replace
            return generatedLoot;
        }

        if (context.getRandom().nextFloat() >= SFConfig.getLegendaryFishChance(context.getLuck())) {
            // didn't get lucky
            return generatedLoot;
        }

        int index = 0;
        for (ItemStack stack : generatedLoot) {
            if (stack.is(ItemTags.FISHES)) {
                break;
            }
            index++;
        }

        if (index == generatedLoot.size()) {
            // no fish to replace
            return generatedLoot;
        }

        ResourceLocation lootId = context.getQueriedLootTableId();
        if (FISHING_LOOT_TABLES.stream().noneMatch(id -> lootId.getNamespace().equals(id.getNamespace()) && lootId.getPath().startsWith(id.getPath()))) {
            // this isn't a fishing loot table
            return generatedLoot;
        }

        ServerLevel level = context.getLevel();
        BlockPos pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));

        LegendaryCategory category = findLegendaryCategory(level, pos);
        if (category != null) {
            generatedLoot.set(index, new ItemStack(category.chooseFish(level)));
        }

        return generatedLoot;
    }

    @Nullable
    private LegendaryCategory findLegendaryCategory(Level level, BlockPos pos) {
        LegendaryCategory direct = null;

        Holder<Biome> biome = level.getBiome(pos);
        if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
        else if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        else if (LegendaryCategory.WARM_OCEAN.matches(biome)) direct = LegendaryCategory.WARM_OCEAN;
        else if (LegendaryCategory.NORMAL_OCEAN.matches(biome)) direct = LegendaryCategory.NORMAL_OCEAN;
        else if (LegendaryCategory.RIVER.matches(biome)) direct = LegendaryCategory.RIVER;

        for (Vector2i offset : BIOME_CHECK_OFFSETS) {
            biome = level.getBiome(pos.offset(offset.x, 0, offset.y));
            if (LegendaryCategory.JUNGLE.matches(biome)) return LegendaryCategory.JUNGLE;
            if (LegendaryCategory.ARID.matches(biome)) return LegendaryCategory.ARID;
        }

        return direct;
    }

    @Override
    public MapCodec<LegendaryFishModifier> codec() {
        return CODEC;
    }

    public enum LegendaryCategory {
        JUNGLE(StardewFishing.HAS_JUNGLE_FISH),
        ARID(StardewFishing.HAS_ARID_FISH),
        NORMAL_OCEAN(StardewFishing.HAS_NORMAL_OCEAN_FISH),
        WARM_OCEAN(StardewFishing.HAS_WARM_OCEAN_FISH),
        RIVER(StardewFishing.HAS_RIVER_FISH);

        private final TagKey<Biome> tag;

        LegendaryCategory(TagKey<Biome> tag) {
            this.tag = tag;
        }

        public boolean matches(Holder<Biome> biome) {
            return biome.is(tag);
        }

        public Item chooseFish(Level level) {
            boolean rand = level.random.nextBoolean();
            return (switch (this) {
                case NORMAL_OCEAN -> rand ? SFItems.STORM_TARPON : SFItems.GOLIATH_GROUPER;
                case WARM_OCEAN -> rand ? SFItems.BLAZING_OARFISH : SFItems.CYCLOPS_MAHIMAHI;
                case RIVER -> rand ? SFItems.DEMON_GAR : SFItems.CRYSTALLINE_SNAKEHEAD;
                case JUNGLE -> rand ? SFItems.CHROMATIC_ARAPAIMA : SFItems.VAMPIRE_PAYARA;
                case ARID -> rand ? SFItems.SABRETOOTHED_TIGERFISH : SFItems.GOLDEN_SNOOK;
            }).get();
        }
    }
}
