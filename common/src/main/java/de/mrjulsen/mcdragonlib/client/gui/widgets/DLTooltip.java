package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;

public class DLTooltip {
    public static final int WIDTH_UNDEFINED = -1;

    private final List<FormattedText> lines;
    protected int maxWidth = WIDTH_UNDEFINED;
    protected GuiAreaDefinition assignedArea = null;
    protected AbstractWidget assignedWidget = null;
    protected boolean visible = true;

    protected Supplier<Integer> dynamicOffsetX;
    protected Supplier<Integer> dynamicOffsetY;

    protected DLTooltip(List<FormattedText> lines) {
        this.lines = lines;
    }

    public static DLTooltip empty() {
        return DLTooltip.of((FormattedText)null);
    }

    public static DLTooltip of(String text) {
        return new DLTooltip(text == null ? null : List.of(TextUtils.text(text)));
    }

    public static DLTooltip of(Collection<String> text) {
        return new DLTooltip(text.stream().map(x -> (FormattedText)TextUtils.text(x)).toList());
    }

    public static DLTooltip of(String... texts) {
        return new DLTooltip(Arrays.stream(texts).map(x -> (FormattedText)TextUtils.text(x)).toList());
    }

    public static DLTooltip of(FormattedText formattedText) {
        return new DLTooltip(formattedText == null ? null : List.of(formattedText));
    }

    public static DLTooltip of(List<FormattedText> formattedTexts) {
        return new DLTooltip(formattedTexts);
    }

    public static DLTooltip of(FormattedText... formattedTexts) {
        return new DLTooltip(Arrays.stream(formattedTexts).toList());
    }

    public static <E extends Enum<E> & ITranslatableEnum> DLTooltip of(String modid, Class<E> enumClass) {
        return new DLTooltip(GuiUtils.getEnumTooltipData(modid, enumClass).stream().map(x -> (FormattedText)x).toList());
    }

    public DLTooltip withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public DLTooltip assignedTo(AbstractWidget widget) {
        assignedWidget = widget;
        return this;
    }

    public DLTooltip assignedTo(GuiAreaDefinition area) {
        assignedArea = area;
        return this;
    }

    public DLTooltip withVisibility(boolean b) {
        visible = b;
        return this;
    }


    public GuiAreaDefinition getAssignedArea() {
        return assignedArea;
    }

    public AbstractWidget getAssignedWidget() {
        return assignedWidget;
    }

    public List<FormattedText> getLines() {
        return lines;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setDynamicOffset(Supplier<Integer> offsetX, Supplier<Integer> offsetY) {
        this.dynamicOffsetX = offsetX;
        this.dynamicOffsetY = offsetY;
    }

    public Supplier<Integer> getDynamicOffsetX() {
        return dynamicOffsetX;
    }

    public Supplier<Integer> getDynamicOffsetY() {
        return dynamicOffsetY;
    }

    public void render(Screen screen, Graphics graphics, int mouseX, int mouseY) {
        render(screen, graphics, mouseX + 8, mouseY - 16, mouseX, mouseY, getDynamicOffsetX().get(), getDynamicOffsetY().get());
    }

    public void render(Screen screen, Graphics graphics, int x, int y, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (lines.size() <= 0) {
            return;
        }

        if (assignedWidget != null) {
            if ((assignedWidget instanceof IDragonLibWidget wgt && wgt.isMouseSelected()) || (assignedWidget.visible && assignedWidget.isMouseOver(mouseX, mouseY)))
            GuiUtils.renderTooltipAt(screen, GuiAreaDefinition.of(assignedWidget), getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, graphics, x, y, mouseX, mouseY, xOffset, yOffset);            
        } else if (assignedArea != null) {
            GuiUtils.renderTooltipAt(screen, assignedArea, getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, graphics, x, y, mouseX, mouseY, xOffset, yOffset);  
        } else {
            GuiUtils.renderTooltipAt(screen, GuiAreaDefinition.of(screen), getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, graphics, x, y, mouseX, mouseY, xOffset, yOffset); 
        }        
    }
}
