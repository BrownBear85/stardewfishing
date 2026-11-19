package com.bonker.stardewfishing.server.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class OptionalLootItem extends SingleEntryContainerBase {
    public static final MapCodec<OptionalLootItem> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("name").forGetter(e -> e.itemId)
    ).and(uniformFields(inst)).apply(inst, OptionalLootItem::new));

    private final Identifier itemId;
    @Nullable
    private final Item item;

    private OptionalLootItem(Identifier itemId, int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(zeroWeightIfItemUnregistered(itemId, weight), quality, condition, modifier);
        this.itemId = itemId;
        this.item = weight == 0 ? null : BuiltInRegistries.ITEM.getValue(itemId);
    }

    @Override
    public MapCodec<OptionalLootItem> codec() {
        return MAP_CODEC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> output, LootContext context) {
        if (item != null) {
            output.accept(new ItemStack(this.item));
        }
    }

    private static int zeroWeightIfItemUnregistered(Identifier itemId, int weight) {
        return BuiltInRegistries.ITEM.containsKey(itemId) ? weight : 0;
    }
}
