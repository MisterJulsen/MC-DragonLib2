package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.GLGuiTextureData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiTexture;

public class VanillaSimpleButtonRenderer implements IStateRenderer<ButtonState> {

    public static final VanillaSimpleButtonRenderer VANILLA_BUTTON_GRAY = new VanillaSimpleButtonRenderer(ColorVariant.GRAY);
    public static final VanillaSimpleButtonRenderer VANILLA_BUTTON_BROWN = new VanillaSimpleButtonRenderer(ColorVariant.BROWN);
    public static final VanillaSimpleButtonRenderer VANILLA_BUTTON_RED = new VanillaSimpleButtonRenderer(ColorVariant.RED);

    public static enum ColorVariant {
        GRAY("gray"),
        BROWN("brown"),
        RED("red");

        private final String name;

        private ColorVariant(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final ColorVariant variant;

    public VanillaSimpleButtonRenderer(ColorVariant variant) {
        this.variant = variant;
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, ButtonState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> GuiTexture.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_selected");
            case DOWN -> GuiTexture.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_down");
            case DISABLED -> GuiTexture.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_disabled");
            case DOWN_SELECTED -> GuiTexture.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_down_selected");
            case DISABLED_SELECTED -> GuiTexture.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_disabled");
            default -> GuiTexture.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
