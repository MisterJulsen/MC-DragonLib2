package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.DLRichTextEditBox.TextBoxState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiTexture;

public class VanillaTextBoxRenderer implements IStateRenderer<TextBoxState> {

    public static final VanillaTextBoxRenderer VANILLA_TEXTBOX = new VanillaTextBoxRenderer();
    
    public VanillaTextBoxRenderer() {
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, TextBoxState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> GuiTexture.VANILLA_TEXTBOX.getSprite("selected");
            case FOCUSED -> GuiTexture.VANILLA_TEXTBOX.getSprite("focused");
            case DISABLED -> GuiTexture.VANILLA_TEXTBOX.getSprite("disabled");
            default -> GuiTexture.VANILLA_TEXTBOX.getSprite("normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
