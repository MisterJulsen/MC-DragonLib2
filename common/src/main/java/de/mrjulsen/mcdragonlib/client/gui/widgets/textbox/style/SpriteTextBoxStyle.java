package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox.TextBoxState;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public class SpriteTextBoxStyle extends TextBoxStyle {

    public static final SpriteTextBoxStyle VANILLA_SPRITES = new SpriteTextBoxStyle();

    public DLTextureSheet sheet = DLTextureSheet.VANILLA_TEXTBOX;

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component,
                             TextBoxState state) {
        sheet.getSprite(spriteNameOf(state)).render(graphics, x, y, w, h);
    }

    public String spriteNameOf(TextBoxState state) {
        return switch (state) {
            case SELECTED -> "selected";
            case FOCUSED -> "focused";
            case DISABLED -> "disabled";
            default -> "normal";
        };
    }
}
