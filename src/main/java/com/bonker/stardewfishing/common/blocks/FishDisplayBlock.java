package com.bonker.stardewfishing.common.blocks;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFParticles;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class FishDisplayBlock extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    private static final MapCodec<FishDisplayBlock> CODEC = simpleCodec(FishDisplayBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_NORTH = box(0, 2, 0, 1, 14, 16);
    private static final VoxelShape SHAPE_SOUTH = box(15, 2, 0, 16, 14, 16);
    private static final VoxelShape SHAPE_WEST = box(0, 2, 15, 16, 14, 16);
    private static final VoxelShape SHAPE_EAST = box(0, 2, 0, 16, 14, 1);
    private static final Component TOOLTIP = Component.translatable("item.stardew_fishing.fish_display.tooltip")
            .withStyle(StardewFishing.LIGHTER_COLOR);

    public FishDisplayBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    public static boolean canBeDisplayed(ItemStack stack) {
        return stack.is(StardewFishing.IN_FISH_DISPLAY);
    }


    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
            ItemStack displayed = fishDisplay.getItem();

            if (displayed.isEmpty()) {
                if (canBeDisplayed(stack) && !displayed.is(stack.getItem())) {
                    fishDisplay.setItem(stack.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.swing(hand);
                    return ItemInteractionResult.SUCCESS;
                }
            } else {
                if (!player.getAbilities().instabuild) {
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, displayed));
                }
                fishDisplay.setItem(ItemStack.EMPTY);
                level.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.swing(hand);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
            ItemStack stack = fishDisplay.getItem();
            if (ItemUtils.isLegendaryFish(stack) && pLevel.random.nextFloat() < 0.25F) {
                Direction facing = pState.getValue(FACING);
                Vector3f spawnPos = new Vector3f(pPos.getX() + 0.5F - facing.getStepX() * 0.3F, pPos.getY(), pPos.getZ() + 0.5F - facing.getStepZ() * 0.3F);
                if (facing.getAxis() == Direction.Axis.X) {
                    spawnPos.z += pLevel.random.nextFloat() - 0.5F;
                } else {
                    spawnPos.x += pLevel.random.nextFloat() - 0.5F;
                }
                spawnPos.y += pLevel.random.nextFloat();
                pLevel.addParticle(SFParticles.SPARKLE.get(), spawnPos.x, spawnPos.y, spawnPos.z, 0, 0, 0);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(TOOLTIP);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
                Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), fishDisplay.getItem());
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case NORTH -> SHAPE_WEST;
            case SOUTH -> SHAPE_EAST;
            case WEST -> SHAPE_SOUTH;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction facing = pContext.getClickedFace();
        if (pContext.isSecondaryUseActive() || facing.getAxis() == Direction.Axis.Y) {
            facing = pContext.getHorizontalDirection().getOpposite();
        }
        Fluid fluid = pContext.getLevel().getFluidState(pContext.getClickedPos()).getType();
        return defaultBlockState().setValue(FACING, facing).setValue(WATERLOGGED, fluid == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WATERLOGGED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FishDisplayBlockEntity(pPos, pState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FishDisplayBlockEntity fishDisplay) {
            ItemStack stack = fishDisplay.getItem();
            if (!stack.isEmpty()) {
                return stack.copyWithCount(1);
            }
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }
}
