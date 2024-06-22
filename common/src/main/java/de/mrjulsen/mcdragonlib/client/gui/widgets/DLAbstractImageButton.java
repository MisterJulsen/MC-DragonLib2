package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.network.chat.Component;

public abstract class DLAbstractImageButton<T extends DLAbstractImageButton<T>> extends DLButton {

    private boolean selected = false;
    private final WidgetsCollection collection;
    private ButtonType type;
    private AreaStyle style;
    private EAlignment alignment = EAlignment.CENTER;

    public DLAbstractImageButton(ButtonType type, AreaStyle style, int pX, int pY, int w, int h, Component pMessage, Consumer<T> onClick) {
        this(type, style, null, pX, pY, w, h, pMessage, onClick);
    }

    public DLAbstractImageButton(ButtonType type, AreaStyle style, WidgetsCollection collection, int pX, int pY, int w, int h, Component pMessage, Consumer<T> onClick) {
        super(pX, pY, w, h, pMessage, onClick);
        withButtonType(type);
        withStyle(style);

        if (collection != null) {
            collection.components.add(this);
        }
        this.collection = collection;
    }

    public ButtonType getButtonType() {
        return type;
    }

    public EAlignment getAlignment() {
        return alignment;
    }

    public AreaStyle getStyle() {
        return style;
    }

    @SuppressWarnings("unchecked")
    public T withAlignment(EAlignment alignment) {
        this.alignment = alignment;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T withStyle(AreaStyle style) {
        this.style = style;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T withButtonType(ButtonType type) {
        this.type = type;
        return (T)this;
    }

    

    

    public boolean isSelected() {
        return selected;
    }

    public void deselect() {
        this.selected = false;
    }

    public void select() {
        this.selected = true;
    }

    public void toggleSelection() {
        this.selected = !this.selected;
    }

    @Override
    public void onPress() {

        switch (type) {
            case RADIO_BUTTON:
                if (selected) {
                    return;
                }
                selected = true;

                if (collection != null) {
                    collection.performForEach((x) -> { return x instanceof DLAbstractImageButton && x != this; }, (x) -> ((DLAbstractImageButton<?>)x).deselect());
                }
                break;
            case TOGGLE_BUTTON:
                this.toggleSelection();
                break;
            default:
                break;
        }       
        
        super.onPress();
    }

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderArea(graphics, x, y, width, height, getBackColor(), this.style, isActive() ? (isSelected() ? ButtonState.DOWN : (isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON)) : ButtonState.DISABLED);
        GuiUtils.resetTint();
        renderImage(graphics, pMouseX, pMouseY, pPartialTick);
        GuiUtils.resetTint();
    }

    public abstract void renderImage(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick);

    public enum ButtonType {
        DEFAULT,
        RADIO_BUTTON,
        TOGGLE_BUTTON;
    }    
}