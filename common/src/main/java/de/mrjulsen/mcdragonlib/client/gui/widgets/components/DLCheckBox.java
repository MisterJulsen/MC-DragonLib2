/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
