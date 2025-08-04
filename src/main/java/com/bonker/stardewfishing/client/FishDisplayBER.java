package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.blocks.FishDisplayBlockEntity;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FishDisplayBER implements BlockEntityRenderer<FishDisplayBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FishDisplayBER(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FishDisplayBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemStack stack = pBlockEntity.getItem();
        if (stack.isEmpty() || pBlockEntity.getLevel() == null) {
            return;
        }

        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getLevel().getBlockState(pos);
        if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        boolean legendary = ItemUtils.isLegendaryFish(stack);

        pPoseStack.pushPose();

        double offset = -0.5 + 5/64.0;
        pPoseStack.translate(0.5 + facing.getStepX() * offset, 0.5, 0.5 + facing.getStepZ() * offset);

        float rot = switch (facing) {
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        if (rot != 0) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
        }

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            if (((BlockHitResult) hitResult).getBlockPos().equals(pos)) {
                MutableComponent component = Component.empty().append(stack.getHoverName());
                if (stack.has(DataComponents.CUSTOM_NAME)) {
                    component.withStyle(ChatFormatting.ITALIC);
                }
                if (legendary) {
                    component.withStyle(StardewFishing.LEGENDARY);
                }

                pPoseStack.pushPose();
                pPoseStack.translate(0, -0.25F, -0.25F);
                pPoseStack.scale(-0.025F, -0.025F, 0.025F);

                float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int bg = (int) (opacity * 255.0F) << 24;
                Font font = Minecraft.getInstance().font;
                font.drawInBatch(component, -font.width(component) / 2F, 0, -1, false, pPoseStack.last().pose(), pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight);
                font.drawInBatch(component, -font.width(component) / 2F, 0, 0x20FFFFFF, false, pPoseStack.last().pose(), pBuffer, Font.DisplayMode.SEE_THROUGH, bg, pPackedLight);
                pPoseStack.popPose();
            }
        }

        float scale;
        if (legendary) {
            scale = 1.19F;
        } else {
            scale = 0.8F;
        }
        pPoseStack.scale(scale, scale, scale);

        int sum = pos.getX() + pos.getY() + pos.getZ();
        if (sum % 2 == 0) {
            pPoseStack.translate(0, 0, -1/128F);
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);

        pPoseStack.popPose();
    }
}
