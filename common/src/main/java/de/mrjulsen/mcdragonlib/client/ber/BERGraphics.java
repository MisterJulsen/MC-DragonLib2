package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BERGraphics<B extends BlockEntity>(B blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, BlockEntityRendererProvider.Context BERProviderContext, int packedLight, int packedOverlay) { 
    public VertexConsumer vertexConsumer(ResourceLocation textureLocation) {
        return multiBufferSource.getBuffer(RenderType.text(textureLocation));
    }
}
