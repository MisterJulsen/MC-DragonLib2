package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLItemPicker.ItemPickerState;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public class DLItemPickerRenderer implements IStateRenderer<ItemPickerState> {

    public static final DLItemPickerRenderer INSTANCE = new DLItemPickerRenderer();
    
    public DLItemPickerRenderer() {
    }

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, ItemPickerState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("selected");
            case FOCUSED -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("focused");
            case DISABLED -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("disabled");
            default -> DefaultGuiTextures.VANILLA_TEXTBOX.getSprite("normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
