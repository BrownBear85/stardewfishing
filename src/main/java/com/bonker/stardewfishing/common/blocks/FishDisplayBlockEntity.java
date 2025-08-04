package com.bonker.stardewfishing.common.blocks;

import com.bonker.stardewfishing.common.init.SFBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FishDisplayBlockEntity extends BlockEntity {
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
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);

        if (nbt.contains("displayed_item")) {
            item = ItemStack.parseOptional(registries, nbt.getCompound("displayed_item"));
        } else {
            item = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (!item.isEmpty()) {
            nbt.put("displayed_item", item.save(registries));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt, registries);
        return nbt;
    }
}
