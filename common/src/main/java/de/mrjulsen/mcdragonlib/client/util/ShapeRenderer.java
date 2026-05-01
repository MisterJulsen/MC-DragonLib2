package de.mrjulsen.mcdragonlib.client.util;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor.ARGB32;

public class ShapeRenderer {
    private final GuiGraphics gui;

    public ShapeRenderer(GuiGraphics gfx) {
        this.gui = gfx;
    }

    /** 
     * Basisfunktion: Polygon zeichnen 
     * @param x      x-Koordinaten der Punkte
     * @param y      y-Koordinaten der Punkte
     * @param z      Tiefe
     * @param fillColor  Füllfarbe (ARGB), -1 wenn keine Füllung
     * @param lineColor  Linienfarbe (ARGB), -1 wenn keine Linie
     * @param lineWidth  Linienstärke (>=1)
     */
    public void drawPolygon(float[] x, float[] y, int z, int fillColor, int lineColor, float lineWidth) {
        Matrix4f matrix = gui.pose().last().pose();

        // --- Füllung ---
        if (fillColor != -1) {
            float a = (float)ARGB32.alpha(fillColor) / 255f;
            float r = (float)ARGB32.red(fillColor) / 255f;
            float g = (float)ARGB32.green(fillColor) / 255f;
            float b = (float)ARGB32.blue(fillColor) / 255f;

            VertexConsumer buf = gui.bufferSource().getBuffer(RenderType.gui());
            for (int i = 1; i < x.length - 1; i++) {
                buf.vertex(matrix, x[0], y[0], z).color(r, g, b, a).endVertex();
                buf.vertex(matrix, x[i], y[i], z).color(r, g, b, a).endVertex();
                buf.vertex(matrix, x[i + 1], y[i + 1], z).color(r, g, b, a).endVertex();
            }
            gui.flush();
        }

        // --- Linien ---
        if (lineColor != -1 && lineWidth > 0) {
            float a = (float)ARGB32.alpha(lineColor) / 255f;
            float r = (float)ARGB32.red(lineColor) / 255f;
            float g = (float)ARGB32.green(lineColor) / 255f;
            float b = (float)ARGB32.blue(lineColor) / 255f;

            VertexConsumer buf = gui.bufferSource().getBuffer(RenderType.lines());
            for (int i = 0; i < x.length; i++) {
                int j = (i + 1) % x.length;
                buf.vertex(matrix, x[i], y[i], z).color(r, g, b, a).normal(1, 0, 0).endVertex();
                buf.vertex(matrix, x[j], y[j], z).color(r, g, b, a).normal(1, 0, 0).endVertex();
            }
            gui.flush();
        }
    }

    // --- Hilfsfunktionen ---

    public void drawLine(float x1, float y1, float x2, float y2, int z, int color, float width) {
        drawPolygon(new float[]{x1, x2}, new float[]{y1, y2}, z, -1, color, width);
    }

    public void drawRectangle(float x, float y, float w, float h, int z, int fillColor, int lineColor, float lineWidth) {
        float[] px = {x, x + w, x + w, x};
        float[] py = {y, y, y + h, y + h};
        drawPolygon(px, py, z, fillColor, lineColor, lineWidth);
    }

    public void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, int z, int fillColor, int lineColor, float lineWidth) {
        float[] px = {x1, x2, x3};
        float[] py = {y1, y2, y3};
        drawPolygon(px, py, z, fillColor, lineColor, lineWidth);
    }

    public void drawCircle(float cx, float cy, float radius, int z, int fillColor, int lineColor, float lineWidth) {
        int segments = 40;
        float[] px = new float[segments];
        float[] py = new float[segments];
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            px[i] = cx + (float)Math.cos(angle) * radius;
            py[i] = cy + (float)Math.sin(angle) * radius;
        }
        drawPolygon(px, py, z, fillColor, lineColor, lineWidth);
    }
}
