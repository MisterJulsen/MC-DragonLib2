/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
        this.consumer.flush();
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
        // The pending quad must reach its buffer before the source may finish that buffer when the render type changes.
        consumer.flush();
        return consumer.setDelegate(source.getBuffer(type));
    }

    /**
     * Writes the last buffered quad to its buffer. Must be called after rendering into this source.
     */
    public void flush() {
        consumer.flush();
    }
}
