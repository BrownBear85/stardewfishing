package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.blocks.FishDisplayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StardewFishing.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishDisplayBlockEntity>> FISH_DISPLAY = BLOCK_ENTITY_TYPES.register("fish_display",
        () -> BlockEntityType.Builder.of(FishDisplayBlockEntity::new, SFBlocks.FISH_DISPLAY.get()).build(null));
}
