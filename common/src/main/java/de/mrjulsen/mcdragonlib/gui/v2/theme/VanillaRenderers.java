package de.mrjulsen.mcdragonlib.gui.v2.theme;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;

/**
 * Ready made {@link WidgetRenderer}s drawing the vanilla Minecraft textures.
 * <p>Note that these currently always draw the sprite of the normal state, no matter what state the
 * component is in.</p>
 */
public final class VanillaRenderers {

    public static final WidgetRenderer BUTTON = (g, component, x, y, width, height) ->
        GuiUtils.sprite(g, sprite(DLTextureSheet.VANILLA_BUTTON, buttonSprite(WidgetState.NORMAL)), x, y, width, height);

    public static final WidgetRenderer TEXT_BOX = (g, component, x, y, width, height) ->
        GuiUtils.sprite(g, sprite(DLTextureSheet.VANILLA_TEXTBOX, textBoxSprite(WidgetState.NORMAL)), x, y, width, height);

    public static final WidgetRenderer SCROLL_THUMB_VERTICAL = (g, component, x, y, width, height) ->
        GuiUtils.sprite(g, sprite(DLTextureSheet.VANILLA_SCROLLBAR, "scroller_vertical_" + scrollerSprite(WidgetState.NORMAL)), x, y, width, height);

    public static final WidgetRenderer SCROLL_THUMB_HORIZONTAL = (g, component, x, y, width, height) ->
        GuiUtils.sprite(g, sprite(DLTextureSheet.VANILLA_SCROLLBAR, "scroller_horizontal_" + scrollerSprite(WidgetState.NORMAL)), x, y, width, height);

    private VanillaRenderers() {
    }

    private static AbstractSprite sprite(DLTextureSheet sheet, String name) {
        return sheet.getSprite(name);
    }

    private static String buttonSprite(WidgetState state) {
        return switch (state) {
            case DISABLED -> "disabled";
            case PRESSED -> "down_selected";
            case HOVERED, FOCUSED -> "selected";
            default -> "normal";
        };
    }

    private static String textBoxSprite(WidgetState state) {
        return switch (state) {
            case DISABLED -> "disabled";
            case FOCUSED -> "focused";
            case HOVERED -> "selected";
            default -> "normal";
        };
    }

    private static String scrollerSprite(WidgetState state) {
        return switch (state) {
            case DISABLED -> "disabled";
            case PRESSED -> "down_selected";
            case HOVERED, FOCUSED -> "selected";
            default -> "normal";
        };
    }
}
