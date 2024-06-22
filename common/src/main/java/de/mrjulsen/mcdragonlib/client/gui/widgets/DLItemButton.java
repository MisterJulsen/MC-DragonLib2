package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DLItemButton extends DLAbstractImageButton<DLItemButton> {

    public static final int DEFAULT_BUTTON_WIDTH = 18;
    public static final int DEFAULT_BUTTON_HEIGHT = 18;

    private ItemStack item;
    private boolean renderItemTooltip = true;

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, WidgetsCollection collection, int pX, int pY, int w, int h, Component customText, Consumer<DLItemButton> onClick) {
        super(type, color, collection, pX, pY, w, h, customText == null ? item.getHoverName() : customText, onClick);
        withItem(item);

        if (color == AreaStyle.NATIVE) {
            setFontColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE); 
        } else {            
            setFontColor(DragonLib.NATIVE_UI_FONT_COLOR);
        }
    }

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, int pX, int pY, int w, int h, Component customText, Consumer<DLItemButton> onClick) {
        this(type, color, item, null, pX, pY, w, h, customText, onClick);
    }

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, WidgetsCollection collection, int pX, int pY, Component customText, Consumer<DLItemButton> onClick) {
        this(type, color, item, collection, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, customText, onClick);
    }

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, int pX, int pY, Component customText, Consumer<DLItemButton> onClick) {
        this(type, color, item, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, customText, onClick);
    }

    public ItemStack getItem() {
        return item;
    }

    public DLItemButton withItem(ItemStack stack) {
        this.item = stack;
        return this;
    }

    public DLItemButton withDefaultItemTooltip(boolean b) {
        this.renderItemTooltip = b;
        return this;
    }

    @Override
    public void renderImage(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int labelWidth = 0;
        switch (getAlignment()) {            
            case LEFT:
                if (this.getMessage() != null) {
                    GuiUtils.drawString(graphics, font, getX() + 2 + 16 + 4, getY() + height / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.LEFT, false);
                }
                graphics.graphics().renderItem(item, getX() + 2, getY() + height / 2 - 8);
                break;
            case RIGHT:
                if (this.getMessage() != null) {
                    labelWidth = font.width(this.getMessage()) + 4;
                    GuiUtils.drawString(graphics, font, getX() + width - 2 + 2, getY() + height / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.RIGHT, false);
                }
                graphics.graphics().renderItem(item, getX() + width - 2 - labelWidth - 16, getY() + height / 2 - 8);
                break;
            case CENTER:
            default:
                if (this.getMessage() != null) {
                    labelWidth = font.width(this.getMessage()) + 4;
                    GuiUtils.drawString(graphics, font, getX() + width / 2 + 8 + 2, getY() + height / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.CENTER, false);
                }
                graphics.graphics().renderItem(item, getX() + width / 2 - 8 - labelWidth / 2, getY() + height / 2 - 8);
                break;
        }
        
    }

    public void renderTooltip(Screen parent, Graphics graphics, int pMouseX, int pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && renderItemTooltip) {
            GuiUtils.renderTooltip(parent, this, Screen.getTooltipFromItem(Minecraft.getInstance(), getItem()), -1, graphics, pMouseX, pMouseY);
        }
    }

    public static void renderAllItemButtonTooltips(Screen parent, Graphics graphics, int pMouseX, int pMouseY) {
        parent.renderables.stream().filter(x -> x instanceof DLItemButton b && b.isMouseOver(pMouseX, pMouseY) && b.renderItemTooltip).forEach(x -> ((DLItemButton)x).renderTooltip(parent, graphics, pMouseX, pMouseY));
    }
}