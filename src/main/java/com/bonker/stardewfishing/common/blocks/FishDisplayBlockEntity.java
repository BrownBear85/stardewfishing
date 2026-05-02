package com.bonker.stardewfishing.common.blocks;

import com.bonker.stardewfishing.common.init.SFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("displayed_item")) {
            item = ItemStack.of(nbt.getCompound("displayed_item"));
        } else {
            item = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (!item.isEmpty()) {
            nbt.put("displayed_item", item.save(new CompoundTag()));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public void clearContent() {
        item = ItemStack.EMPTY;
    }
}
