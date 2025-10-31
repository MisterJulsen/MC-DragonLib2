package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BERGraphics<B extends BlockEntity> extends DLGraphics { 
    
    protected final B blockEntity;
    protected final BlockEntityRendererProvider.Context berProviderContext;

    public BERGraphics(B blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, BlockEntityRendererProvider.Context berProviderContext, int packedLight, int packedOverlay, float partialTick) {
        super(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick);
        this.blockEntity = blockEntity;
        this.berProviderContext = berProviderContext;
    }
    
    public B blockEntity() {
        return blockEntity;
    }

    public BlockEntityRendererProvider.Context BERProviderContext() {
        return berProviderContext;
    }

    public VertexConsumer vertexConsumer(ResourceLocation textureLocation) {
        return multiBufferSource.getBuffer(RenderType.text(textureLocation));
    }
}
