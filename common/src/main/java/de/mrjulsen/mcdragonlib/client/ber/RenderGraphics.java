package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RenderGraphics { 

    protected final PoseStack poseStack;
    protected final MultiBufferSource multiBufferSource;
    protected final int packedLight;
    protected final int packedOverlay;

    public RenderGraphics(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.packedOverlay = packedOverlay;
    }

    public PoseStack poseStack() {
        return poseStack;
    }

    public MultiBufferSource multiBufferSource() {
        return multiBufferSource;
    }

    public int packedLight() {
        return packedLight;
    }

    public int packedOverlay() {
        return packedOverlay;
    }

    public VertexConsumer vertexConsumer(ResourceLocation textureLocation) {
        return multiBufferSource.getBuffer(RenderType.text(textureLocation));
    }
}
