package com.bonker.stardewfishing.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class RenderUtil {
    public static void blitF(GuiGraphics guiGraphics, ResourceLocation texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight) {
        float maxX = x + uWidth;
        float maxY = y + vHeight;
        float minU = uOffset / 256F;
        float minV = vOffset / 256F;
        float maxU = (uOffset + uWidth) / 256F;
        float maxV = (vOffset + vHeight) / 256F;
        
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, x, y, 0).setUv(minU, minV);
        bufferbuilder.addVertex(matrix4f, x, maxY, 0).setUv(minU, maxV);
        bufferbuilder.addVertex(matrix4f, maxX, maxY, 0).setUv(maxU, maxV);
        bufferbuilder.addVertex(matrix4f, maxX, y, 0).setUv(maxU, minV);
        try (MeshData mesh = bufferbuilder.build()) {
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        }
    }

    public static void fillF(GuiGraphics guiGraphics, float pMinX, float pMinY, float pMaxX, float pMaxY, float pZ, int pColor) {
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        if (pMinX < pMaxX) {
            float temp = pMinX;
            pMinX = pMaxX;
            pMaxX = temp;
        }

        if (pMinY < pMaxY) {
            float temp = pMinY;
            pMinY = pMaxY;
            pMaxY = temp;
        }

        float alpha =  FastColor.ARGB32.alpha(pColor) / 255.0F;
        float red = FastColor.ARGB32.red(pColor) / 255.0F;
        float green = FastColor.ARGB32.green(pColor) / 255.0F;
        float blue = FastColor.ARGB32.blue(pColor) / 255.0F;
        VertexConsumer vertexconsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        vertexconsumer.addVertex(matrix4f, pMinX, pMinY, pZ).setColor(red, green, blue, alpha);
        vertexconsumer.addVertex(matrix4f, pMinX, pMaxY, pZ).setColor(red, green, blue, alpha);
        vertexconsumer.addVertex(matrix4f, pMaxX, pMaxY, pZ).setColor(red, green, blue, alpha);
        vertexconsumer.addVertex(matrix4f, pMaxX, pMinY, pZ).setColor(red, green, blue, alpha);
        guiGraphics.flush();
    }

    public static void drawRotatedAround(PoseStack poseStack, float radians, float pivotX, float pivotY, Runnable runnable) {
        poseStack.pushPose();
        poseStack.rotateAround(Axis.ZN.rotation(radians), pivotX, pivotY, 0);
        runnable.run();
        poseStack.popPose();
    }

    public static void drawWithAlpha(float alpha, Runnable runnable) {
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        runnable.run();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawWithBlend(Runnable runnable) {
        RenderSystem.enableBlend();
        runnable.run();
        RenderSystem.disableBlend();
    }

    public static void drawWithShake(PoseStack poseStack, Shake shake, float partialTick, boolean doShake, Runnable runnable) {
        if (doShake) {
            poseStack.pushPose();
            poseStack.translate(shake.getXOffset(partialTick), shake.getYOffset(partialTick), 0);
        }

        runnable.run();

        if (doShake) {
            poseStack.popPose();
        }
    }

    private static IntIterator slices(int pTarget, int pTotal) {
        int i = Mth.positiveCeilDiv(pTarget, pTotal);
        return new Divisor(pTarget, i);
    }

    public static void blitRepeatingF(GuiGraphics guiGraphics, ResourceLocation texture, float x, float y, int uOffset, int vOffset, int uWidth, int vHeight, int sourceWidth, int sourceHeight) {
        int width;
        for (IntIterator intiterator = slices(uWidth, sourceWidth); intiterator.hasNext(); x += width) {
            width = intiterator.nextInt();
            int du = (sourceWidth - width) / 2;

            int height;
            for(IntIterator iterator = slices(vHeight, sourceHeight); iterator.hasNext(); y += height) {
                height = iterator.nextInt();
                int dv = (sourceHeight - height) / 2;
                blitF(guiGraphics, texture, x, y, uOffset + du, vOffset + dv, width, height);
            }
        }
    }

    public static void renderItemF(GuiGraphics guiGraphics, ItemStack item, float x, float y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x - (int) x, y - (int) y, 0);
        guiGraphics.renderItem(item, (int) x, (int) y);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, item, (int) x, (int) y);
        guiGraphics.pose().popPose();
    }
}
