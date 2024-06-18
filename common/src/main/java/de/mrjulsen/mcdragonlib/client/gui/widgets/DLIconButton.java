package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.network.chat.Component;

public class DLIconButton extends DLAbstractImageButton<DLIconButton> {

    public static final int DEFAULT_BUTTON_WIDTH = 18;
    public static final int DEFAULT_BUTTON_HEIGHT = 18;

    private Sprite sprite;

    public DLIconButton(ButtonType type, AreaStyle color, Sprite sprite, WidgetsCollection collection, int pX, int pY, int w, int h, Component pMessage, Consumer<DLIconButton> onClick) {
        super(type, color, collection, pX, pY, w, h, pMessage, onClick);
        this.sprite = sprite;        

        if (color == AreaStyle.NATIVE) {
            setFontColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
        } else {
            setFontColor(DragonLib.NATIVE_UI_FONT_COLOR);
        }
    }

    public DLIconButton(ButtonType type, AreaStyle color, Sprite sprite, int pX, int pY, int w, int h, Component pMessage, Consumer<DLIconButton> onClick) {
        this(type, color, sprite, null, pX, pY, w, h, pMessage, onClick);
    }

    public DLIconButton(ButtonType type, AreaStyle color, Sprite sprite, WidgetsCollection collection, int pX, int pY, Component pMessage, Consumer<DLIconButton> onClick) {
        this(type, color, sprite, collection, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, pMessage, onClick);
    }

    public DLIconButton(ButtonType type, AreaStyle color, Sprite sprite, int pX, int pY, Component pMessage, Consumer<DLIconButton> onClick) {
        this(type, color, sprite, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, pMessage, onClick);
    }

    @Override
    public void renderImage(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int labelWidth = 0;
        switch (getAlignment()) {            
            case LEFT:
                sprite.render(graphics, getX() + 2, getY() + height / 2 - sprite.getHeight() / 2);
                if (this.getMessage() != null) {
                    GuiUtils.drawString(graphics, font, getX() + 2 + 4 + sprite.getWidth(), getY() + height / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.LEFT, false);
                }
                break;
            case RIGHT:
                sprite.render(graphics, getX() + width - 2 - 4 - sprite.getWidth(), getY() + height / 2 - sprite.getHeight() / 2);
                if (this.getMessage() != null) {;
                    GuiUtils.drawString(graphics, font, getX() + width - 2 + 4 - 4, getY() + height / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.LEFT, false);
                }
                break;
            case CENTER:
            default:
            sprite.render(graphics, getX() + width / 2 - sprite.getWidth() / 2 - labelWidth / 2, getY() + height / 2 - sprite.getHeight() / 2);
                if (this.getMessage() != null) {
                    labelWidth = font.width(this.getMessage()) + 4;
                    GuiUtils.drawString(graphics, font, getX() + width / 2 + sprite.getWidth() / 2 - labelWidth / 2 + 4, getY() + height / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.LEFT, false);
                }
                break;
        }
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Sprite getSprite() {
        return sprite;
    }
}