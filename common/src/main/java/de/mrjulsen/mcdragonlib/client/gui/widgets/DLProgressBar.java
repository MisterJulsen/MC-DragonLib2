package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public class DLProgressBar implements Renderable {

    public static final int DEFAULT_HEIGHT = 10;

    private int x;
    private int y;
    private int width;
    private int height;

    private int backColor;
    private int borderColor;
    private int barColor;
    private int bufferBarColor;

    private double min;
    private double max;
    private double value;
    private double bufferValue;

    public DLProgressBar(int x, int y, int w, int h, double min, double max, double value) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;

        this.min = min;
        this.max = max;
        this.value = value;
    }

    public DLProgressBar(int x, int y, int w, double min, double max, double value) {
        this(x, y, w, DEFAULT_HEIGHT, min, max, value);
    }

    @Override
    public void render(GuiGraphics guigraphics, int mouseX, int mouseY, float partialTicks) {
        Graphics graphics = new Graphics(guigraphics, guigraphics.pose());
        GuiUtils.drawBox(graphics, new GuiAreaDefinition(x, y, width, height), backColor, borderColor);
        GuiUtils.fill(graphics, x + 2, y + 2, bufferBarWidth(width - 4), height - 4, bufferBarColor);
        GuiUtils.fill(graphics, x + 2, y + 2, barWidth(width - 4), height - 4, barColor);
    }

    private int barWidth(int w) {
        return (int)((double)w / max * value);
    }

    private int bufferBarWidth(int w) {
        return (int)((double)w / max * bufferValue);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBackColor() {
        return backColor;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBarColor() {
        return barColor;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getValue() {
        return value;
    }

    public int getBufferBarColor() {
        return bufferBarColor;
    }

    public double getBufferValue() {
        return bufferValue;
    }



    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }
    
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }
    
    public void setBufferBarColor(int bufferBarColor) {
        this.bufferBarColor = bufferBarColor;
    }

    public void setBufferValue(double bufferValue) {
        this.bufferValue = bufferValue;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
