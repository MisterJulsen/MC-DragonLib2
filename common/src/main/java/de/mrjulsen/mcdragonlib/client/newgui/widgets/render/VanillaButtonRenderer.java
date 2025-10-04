package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiTexture;

public class VanillaButtonRenderer implements IStateRenderer<ButtonState> {

    public static final VanillaButtonRenderer VANILLA_BUTTONS = new VanillaButtonRenderer(false);
    public static final VanillaButtonRenderer VANILLA_LEGACY_BUTTONS = new VanillaButtonRenderer(true);
    
    private final boolean legacy;

    public VanillaButtonRenderer(boolean legacy) {
        this.legacy = legacy;
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, ButtonState state) {
        String prefix = legacy ? "legacy_" : "";
        AbstractSprite sprite = switch (state) {
            case SELECTED -> GuiTexture.VANILLA_BUTTON.getSprite(prefix + "selected");
            case DOWN -> GuiTexture.VANILLA_BUTTON.getSprite(prefix + "down");
            case DISABLED -> GuiTexture.VANILLA_BUTTON.getSprite("disabled");
            case DOWN_SELECTED -> GuiTexture.VANILLA_BUTTON.getSprite(prefix + "down_selected");
            case DISABLED_SELECTED -> GuiTexture.VANILLA_BUTTON.getSprite(prefix + "disabled_selected");
            default -> GuiTexture.VANILLA_BUTTON.getSprite(prefix + "normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
