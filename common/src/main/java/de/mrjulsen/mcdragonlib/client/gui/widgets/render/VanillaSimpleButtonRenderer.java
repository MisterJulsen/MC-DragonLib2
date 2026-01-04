package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

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
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, ButtonState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DLTextureSheet.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_selected");
            case DOWN -> DLTextureSheet.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_down");
            case DISABLED -> DLTextureSheet.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_disabled");
            case DOWN_SELECTED -> DLTextureSheet.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_down_selected");
            case DISABLED_SELECTED -> DLTextureSheet.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_disabled");
            default -> DLTextureSheet.DRAGONLIB_UI.getSprite("button_" + variant.getName() + "_normal");
        };
        sprite.render(graphics, x, y, w, h);
    }
}
