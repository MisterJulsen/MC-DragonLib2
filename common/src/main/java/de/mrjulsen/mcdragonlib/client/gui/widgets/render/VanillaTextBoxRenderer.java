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

package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox.TextBoxState;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

@Deprecated
public class VanillaTextBoxRenderer implements IStateRenderer<TextBoxState> {

    public static final VanillaTextBoxRenderer VANILLA_TEXTBOX = new VanillaTextBoxRenderer();
    
    public VanillaTextBoxRenderer() {
    }

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, TextBoxState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DLTextureSheet.VANILLA_TEXTBOX.getSprite("selected");
            case FOCUSED -> DLTextureSheet.VANILLA_TEXTBOX.getSprite("focused");
            case DISABLED -> DLTextureSheet.VANILLA_TEXTBOX.getSprite("disabled");
            default -> DLTextureSheet.VANILLA_TEXTBOX.getSprite("normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
