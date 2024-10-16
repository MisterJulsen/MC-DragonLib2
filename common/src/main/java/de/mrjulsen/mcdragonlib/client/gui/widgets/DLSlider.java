package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Slider widget implementation which allows inputting values in a certain range with optional step size.
 */
public class DLSlider extends AbstractSliderButton implements IDragonLibWidget {
    protected Component prefix;
    protected Component suffix;

    protected double minValue;
    protected double maxValue;

    /** Allows input of discontinuous values with a certain step */
    protected double stepSize;
    protected boolean drawString;

    protected AreaStyle style = AreaStyle.NATIVE;
    protected int fontColor = 0xFFFFFFFF;
    protected int backColor = 0xFFFFFFFF;
    protected boolean textShadow = true;
    protected EAlignment textAlignment;

    private final DecimalFormat format;
    private final Consumer<DLSlider> onUpdateMessage;

    private DLContextMenu menu;
    private boolean mouseSelected;

    /**
     * @param x x position of upper left corner
     * @param y y position of upper left corner
     * @param width Width of the widget
     * @param height Height of the widget
     * @param prefix {@link Component} displayed before the value string
     * @param suffix {@link Component} displayed after the value string
     * @param minValue Minimum (left) value of slider
     * @param maxValue Maximum (right) value of slider
     * @param currentValue Starting value when widget is first displayed
     * @param stepSize Size of step used. Precision will automatically be calculated based on this value if this value is not 0.
     * @param precision Only used when {@code stepSize} is 0. Limited to a maximum of 4 (inclusive).
     * @param drawString Should text be displayed on the widget
     */
    public DLSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, Consumer<DLSlider> onUpdateMessage) {
        super(x, y, width, height, TextUtils.empty(), 0D);
        this.onUpdateMessage = onUpdateMessage;
        this.prefix = prefix;
        this.suffix = suffix;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = Math.abs(stepSize);
        this.value = this.snapToNearest((currentValue - minValue) / (maxValue - minValue));
        this.drawString = drawString;

        if (stepSize == 0D) {
            precision = Math.min(precision, 4);
            StringBuilder builder = new StringBuilder("0");

            if (precision > 0) {
                builder.append('.');
            }

            while (precision-- > 0) {                
                builder.append('0');
            }

            this.format = new DecimalFormat(builder.toString());
        } else if (Mth.equal(this.stepSize, Math.floor(this.stepSize))) {
            this.format = new DecimalFormat("0");
        } else {
            this.format = new DecimalFormat(Double.toString(this.stepSize).replaceAll("\\d", "0"));
        }

        this.updateMessage();
    }

    public boolean isRenderingTextShadow() {
        return textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public EAlignment getTextAlignment() {
        return textAlignment;
    }

    public void setTextAlignment(EAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    public void setRenderStyle(AreaStyle style) {
        this.style = style;
        if (style.isCustom()) {
            setBackColor(DragonLib.DEFAULT_BUTTON_COLOR);
        }
    }

    public AreaStyle getStyle() {
        return style;
    }

    /**
     * Overload with {@code stepSize} set to 1, useful for sliders with whole number values.
     */
    public DLSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, Consumer<DLSlider> onUpdateMessage) {
        this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1D, 0, drawString, onUpdateMessage);
    }

    /**
     * @return Current slider value as a double
     */
    public double getValue() {
        return this.value * (maxValue - minValue) + minValue;
    }

    /**
     * @return Current slider value as an long
     */
    public long getValueLong() {
        return Math.round(this.getValue());
    }

    /**
     * @return Current slider value as an int
     */
    public int getValueInt() {
        return (int)this.getValueLong();
    }

    /**
     * @param value The new slider value
     */
    public void setValue(double value) {
        this.value = this.snapToNearest((value - this.minValue) / (this.maxValue - this.minValue));
        this.updateMessage();
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        this.setValueFromMouse(mouseX);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean flag = keyCode == GLFW.GLFW_KEY_LEFT;
        if (flag || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.minValue > this.maxValue) {
                flag = !flag;
            }
            float f = flag ? -1F : 1F;
            if (stepSize <= 0D) {
                this.setSliderValue(this.value + (f / (this.width - 8)));
            } else {
                this.setValue(this.getValue() + f * this.stepSize);
            }
        }

        return false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderMainLayer(new Graphics(poseStack), mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("resource")
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        DynamicGuiRenderer.renderArea(graphics, x(), y(), width(), height(), getBackColor(), style, ButtonState.DISABLED);
        DynamicGuiRenderer.renderArea(graphics, new GuiAreaDefinition(this.x() + (int)(this.value * (double)(this.getWidth() - 8)), this.y(), 8, getHeight()), getBackColor(), style, isActive() ? (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON) : ButtonState.DISABLED);
        
        int j = active() ? getFontColor() : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED;
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, this.x() + this.width() / 2, this.y() + (this.height() - 8) / 2, this.getMessage(), j, getTextAlignment(), isRenderingTextShadow());
        GuiUtils.resetTint();

    }

    @Override
    protected final void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {}

    private void setValueFromMouse(double mouseX) {
        this.setSliderValue((mouseX - (this.x + 4)) / (this.width - 8));
    }

    /**
     * @param value Percentage of slider range
     */
    private void setSliderValue(double value) {
        double oldValue = this.value;
        this.value = this.snapToNearest(value);
        if (!Mth.equal(oldValue, this.value)) {            
            this.applyValue();
        }

        this.updateMessage();
    }

    /**
     * Snaps the value, so that the displayed value is the nearest multiple of {@code stepSize}.
     * If {@code stepSize} is 0, no snapping occurs.
     */
    private double snapToNearest(double value) {
        if(stepSize <= 0D) {            
            return Mth.clamp(value, 0D, 1D);
        }

        value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);
        value = (stepSize * Math.round(value / stepSize));

        if (this.minValue > this.maxValue) {
            value = Mth.clamp(value, this.maxValue, this.minValue);
        } else {
            value = Mth.clamp(value, this.minValue, this.maxValue);
        }

        return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
    }

    @Override
    protected void updateMessage() {

        if (onUpdateMessage == null) {
            if (this.drawString) {
                this.setMessage(TextUtils.empty().append(prefix).append(this.getValueString()).append(suffix));
            } else {
                this.setMessage(TextUtils.empty());
            }
            return;
        }
        
        onUpdateMessage.accept(this);
    }

    @Override
    protected void applyValue() {}

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (menu != null) {
            this.menu.render(graphics.poseStack(), mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public DLContextMenu getContextMenu() {
        return this.menu;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
    }

    @Override
    public void onFocusChangeEvent(boolean focus) {
        setFocused(focus);
    }

    @Override
    public boolean isMouseSelected() {
        return mouseSelected;
    }

    @Override
    public void setMouseSelected(boolean selected) {
        this.mouseSelected = selected;
    }

    public int getBackColor() {
        return backColor;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setBackColor(int color) {
        this.backColor = color;
    }

    public void setFontColor(int color) {
        this.fontColor = color;        
    }
    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }    

    @Override
    public void set_x(int x) {
        this.x = x;
    }

    @Override
    public void set_y(int y) {
        this.y = y;
    }

    @Override
    public void set_width(int w) {
        this.width = w;
    }

    @Override
    public void set_height(int h) {
        this.height = h;
    }

    @Override
    public void set_visible(boolean b) {
        this.visible = b;
    }

    @Override
    public boolean visible() {
        return visible;
    }

    @Override
    public void set_active(boolean b) {
        this.active = b;
    }

    @Override
    public boolean active() {
        return super.isActive();
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }
}
