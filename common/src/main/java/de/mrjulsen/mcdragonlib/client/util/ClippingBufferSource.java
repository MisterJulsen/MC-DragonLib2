package de.mrjulsen.mcdragonlib.client.util;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * {@link MultiBufferSource} wrapper that routes every buffer through a {@link ClippingVertexConsumer}.
 * Pass it together with an identity matrix to e.g. {@code Font.drawInBatch} to get clipped text in world space.
 * <p>
 * Reusable: call {@link #setup} before each use. Not thread-safe.
 */
public class ClippingBufferSource implements MultiBufferSource {

    private final ClippingVertexConsumer consumer = new ClippingVertexConsumer();
    private MultiBufferSource source;

    public ClippingBufferSource setup(MultiBufferSource source, Matrix4f pose, Matrix3f normal, float minX, float minY, float maxX, float maxY) {
        this.source = source;
        this.consumer.setup(pose, normal, minX, minY, maxX, maxY);
        return this;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return consumer.setDelegate(source.getBuffer(renderType));
    }
}
