package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.blocks.FishDisplayBlockEntity;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class FishDisplayBER implements BlockEntityRenderer<@NotNull FishDisplayBlockEntity, FishDisplayBER.@NotNull FishDisplayRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FishDisplayBER(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class FishDisplayRenderState extends BlockEntityRenderState {
        public ItemStackRenderState displayItem;
        public ItemStack item;
        public Direction facing;
        public boolean renderLabel;
        public boolean offsetRender;
    }

    @Override
    public FishDisplayRenderState createRenderState() {
        return new FishDisplayRenderState();
    }

    @Override
    public void extractRenderState(@NotNull FishDisplayBlockEntity blockEntity, @NotNull FishDisplayRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);

        ItemStack stack = blockEntity.getItem();
        if (!stack.isEmpty() && blockEntity.getLevel() != null) {
            BlockPos pos = blockEntity.getBlockPos();
            BlockState state = blockEntity.getBlockState();
            if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                return;
            }

            renderState.facing = state.getValue(HorizontalDirectionalBlock.FACING);
            renderState.displayItem = new ItemStackRenderState();
            renderState.item = stack;
            HitResult hitResult = Minecraft.getInstance().hitResult;
            renderState.renderLabel = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && ((BlockHitResult) hitResult).getBlockPos().equals(pos);
            int sum = pos.getX() + pos.getY() + pos.getZ();
            renderState.offsetRender = sum % 2 == 0;

            itemModelResolver.updateForTopItem(renderState.displayItem, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(FishDisplayRenderState renderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector nodeCollector, @NotNull CameraRenderState cameraRenderState) {
        if (renderState.displayItem == null) {
            return;
        }

        ItemStack stack = renderState.item;
        if (stack.isEmpty() || renderState.facing == null) {
            return;
        }

        boolean legendary = ItemUtils.isLegendaryFish(stack);

        poseStack.pushPose();

        double offset = -0.5 + 5/64.0;
        poseStack.translate(0.5 + renderState.facing.getStepX() * offset, 0.5, 0.5 + renderState.facing.getStepZ() * offset);

        float rot = switch (renderState.facing) {
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        if (rot != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rot));
        }

        if (renderState.renderLabel) {
            MutableComponent component = Component.empty().append(stack.getHoverName());
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                component.withStyle(ChatFormatting.ITALIC);
            }
            if (legendary) {
                component.withStyle(StardewFishing.LEGENDARY);
            }

            poseStack.pushPose();
            poseStack.translate(0, -0.25F, -0.25F);
            poseStack.scale(-0.025F, 0.025F, 0.025F);
            poseStack.mulPose(Axis.XP.rotation(Mth.PI));

            nodeCollector.submitText(poseStack, -Minecraft.getInstance().font.width(component) / 2F, 0, component.getVisualOrderText(), true, Font.DisplayMode.NORMAL, renderState.lightCoords, 0xFFFFFFFF, 0x20FFFFFF, 0);

            poseStack.popPose();
        }

        float scale;
        if (legendary) {
            scale = 1.19F;
        } else {
            scale = 0.8F;
        }
        poseStack.scale(scale, scale, scale);

        if (renderState.offsetRender) {
            poseStack.translate(0, 0, -1/128F);
        }

        renderState.displayItem.submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
