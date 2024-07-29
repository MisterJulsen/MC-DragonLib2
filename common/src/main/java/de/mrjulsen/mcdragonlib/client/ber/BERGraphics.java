package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public record BERGraphics(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) { 
    public VertexConsumer getVertexConsumer(ResourceLocation textureLocation) {
        return multiBufferSource.getBuffer(RenderType.text(textureLocation));
    }
}
