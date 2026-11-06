package de.mrjulsen.mcdragonlib.gui.v2.text.style;

import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetState;
import de.mrjulsen.mcdragonlib.util.DLColor;

/**
 * The background renderers a text box can be themed with, see
 * de.mrjulsen.mcdragonlib.gui.v2.widget.DLTextBox#THEME_KEY_RENDERER.
 */
public final class TextBoxRenderers {

    /**
     * A filled rectangle with a border, in the colors of the component's {@link TextBoxStyle}.
     */
    public static final WidgetRenderer<GuiComponent> FLAT = TextBoxRenderers::renderFlat;

    /**
     * The vanilla text box texture.
     */
    public static final WidgetRenderer<GuiComponent> VANILLA_SPRITES = new SpriteRenderer(DLTextureSheet.VANILLA_TEXTBOX);

    private TextBoxRenderers() {
    }

    private static void renderFlat(DLGuiGraphics g, GuiComponent component, double x, double y, double width, double height) {
        TextBoxStyle style = component.getThemeValue(TextBoxStyle.THEME_KEY);
        int border = style.borderOf(component.isEnabled(), component.isFocused());
        GuiUtils.fill(g, x, y, width, height, DLColor.fromInt(border));
        GuiUtils.fill(g, x + style.borderWidth, y + style.borderWidth,
            Math.max(0, width - style.borderWidth * 2), Math.max(0, height - style.borderWidth * 2),
            DLColor.fromInt(style.background));
    }

    /**
     * Draws the background from a sprite sheet, picking the sprite by {@link WidgetState}.
     */
    public static class SpriteRenderer implements WidgetRenderer<GuiComponent> {

        protected final DLTextureSheet sheet;

        public SpriteRenderer(DLTextureSheet sheet) {
            this.sheet = sheet;
        }

        @Override
        public void render(DLGuiGraphics g, GuiComponent component, double x, double y, double width, double height) {
            GuiUtils.sprite(g, sheet.getSprite(spriteNameOf(WidgetState.of(component))), x, y, width, height);
        }

        public String spriteNameOf(WidgetState state) {
            return switch (state) {
                case FOCUSED -> "focused";
                case DISABLED -> "disabled";
                case HOVERED, PRESSED -> "selected";
                default -> "normal";
            };
        }
    }
}
