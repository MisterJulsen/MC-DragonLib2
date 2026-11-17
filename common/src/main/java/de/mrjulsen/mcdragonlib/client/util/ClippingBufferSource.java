package de.mrjulsen.mcdragonlib.client.util;

import java.util.function.UnaryOperator;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class ClippingBufferSource implements MultiBufferSource {

    private final ClippingVertexConsumer consumer = new ClippingVertexConsumer();
    private MultiBufferSource source;
    private UnaryOperator<RenderType> renderTypeMapper;

    public ClippingBufferSource setup(MultiBufferSource source, Matrix4f pose, Matrix3f normal, float minX, float minY, float maxX, float maxY) {
        this.source = source;
        this.consumer.setup(pose, normal, minX, minY, maxX, maxY);
        return this;
    }

    public ClippingBufferSource setRenderTypeMapper(UnaryOperator<RenderType> mapper) {
        this.renderTypeMapper = mapper;
        return this;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        RenderType type = renderTypeMapper == null ? renderType : renderTypeMapper.apply(renderType);
        return consumer.setDelegate(source.getBuffer(type));
    }
}
