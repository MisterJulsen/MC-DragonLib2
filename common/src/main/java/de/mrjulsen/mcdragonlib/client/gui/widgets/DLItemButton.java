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

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, WidgetsCollection collection, int x, int y, int w, int h, Component customText, Consumer<DLItemButton> onClick) {
        super(type, color, collection, x, y, w, h, customText == null ? item.getHoverName() : customText, onClick);
        withItem(item);

        if (color == AreaStyle.NATIVE) {
            setFontColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE); 
        } else {            
            setFontColor(DragonLib.NATIVE_UI_FONT_COLOR);
        }
    }

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, int x, int y, int w, int h, Component customText, Consumer<DLItemButton> onClick) {
        this(type, color, item, null, x, y, w, h, customText, onClick);
    }

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, WidgetsCollection collection, int x, int y, Component customText, Consumer<DLItemButton> onClick) {
        this(type, color, item, collection, x, y, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, customText, onClick);
    }

    public DLItemButton(ButtonType type, AreaStyle color, ItemStack item, int x, int y, Component customText, Consumer<DLItemButton> onClick) {
        this(type, color, item, x, y, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, customText, onClick);
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
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, x + 2, y + height / 2 - 8);
                if (this.getMessage() != null) {
                    GuiUtils.drawString(graphics, font, x() + 2 + 16 + 4, y() + height() / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.LEFT, isRenderingTextShadow());
                }
                break;
            case RIGHT:            
                if (this.getMessage() != null && !this.getMessage().getString().isEmpty()) {
                    labelWidth = font.width(this.getMessage()) + 4;
                }
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, x() + width() - 2 - labelWidth - 16, y() + height() / 2 - 8);
                if (this.getMessage() != null) {
                    GuiUtils.drawString(graphics, font, x() + width() - 2 + 2, y() + height() / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.RIGHT, isRenderingTextShadow());
                }
                break;
            case CENTER:
            default:
            if (this.getMessage() != null && !this.getMessage().getString().isEmpty()) {
                    labelWidth = font.width(this.getMessage()) + 4;
                }
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, x() + width() / 2 - 8 - labelWidth / 2, y() + height() / 2 - 8);
                if (this.getMessage() != null) {
                    labelWidth = font.width(this.getMessage()) + 4;
                    GuiUtils.drawString(graphics, font, x() + width() / 2 + 8 + 2, y() + height() / 2 - font.lineHeight / 2, getMessage(), getFontColor(), EAlignment.CENTER, isRenderingTextShadow());
                }
                break;
        }
        
    }

    public void renderTooltip(Screen parent, Graphics graphics, int pMouseX, int pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && renderItemTooltip) {
            GuiUtils.renderTooltip(parent, this, parent.getTooltipFromItem(getItem()), -1, graphics, pMouseX, pMouseY);
        }
    }

    public static void renderAllItemButtonTooltips(Screen parent, Graphics graphics, int pMouseX, int pMouseY) {
        parent.renderables.stream().filter(x -> x instanceof DLItemButton b && b.isMouseOver(pMouseX, pMouseY) && b.renderItemTooltip).forEach(x -> ((DLItemButton)x).renderTooltip(parent, graphics, pMouseX, pMouseY));
    }
}