/*
 * DragonLib 3 - 1.21.1
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

package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public class VanillaButtonRenderer implements IStateRenderer<ButtonState> {

    public static final VanillaButtonRenderer VANILLA_BUTTONS = new VanillaButtonRenderer(false);
    public static final VanillaButtonRenderer VANILLA_LEGACY_BUTTONS = new VanillaButtonRenderer(true);
    
    private final boolean legacy;

    public VanillaButtonRenderer(boolean legacy) {
        this.legacy = legacy;
    }

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, ButtonState state) {
        String prefix = legacy ? "legacy_" : "";
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DLTextureSheet.VANILLA_BUTTON.getSprite(prefix + "selected");
            case DOWN -> DLTextureSheet.VANILLA_BUTTON.getSprite(prefix + "down");
            case DISABLED -> DLTextureSheet.VANILLA_BUTTON.getSprite("disabled");
            case DOWN_SELECTED -> DLTextureSheet.VANILLA_BUTTON.getSprite(prefix + "down_selected");
            case DISABLED_SELECTED -> DLTextureSheet.VANILLA_BUTTON.getSprite(prefix + "disabled_selected");
            default -> DLTextureSheet.VANILLA_BUTTON.getSprite(prefix + "normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
