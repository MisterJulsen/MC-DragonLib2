package de.mrjulsen.mcdragonlib.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.mcdragonlib.util.DLColor;
import net.minecraft.client.renderer.RenderType;

import org.joml.Vector2f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PolygonRenderUtils {

    public static enum OutlineMode {
        INWARD, CENTER, OUTWARD;
    }

    public static void drawPolygon(DLGuiGraphics graphics, List<Vector2f> points, int z, DLColor fillColor, DLColor outlineColor, float outlineWidth) {
        fillPolygon(graphics, points, z, fillColor);
        drawPolygonOutline(graphics, points, outlineWidth, outlineColor);
    }

    public static void drawLine(DLGuiGraphics graphics, float x1, float y1, float x2, float y2, float width, DLColor color) {
        Vector2f p1 = new Vector2f(x1, y1);
        Vector2f p2 = new Vector2f(x2, y2);
        Vector2f dir = new Vector2f(p2).sub(p1).normalize();
        Vector2f normal = new Vector2f(-dir.y, dir.x).mul(width / 2f);

        List<Vector2f> quad = List.of(
                new Vector2f(p1).add(normal),
                new Vector2f(p2).add(normal),
                new Vector2f(p2).sub(normal),
                new Vector2f(p1).sub(normal)
        );

        fillPolygon(graphics, quad, 0, color);
    }

    public static void drawRectangle(DLGuiGraphics graphics, float x, float y, float width, float height, DLColor fillColor, DLColor outlineColor, float outlineWidth) {
        List<Vector2f> rect = List.of(
                new Vector2f(x, y),
                new Vector2f(x + width, y),
                new Vector2f(x + width, y + height),
                new Vector2f(x, y + height)
        );
        drawPolygon(graphics, rect, 0, fillColor, outlineColor, outlineWidth);
    }

    public static void drawTriangle(DLGuiGraphics graphics, float x1, float y1, float x2, float y2, float x3, float y3, DLColor fillColor, DLColor outlineColor, float outlineWidth) {
        List<Vector2f> tri = List.of(
                new Vector2f(x1, y1),
                new Vector2f(x2, y2),
                new Vector2f(x3, y3)
        );
        drawPolygon(graphics, tri, 0, fillColor, outlineColor, outlineWidth);
    }

    public static void drawCircle(DLGuiGraphics graphics, float cx, float cy, float radius, int segments, DLColor fillColor, DLColor outlineColor, float outlineWidth) {
        List<Vector2f> circle = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = cx + (float)Math.cos(angle) * radius;
            float y = cy + (float)Math.sin(angle) * radius;
            circle.add(new Vector2f(x, y));
        }
        drawPolygon(graphics, circle, 0, fillColor, outlineColor, outlineWidth);
    }

    public static void drawEllipse(DLGuiGraphics graphics, float cx, float cy, float rx, float ry, int segments, DLColor fillColor, DLColor outlineColor, float outlineWidth) {
        List<Vector2f> ellipse = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = cx + (float)Math.cos(angle) * rx;
            float y = cy + (float)Math.sin(angle) * ry;
            ellipse.add(new Vector2f(x, y));
        }
        drawPolygon(graphics, ellipse, 0, fillColor, outlineColor, outlineWidth);
    }

    public static void drawRegularPolygon(DLGuiGraphics graphics, float cx, float cy, float radius, int sides, DLColor fillColor, DLColor outlineColor, float outlineWidth) {
        List<Vector2f> poly = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            float x = cx + (float)Math.cos(angle) * radius;
            float y = cy + (float)Math.sin(angle) * radius;
            poly.add(new Vector2f(x, y));
        }
        drawPolygon(graphics, poly, 0, fillColor, outlineColor, outlineWidth);
    }


    public static void drawPolygonOutline(DLGuiGraphics graphics, List<Vector2f> points, float lineWidth, DLColor color) {
        if (points.size() < 2) return;

        Matrix4f matrix = graphics.poseStack().last().pose();
        VertexConsumer vertexConsumer = graphics.graphics().bufferSource().getBuffer(RenderType.gui());

        float alpha = color.getAlphaF();
        float red   = color.getRedF();
        float green = color.getGreenF();
        float blue  = color.getBlueF();

        int n = points.size();
        boolean ccw = polygonArea(points) > 0;

        Vector2f[] miters = new Vector2f[n];
        for (int i = 0; i < n; i++) {
            Vector2f pPrev = points.get((i - 1 + n) % n);
            Vector2f pCurr = points.get(i);
            Vector2f pNext = points.get((i + 1) % n);

            Vector2f dirPrev = new Vector2f(pCurr).sub(pPrev).normalize();
            Vector2f normalPrev = new Vector2f(ccw ? -dirPrev.y : dirPrev.y, ccw ? dirPrev.x : -dirPrev.x);

            Vector2f dirNext = new Vector2f(pNext).sub(pCurr).normalize();
            Vector2f normalNext = new Vector2f(ccw ? -dirNext.y : dirNext.y, ccw ? dirNext.x : -dirNext.x);

            Vector2f miter = new Vector2f(normalPrev).add(normalNext).normalize();

            float dot = miter.dot(normalNext);
            float miterLength = (dot != 0) ? (lineWidth / dot) : lineWidth;

            miters[i] = miter.mul(miterLength, new Vector2f());
        }

        for (int i = 0; i < n; i++) {
            Vector2f p0 = points.get(i);
            Vector2f p1 = points.get((i + 1) % n);

            Vector2f m0 = miters[i];
            Vector2f m1 = miters[(i + 1) % n];

            Vector2f v0 = new Vector2f(p0).add(m0);
            Vector2f v1 = new Vector2f(p1).add(m1);
            Vector2f v2 = new Vector2f(p1);
            Vector2f v3 = new Vector2f(p0);

            if (!isCCW(v0, v1, v2)) {
                vertexConsumer.addVertex(matrix, v0.x, v0.y, 0).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, v1.x, v1.y, 0).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, v2.x, v2.y, 0).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, v3.x, v3.y, 0).setColor(red, green, blue, alpha);
            } else {
                vertexConsumer.addVertex(matrix, v0.x, v0.y, 0).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, v3.x, v3.y, 0).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, v2.x, v2.y, 0).setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(matrix, v1.x, v1.y, 0).setColor(red, green, blue, alpha);
            }

        }

        graphics.graphics().flush();
    }

    private static boolean isCCW(Vector2f a, Vector2f b, Vector2f c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
    }

    public static void fillPolygon(DLGuiGraphics graphics, List<Vector2f> points, int z, DLColor color) {
        if (points.size() < 3)
            return;

        Matrix4f matrix = graphics.poseStack().last().pose();
        VertexConsumer vertexConsumer = graphics.graphics().bufferSource().getBuffer(RenderType.gui());

        float a = color.getAlphaF();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();

        List<Vector2f> poly = new ArrayList<>(points);

        if (polygonArea(poly) < 0) {
            Collections.reverse(poly);
        }

        List<int[]> triangles = earClipTriangulate(poly);

        for (int[] tri : triangles) {
            Vector2f v0 = poly.get(tri[0]);
            Vector2f v1 = poly.get(tri[1]);
            Vector2f v2 = poly.get(tri[2]);

            float cross = (v1.x - v0.x) * (v2.y - v0.y) - (v1.y - v0.y) * (v2.x - v0.x);

            if (cross < 0) {
                Vector2f tmp = v1;
                v1 = v2;
                v2 = tmp;
            }

            vertexConsumer.addVertex(matrix, v2.x, v2.y, (float) z).setColor(r, g, b, a);
            vertexConsumer.addVertex(matrix, v2.x, v2.y, (float) z).setColor(r, g, b, a);
            vertexConsumer.addVertex(matrix, v1.x, v1.y, (float) z).setColor(r, g, b, a);
            vertexConsumer.addVertex(matrix, v0.x, v0.y, (float) z).setColor(r, g, b, a);
        }

        graphics.graphics().flush();
    }

    private static float polygonArea(List<Vector2f> poly) {
        float sum = 0;
        for (int i = 0; i < poly.size(); i++) {
            Vector2f p1 = poly.get(i);
            Vector2f p2 = poly.get((i + 1) % poly.size());
            sum += (p1.x * p2.y - p2.x * p1.y);
        }
        return sum * 0.5f;
    }

    private static List<int[]> earClipTriangulate(List<Vector2f> poly) {
        List<int[]> result = new ArrayList<>();
        List<Integer> V = new ArrayList<>();
        for (int i = 0; i < poly.size(); i++)
            V.add(i);

        while (V.size() > 3) {
            boolean earFound = false;
            for (int i = 0; i < V.size(); i++) {
                int i0 = V.get((i + V.size() - 1) % V.size());
                int i1 = V.get(i);
                int i2 = V.get((i + 1) % V.size());

                Vector2f a = poly.get(i0);
                Vector2f b = poly.get(i1);
                Vector2f c = poly.get(i2);

                if (isConvex(a, b, c)) {
                    boolean contains = false;
                    for (int j = 0; j < V.size(); j++) {
                        int vi = V.get(j);
                        if (vi == i0 || vi == i1 || vi == i2)
                            continue;
                        if (pointInTriangle(poly.get(vi), a, b, c)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        result.add(new int[] { i0, i1, i2 });
                        V.remove(i);
                        earFound = true;
                        break;
                    }
                }
            }
            if (!earFound) {
                break;
            }
        }
        if (V.size() == 3) {
            result.add(new int[] { V.get(0), V.get(1), V.get(2) });
        }
        return result;
    }

    private static boolean isConvex(Vector2f a, Vector2f b, Vector2f c) {
        return ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) > 0;
    }

    private static boolean pointInTriangle(Vector2f p, Vector2f a, Vector2f b, Vector2f c) {
        float area = Math.abs(cross(a, b) + cross(b, c) + cross(c, a));
        float area1 = Math.abs(cross(p, a) + cross(a, b) + cross(b, p));
        float area2 = Math.abs(cross(p, b) + cross(b, c) + cross(c, p));
        float area3 = Math.abs(cross(p, c) + cross(c, a) + cross(a, p));
        return Math.abs(area - (area1 + area2 + area3)) < 1e-3;
    }

    private static float cross(Vector2f a, Vector2f b) {
        return a.x * b.y - a.y * b.x;
    }

}
