package com.bonker.stardewfishing.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class RenderUtil {
    private static final int defaultColor = 0xFFFFFFFF;
    private static int color = defaultColor;

    public static void blitF(GuiGraphicsExtractor guiGraphics, RenderPipeline pipeline, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x - (int) x, y - (int) y);
        guiGraphics.blit(pipeline, texture, (int) x, (int) y, uOffset, vOffset, uWidth, vHeight, 256, 256, color);
        guiGraphics.pose().popMatrix();
    }

    public static void fillF(GuiGraphicsExtractor guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
        if (minX < maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            float j = minY;
            minY = maxY;
            maxY = j;
        }

        guiGraphics.submitGuiElementRenderState(new ColoredRectangleFRenderState(RenderPipelines.GUI, TextureSetup.noTexture(),
                        new Matrix3x2f(guiGraphics.pose()), minX, minY, maxX, maxY, color, guiGraphics.peekScissorStack()));
    }

    public static void drawRotatedAround(Matrix3x2fStack poseStack, float radians, float pivotX, float pivotY, Runnable runnable) {
        poseStack.pushMatrix();
        poseStack.rotateAbout(radians, pivotX, pivotY);
        runnable.run();
        poseStack.popMatrix();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        color = ARGB.white(alpha);
        runnable.run();
        color = defaultColor;
    }

    public static void drawWithBlend(Runnable runnable) {
//        RenderSystem.enableBlend();
        runnable.run();
//        RenderSystem.disableBlend();
    }

    public static void drawWithShake(Matrix3x2fStack poseStack, Shake shake, float partialTick, boolean doShake, Runnable runnable) {
        if (doShake) {
            poseStack.pushMatrix();
            poseStack.translate(shake.getXOffset(partialTick), shake.getYOffset(partialTick));
        }

        runnable.run();

        if (doShake) {
            poseStack.popMatrix();
        }
    }

    private static IntIterator slices(int pTarget, int pTotal) {
        int i = Mth.positiveCeilDiv(pTarget, pTotal);
        return new Divisor(pTarget, i);
    }

    public static void blitRepeatingF(GuiGraphicsExtractor guiGraphics, RenderPipeline pipeline, Identifier texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight, int sourceWidth, int sourceHeight) {
        int width;
        for (IntIterator intiterator = slices(uWidth, sourceWidth); intiterator.hasNext(); x += width) {
            width = intiterator.nextInt();
            int du = (sourceWidth - width) / 2;

            int height;
            for(IntIterator iterator = slices(vHeight, sourceHeight); iterator.hasNext(); y += height) {
                height = iterator.nextInt();
                int dv = (sourceHeight - height) / 2;
                blitF(guiGraphics, pipeline, texture, x, y, uOffset + du, vOffset + dv, width, height);
            }
        }
    }

    public static void renderItemF(GuiGraphicsExtractor guiGraphics, ItemStack item, float x, float y) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x - (int) x, y - (int) y);
        guiGraphics.item(item, (int) x, (int) y);
        guiGraphics.itemDecorations(Minecraft.getInstance().font, item, (int) x, (int) y);
        guiGraphics.pose().popMatrix();
    }

    public record ColoredRectangleFRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int col1,
            int col2,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        public ColoredRectangleFRenderState(
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                Matrix3x2fc pose,
                float x0,
                float y0,
                float x1,
                float y1,
                int color,
                @Nullable ScreenRectangle scissorArea
        ) {
            this(
                    pipeline,
                    textureSetup,
                    pose,
                    x0,
                    y0,
                    x1,
                    y1,
                    color,
                    color,
                    scissorArea,
                    getBounds(Mth.floor(x0), Mth.floor(y0), Mth.ceil(x1), Mth.ceil(y1), pose, scissorArea)
            );
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0()).setColor(this.col1());
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1()).setColor(this.col2());
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1()).setColor(this.col2());
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0()).setColor(this.col1());
        }

        private static @Nullable ScreenRectangle getBounds(
                int x0, int y0, int x1, int y1, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea
        ) {
            ScreenRectangle screenrectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
            return scissorArea != null ? scissorArea.intersection(screenrectangle) : screenrectangle;
        }
    }
}
