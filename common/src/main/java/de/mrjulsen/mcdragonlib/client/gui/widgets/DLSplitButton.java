package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.network.chat.Component;

public class DLSplitButton extends DLButton {

    public static final int DROP_DOWN_BUTTON_WIDTH = 16;

    public DLSplitButton(int pX, int pY, int pWidth, int pHeight, Component text, Consumer<DLSplitButton> onClick, DLContextMenu dropDownOptions) {
        super(pX, pY, pWidth, pHeight, text, onClick);
        this.setMenu(dropDownOptions);
    }

    @Override
    public int getContextMenuOpenButton() {
        return NO_CONTEXT_MENU_BUTTON;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX > x + width - DROP_DOWN_BUTTON_WIDTH) {
            getContextMenu().open((int)mouseX, (int)mouseY, x, y + height);
        } else {
            super.onClick(mouseX, mouseY);
        }
    }
    
    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderArea(graphics, x, y, width - DROP_DOWN_BUTTON_WIDTH + 1, height, style, !isActive() ? ButtonState.DOWN : (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON));
        DynamicGuiRenderer.renderArea(graphics, x + width - DROP_DOWN_BUTTON_WIDTH, y, DROP_DOWN_BUTTON_WIDTH, height, style, !isActive() || getContextMenu().isOpen() ? ButtonState.DOWN : (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON));

        GuiUtils.drawString(graphics, font, x + (width - DROP_DOWN_BUTTON_WIDTH) / 2, y + height / 2 - font.lineHeight / 2, getMessage(), DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.CENTER, true);
        if (getContextMenu().isOpen()) {
            GuiIcons.ARROW_UP.render(graphics, x + width - DROP_DOWN_BUTTON_WIDTH, y + height / 2 - GuiIcons.ICON_SIZE / 2);
        } else {
            GuiIcons.ARROW_DOWN.render(graphics, x + width - DROP_DOWN_BUTTON_WIDTH, y + height / 2 - GuiIcons.ICON_SIZE / 2);
        }
    }
}
