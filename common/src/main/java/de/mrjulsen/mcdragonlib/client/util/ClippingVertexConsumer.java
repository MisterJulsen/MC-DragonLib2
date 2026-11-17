/*
 * DragonLib 3 - 1.20.1
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

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A {@link VertexConsumer} wrapper that clips quads against an axis-aligned rectangle in its local (untransformed) XY space.
 * <p>
 * Vertices are expected in local coordinates (e.g. render text with an identity matrix). Each quad is buffered, tested against the
 * clip rectangle and, if it intersects the border, clipped with interpolated position, color and UV. The result is then transformed
 * with the configured pose and forwarded to the delegate. Overlay, light and normal are taken from the first vertex of the quad.
 * <p>
 * This is a replacement for scissor-tests in world space (e.g. block entity renderers), which do not exist in vanilla.
 * Only works with render types using {@code VertexFormat.Mode.QUADS}. Not thread-safe, reuse one instance per render call site.
 */
public class ClippingVertexConsumer implements VertexConsumer {

    private static final int X = 0, Y = 1, Z = 2, R = 3, G = 4, B = 5, A = 6, U = 7, V = 8, STRIDE = 9;
    private static final int MAX_POLY = 8; // A convex quad clipped by 4 planes has at most 8 vertices.

    private VertexConsumer delegate;
    private final Matrix4f pose = new Matrix4f();
    private final Matrix3f normalMatrix = new Matrix3f();
    private float minX, minY, maxX, maxY;

    private final float[] polyA = new float[MAX_POLY * STRIDE];
    private final float[] polyB = new float[MAX_POLY * STRIDE];
    private final Vector3f tmp = new Vector3f();
    private int vertexIndex;

    private boolean hasColor, hasUv, hasOverlay, hasLight, hasNormal;
    private int overlayU, overlayV, lightU, lightV;
    private float normalX, normalY, normalZ;

    /**
     * @param pose Transformation applied to the vertices after clipping.
     * @param normal Normal matrix applied to normals after clipping.
     * @param minX Left border of the clip rectangle in local space.
     * @param minY Top border of the clip rectangle in local space.
     * @param maxX Right border of the clip rectangle in local space.
     * @param maxY Bottom border of the clip rectangle in local space.
     */
    public ClippingVertexConsumer setup(Matrix4f pose, Matrix3f normal, float minX, float minY, float maxX, float maxY) {
        this.pose.set(pose);
        this.normalMatrix.set(normal);
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.vertexIndex = 0;
        return this;
    }

    public ClippingVertexConsumer setDelegate(VertexConsumer delegate) {
        if (this.delegate != delegate) {
            this.delegate = delegate;
            this.vertexIndex = 0;
        }
        return this;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        if (vertexIndex == 0) {
            hasColor = hasUv = hasOverlay = hasLight = hasNormal = false;
        }
        int o = vertexIndex * STRIDE;
        polyA[o + X] = (float) x;
        polyA[o + Y] = (float) y;
        polyA[o + Z] = (float) z;
        return this;
    }

    @Override
    public VertexConsumer vertex(Matrix4f m, float x, float y, float z) {
        return vertex(
            m.m00() * x + m.m10() * y + m.m20() * z + m.m30(),
            m.m01() * x + m.m11() * y + m.m21() * z + m.m31(),
            m.m02() * x + m.m12() * y + m.m22() * z + m.m32()
        );
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        int o = vertexIndex * STRIDE;
        polyA[o + R] = r;
        polyA[o + G] = g;
        polyA[o + B] = b;
        polyA[o + A] = a;
        hasColor = true;
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        int o = vertexIndex * STRIDE;
        polyA[o + U] = u;
        polyA[o + V] = v;
        hasUv = true;
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        if (vertexIndex == 0) {
            overlayU = u;
            overlayV = v;
        }
        hasOverlay = true;
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        if (vertexIndex == 0) {
            lightU = u;
            lightV = v;
        }
        hasLight = true;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        if (vertexIndex == 0) {
            normalX = x;
            normalY = y;
            normalZ = z;
        }
        hasNormal = true;
        return this;
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }

