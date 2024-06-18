package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;

public class DLCheckBox extends DLButton {

    public static final int DEFAULT_CHECKBOX_HEIGHT = 12;
    private final int maxLineWidth = width - DEFAULT_CHECKBOX_HEIGHT - 12;

    protected boolean checked;

    protected final GuiAreaDefinition boxArea;
    protected final Consumer<DLCheckBox> onCheckedChanged;

    public DLCheckBox(int pX, int pY, int pWidth, String pMessage, boolean checked, Consumer<DLCheckBox> onCheckedChanged) {
        super(pX, pY, pWidth, DEFAULT_CHECKBOX_HEIGHT + 4, TextUtils.text(pMessage), (b) -> ((DLCheckBox)b).toggleChecked());
        boxArea = new GuiAreaDefinition(getX(), getY() + (DEFAULT_CHECKBOX_HEIGHT + 4) / 2 - DEFAULT_CHECKBOX_HEIGHT / 2, DEFAULT_CHECKBOX_HEIGHT, DEFAULT_CHECKBOX_HEIGHT);
        setChecked(checked);
        this.onCheckedChanged = onCheckedChanged;
    }

    public boolean setChecked(boolean b) {
        this.checked = b;
        if (onCheckedChanged != null) {
            onCheckedChanged.accept(this);
        }
        return checked;
    }

    public boolean toggleChecked() {
        return setChecked(!checked);
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderArea(graphics, boxArea, getBackColor(), getStyle(), ButtonState.DOWN);
        GuiUtils.resetTint();
        if (isMouseSelected()) {
            GuiUtils.drawBox(graphics, boxArea, 0, 0xFFFFFFFF);
        }
        if (isChecked()) {
            GuiUtils.setTint(active ? DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
            GuiIcons.X.render(graphics, getX() + DEFAULT_CHECKBOX_HEIGHT / 2 - GuiIcons.ICON_SIZE / 2, getY() + getHeight() / 2 - GuiIcons.ICON_SIZE / 2);
        }
        GuiUtils.setTint(1.0F, 1.0F, 1.0F, this.alpha);

        final boolean tooWide = font.width(getMessage()) > maxLineWidth;

        int j = active ? getFontColor() : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED;
        GuiUtils.drawString(graphics, font, getX() + DEFAULT_CHECKBOX_HEIGHT + 4, getY() + getHeight() / 2 - font.lineHeight / 2, tooWide ? TextUtils.text(font.substrByWidth(getMessage(), maxLineWidth).getString() + "...") : getMessage(), j, EAlignment.LEFT, true);
    }

    @SuppressWarnings("resource")
    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        final boolean tooWide = font.width(getMessage()) > maxLineWidth;
        if (tooWide && (isMouseSelected())) {
            GuiUtils.renderTooltipAt(Minecraft.getInstance().screen, GuiAreaDefinition.of(this), List.of(getMessage()), 255, graphics, getX() + DEFAULT_CHECKBOX_HEIGHT, getY() + getHeight() / 2 - font.lineHeight / 2 - 4, mouseX, mouseY, 0, 0);
        }

        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
    }
    
}
