package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;

public class DLCheckBox extends DLToggleButton {

    public DLCheckBox(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.setTint(backgroundTint.get());
        if (isSelected()) {
            componentRenderer.get().renderSprite(graphics, 0, height() / 2 - 6, 12, 12, this, ButtonState.DISABLED_SELECTED);
        } else {
            componentRenderer.get().renderSprite(graphics, 0, height() / 2 - 6, 12, 12, this, ButtonState.DISABLED);
        }
        if (checked.get()) {
            GuiUtils.fill(graphics, 3, height() / 2 - 3, 6, 6, textColor.get());
        }
        GuiUtils.resetTint();
        
        GuiUtils.setTint(textColor.get());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, 16, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, text.get(), enabled.get() ? DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR : DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.LEFT, true);
        GuiUtils.resetTint();
    }
    
}
