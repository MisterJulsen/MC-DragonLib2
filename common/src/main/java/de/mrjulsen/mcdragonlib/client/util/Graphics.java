package de.mrjulsen.mcdragonlib.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;

public record Graphics(GuiGraphics graphics, PoseStack poseStack, Font defaultFont, float partialTick) {

    public BufferSource bufferSource() {
        return  graphics().bufferSource();
    }

    public VertexConsumer vertexConsumer(ResourceLocation textureLocation) {
        return graphics().bufferSource().getBuffer(RenderType.text(textureLocation));
    }

    public VertexConsumer vertexConsumer() {
        return graphics().bufferSource().getBuffer(RenderType.text(WorldRenderUtils.BLANK_TEXTURE_LOCATION));
    }
}
