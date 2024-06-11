package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.network.chat.Component;

public class DLDropDownButton extends DLSplitButton {

    public DLDropDownButton(int pX, int pY, int pWidth, int pHeight, Component text, DLContextMenu dropDownOptions) {
        super(pX, pY, pWidth, pHeight, text, (btn) -> {}, dropDownOptions);
    }

    @Override
    public int getContextMenuOpenButton() {
        return NO_CONTEXT_MENU_BUTTON;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        getContextMenu().open((int)mouseX, (int)mouseY, x, y + height);
    }
    
    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderArea(graphics, x, y, width, height, getBackColor(), style, isActive() ? (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON) : ButtonState.DISABLED);
        
        int j = active ? getFontColor() : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED;
        
        GuiUtils.drawString(graphics, font, x + (width - DROP_DOWN_BUTTON_WIDTH) / 2, y + height / 2 - font.lineHeight / 2, getMessage(), j, EAlignment.CENTER, true);
        if (getContextMenu().isOpen()) {
            GuiIcons.ARROW_UP.render(graphics, x + width - DROP_DOWN_BUTTON_WIDTH, y + height / 2 - GuiIcons.ICON_SIZE / 2);
        } else {
            GuiIcons.ARROW_DOWN.render(graphics, x + width - DROP_DOWN_BUTTON_WIDTH, y + height / 2 - GuiIcons.ICON_SIZE / 2);
        }
    }
}
