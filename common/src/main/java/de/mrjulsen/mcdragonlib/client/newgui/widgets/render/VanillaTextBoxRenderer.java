package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLRichTextEditBox.TextBoxState;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.Graphics;

public class VanillaTextBoxRenderer implements IStateRenderer<TextBoxState> {

    public static final VanillaTextBoxRenderer VANILLA_TEXTBOX = new VanillaTextBoxRenderer();
    
    public VanillaTextBoxRenderer() {
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, TextBoxState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("selected");
            case FOCUSED -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("focused");
            case DISABLED -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("disabled");
            default -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
