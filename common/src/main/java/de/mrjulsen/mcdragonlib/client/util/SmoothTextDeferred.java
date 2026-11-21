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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;

@ApiStatus.Internal
public final class SmoothTextDeferred {

    private static final DeferredSource SOURCE = new DeferredSource();
    private static Matrix4f modelViewMatrix;
    private static boolean renderingLevel = false;
    private static boolean deferralLogged = false;

    private SmoothTextDeferred() {}

    public static boolean shouldDefer(DLGraphics graphics) {
        if (!renderingLevel || !(graphics instanceof BERGraphics<?> berGraphics) || berGraphics.blockEntity() == null) {
            return false;
        }
        Level level = berGraphics.blockEntity().getLevel();
        boolean defer = level != null && level != Minecraft.getInstance().level;
        if (defer && !deferralLogged) {
            deferralLogged = true;
            DragonLib.LOGGER.info("Smooth text of block entities in {} is drawn late in the frame.", level.getClass().getName());
        }
        return defer;
    }

    public static MultiBufferSource source() {
        if (modelViewMatrix == null) {
            modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
        }
        return SOURCE;
    }

    public static void draw() {
        if (SOURCE.isEmpty()) {
            modelViewMatrix = null;
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        if (modelViewMatrix != null) {
            modelViewStack.set(modelViewMatrix);
        }
        RenderSystem.applyModelViewMatrix();
        try {
            SOURCE.draw();
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            modelViewMatrix = null;
        }
    }

    public static void beginLevelRendering() {
        SOURCE.discard();
        modelViewMatrix = null;
        renderingLevel = true;
    }

    public static void endLevelRendering() {
        renderingLevel = false;
        SOURCE.discard();
        modelViewMatrix = null;
    }

    private static final class DeferredSource implements MultiBufferSource {

        private final Map<RenderType, ByteBufferBuilder> buffers = new LinkedHashMap<>();
        private final Map<RenderType, BufferBuilder> started = new LinkedHashMap<>();

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return started.computeIfAbsent(type, t -> new BufferBuilder(buffers.computeIfAbsent(t, x -> new ByteBufferBuilder(x.bufferSize())), t.mode(), t.format()));
        }

        boolean isEmpty() {
            return started.isEmpty();
        }

        void draw() {
            for (Map.Entry<RenderType, BufferBuilder> entry : started.entrySet()) {
                RenderType type = entry.getKey();
                MeshData mesh = entry.getValue().build();
                if (mesh == null) {
                    continue;
                }
                if (type.sortOnUpload()) {
                    mesh.sortQuads(buffers.get(type), RenderSystem.getVertexSorting());
                }
                type.draw(mesh);
            }
            started.clear();
        }

        void discard() {
            for (BufferBuilder builder : started.values()) {
                MeshData mesh = builder.build();
                if (mesh != null) {
                    mesh.close();
                }
            }
            started.clear();
        }
    }
}
