package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.LegendaryCatch;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFComponentTypes {
    private static final Codec<Object> NO_SAVE = Codec.unit(new Object());

    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, StardewFishing.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> BOBBER =
            DATA_COMPONENT_TYPES.registerComponentType("bobber", builder ->
                    builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LegendaryCatch>> LEGENDARY_CATCH =
            DATA_COMPONENT_TYPES.registerComponentType("legendary_catch", builder ->
                    builder.persistent(LegendaryCatch.CODEC).networkSynchronized(LegendaryCatch.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Object>> POKEMON_TYPE =
            DATA_COMPONENT_TYPES.registerComponentType("pokemon_type", builder ->
                    builder.persistent(NO_SAVE));
}
