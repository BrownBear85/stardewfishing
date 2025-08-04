package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.LegendaryCatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFComponentTypes {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, StardewFishing.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> BOBBER =
            DATA_COMPONENT_TYPES.registerComponentType("bobber", builder ->
                    builder.persistent(ItemStack.OPTIONAL_CODEC).networkSynchronized(ItemStack.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LegendaryCatch>> LEGENDARY_CATCH =
            DATA_COMPONENT_TYPES.registerComponentType("legendary_catch", builder ->
                    builder.persistent(LegendaryCatch.CODEC).networkSynchronized(LegendaryCatch.STREAM_CODEC));
}