    @Override
    public void endVertex() {
        if (++vertexIndex < 4) {
            return;
        }
        vertexIndex = 0;
        processQuad();
    }


    private void processQuad() {
        float qMinX = Float.MAX_VALUE, qMinY = Float.MAX_VALUE, qMaxX = -Float.MAX_VALUE, qMaxY = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int o = i * STRIDE;
            qMinX = Math.min(qMinX, polyA[o + X]);
            qMaxX = Math.max(qMaxX, polyA[o + X]);
            qMinY = Math.min(qMinY, polyA[o + Y]);
            qMaxY = Math.max(qMaxY, polyA[o + Y]);
        }

        if (qMaxX <= minX || qMinX >= maxX || qMaxY <= minY || qMinY >= maxY) {
            return;
        }

        if (qMinX >= minX && qMaxX <= maxX && qMinY >= minY && qMaxY <= maxY) {
            emitPolygon(polyA, 4);
            return;
        }

        int n = 4;
        n = clipEdge(polyA, n, polyB, X, minX, true);
        n = clipEdge(polyB, n, polyA, X, maxX, false);
        n = clipEdge(polyA, n, polyB, Y, minY, true);
        n = clipEdge(polyB, n, polyA, Y, maxY, false);
        if (n >= 3) {
            emitPolygon(polyA, n);
        }
    }

    private static int clipEdge(float[] src, int n, float[] dst, int axis, float bound, boolean keepGreater) {
        if (n == 0) {
            return 0;
        }
        int out = 0;
        int prev = n - 1;
        float prevVal = src[prev * STRIDE + axis];
        boolean prevIn = keepGreater ? prevVal >= bound : prevVal <= bound;

        for (int cur = 0; cur < n; cur++) {
            float curVal = src[cur * STRIDE + axis];
            boolean curIn = keepGreater ? curVal >= bound : curVal <= bound;

            if (curIn != prevIn) {
                float t = (bound - prevVal) / (curVal - prevVal);
                int po = prev * STRIDE, co = cur * STRIDE, oo = out * STRIDE;
                for (int k = 0; k < STRIDE; k++) {
                    dst[oo + k] = src[po + k] + (src[co + k] - src[po + k]) * t;
                }
                dst[oo + axis] = bound;
                out++;
            }
            if (curIn) {
                System.arraycopy(src, cur * STRIDE, dst, out * STRIDE, STRIDE);
                out++;
            }
            prev = cur;
            prevVal = curVal;
            prevIn = curIn;
        }
        return out;
    }

    private void emitPolygon(float[] poly, int n) {
        for (int i = 1; i < n - 1; i += 2) {
            emitVertex(poly, 0);
            emitVertex(poly, i);
            emitVertex(poly, i + 1);
            emitVertex(poly, Math.min(i + 2, n - 1));
        }
    }

    private void emitVertex(float[] poly, int index) {
        int o = index * STRIDE;
        pose.transformPosition(poly[o + X], poly[o + Y], poly[o + Z], tmp);
        delegate.vertex(tmp.x(), tmp.y(), tmp.z());
        if (hasColor) {
            delegate.color(Math.round(poly[o + R]), Math.round(poly[o + G]), Math.round(poly[o + B]), Math.round(poly[o + A]));
        }
        if (hasUv) {
            delegate.uv(poly[o + U], poly[o + V]);
        }
        if (hasOverlay) {
            delegate.overlayCoords(overlayU, overlayV);
        }
        if (hasLight) {
            delegate.uv2(lightU, lightV);
        }
        if (hasNormal) {
            normalMatrix.transform(normalX, normalY, normalZ, tmp);
            tmp.normalize();
            delegate.normal(tmp.x(), tmp.y(), tmp.z());
        }
        delegate.endVertex();
    }
}
