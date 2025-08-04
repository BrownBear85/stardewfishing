package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.common.init.SFLootPoolEntryTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class OptionalLootItem extends LootPoolSingletonContainer {
    public static final MapCodec<OptionalLootItem> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(o -> o.itemId)
    ).and(singletonFields(inst)).apply(inst, OptionalLootItem::new));

    private final ResourceLocation itemId;
    @Nullable
    private final Item item;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new OptionalEntryBase() {
        public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
            OptionalLootItem.this.createItemStack(LootItemFunction.decorate(OptionalLootItem.this.compositeFunction, stackConsumer, context), context);
        }
    };

    protected OptionalLootItem(ResourceLocation itemId, int pWeight, int pQuality, List<LootItemCondition> pConditions, List<LootItemFunction> pFunctions) {
        super(pWeight, pQuality, pConditions, pFunctions);
        this.itemId = itemId;
        Item item = BuiltInRegistries.ITEM.get(itemId);
        this.item = item == Items.AIR ? null : item;
        this.compositeFunction = LootItemFunctions.compose(pFunctions);
    }

    @Override
    public boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
        if (this.canRun(pLootContext)) {
            pEntryConsumer.accept(entry);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
        pStackConsumer.accept(item == null ? ItemStack.EMPTY : new ItemStack(item));
    }

    @Override
    public LootPoolEntryType getType() {
        return SFLootPoolEntryTypes.MOD_LOADED.get();
    }

    protected abstract class OptionalEntryBase extends LootPoolSingletonContainer.EntryBase {
        @Override
        public int getWeight(float luck) {
            return item == null ? 0 : super.getWeight(luck);
        }
    }
}
