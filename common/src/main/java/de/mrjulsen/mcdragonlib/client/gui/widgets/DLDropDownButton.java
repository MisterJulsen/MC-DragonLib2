package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
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
        getContextMenu().open((int)mouseX, (int)mouseY, getX(), getY() + height);
    }
    
    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderArea(graphics, getX(), getY(), width, height, AreaStyle.NATIVE, isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON);
        GuiUtils.drawString(graphics, font, getX() + (width - DROP_DOWN_BUTTON_WIDTH) / 2, getY() + height / 2 - font.lineHeight / 2, getMessage(), DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.CENTER, true);
        if (getContextMenu().isOpen()) {
            GuiIcons.ARROW_UP.render(graphics, getX() + width - DROP_DOWN_BUTTON_WIDTH, getY() + height / 2 - GuiIcons.ICON_SIZE / 2);
        } else {
            GuiIcons.ARROW_DOWN.render(graphics, getX() + width - DROP_DOWN_BUTTON_WIDTH, getY() + height / 2 - GuiIcons.ICON_SIZE / 2);
        }
    }
}
