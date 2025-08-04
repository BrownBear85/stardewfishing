package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.blocks.FishDisplayBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class SFBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StardewFishing.MODID);

    public static final DeferredBlock<FishDisplayBlock> FISH_DISPLAY = register("fish_display",
            FishDisplayBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN));

    private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties props) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, function, props);
        SFItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
