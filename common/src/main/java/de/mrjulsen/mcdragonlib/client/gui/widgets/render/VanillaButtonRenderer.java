package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
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
            case SELECTED -> DefaultGuiTextures.VANILLA_BUTTON.getSprite(prefix + "selected");
            case DOWN -> DefaultGuiTextures.VANILLA_BUTTON.getSprite(prefix + "down");
            case DISABLED -> DefaultGuiTextures.VANILLA_BUTTON.getSprite("disabled");
            case DOWN_SELECTED -> DefaultGuiTextures.VANILLA_BUTTON.getSprite(prefix + "down_selected");
            case DISABLED_SELECTED -> DefaultGuiTextures.VANILLA_BUTTON.getSprite(prefix + "disabled_selected");
            default -> DefaultGuiTextures.VANILLA_BUTTON.getSprite(prefix + "normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
