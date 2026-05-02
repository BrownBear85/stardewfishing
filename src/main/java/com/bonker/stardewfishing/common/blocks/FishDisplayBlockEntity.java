package com.bonker.stardewfishing.common.blocks;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FishDisplayBlockEntity extends BlockEntity implements Clearable {
    private ItemStack item = ItemStack.EMPTY;

    public FishDisplayBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(SFBlockEntities.FISH_DISPLAY.get(), pPos, pBlockState);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        input.read("displayed_item", ItemStack.CODEC).ifPresentOrElse(stack -> item = stack, () -> item = ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (!item.isEmpty()) {
            output.store("displayed_item", ItemStack.CODEC, item);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector problemReporter = new ProblemReporter.ScopedCollector(problemPath(), StardewFishing.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(problemReporter, registries);
            saveAdditional(output);
            return output.buildResult();
        }
    }

    @Override
    public void clearContent() {
        item = ItemStack.EMPTY;
    }
}
