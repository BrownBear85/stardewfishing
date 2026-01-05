package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.Bobber;
import com.bonker.stardewfishing.common.items.LegendaryCatch;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SFComponentTypes {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, StardewFishing.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Bobber>> BOBBER =
            DATA_COMPONENT_TYPES.registerComponentType("bobber", builder ->
                    builder.persistent(Bobber.CODEC).networkSynchronized(Bobber.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LegendaryCatch>> LEGENDARY_CATCH =
            DATA_COMPONENT_TYPES.registerComponentType("legendary_catch", builder ->
                    builder.persistent(LegendaryCatch.CODEC).networkSynchronized(LegendaryCatch.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> POKEMON_SPECIES =
            DATA_COMPONENT_TYPES.registerComponentType("pokemon_species", builder ->
                    builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
}
