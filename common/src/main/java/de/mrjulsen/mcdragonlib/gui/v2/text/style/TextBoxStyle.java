package de.mrjulsen.mcdragonlib.gui.v2.text.style;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;

/**
 * The colors and metrics a text box draws its content with: selection, caret, gutter, highlights and
 * the spacing around them.
 * <p>This only covers the text itself. The background behind it is drawn by a
 * {@link de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetRenderer}, see {@link TextBoxRenderers}.</p>
 * <p>Colors are packed ARGB ints rather than DLColor, because the text renderer hands them straight
 * to the vanilla font and fill calls.</p>
 */
public class TextBoxStyle {

    public static final TextBoxStyle VANILLA = new TextBoxStyle();

    /**
     * The style a text box uses while no theme defines one.
     */
    public static final Theme.Key<TextBoxStyle> THEME_KEY = Theme.key(DragonLib.MODID + ".textbox.style", VANILLA);

    public int background = 0xFF000000;
    public int border = 0xFFA0A0A0;
    public int borderFocused = 0xFFFFFFFF;
    public int borderDisabled = 0xFF606060;
    public int borderWidth = 1;

    public int textDisabled = 0xFF707070;
    public int placeholder = 0xFF808080;
    public int linkHovered = 0xFF9CC4FF;

    public int selection = 0x803B6EA8;
    public int selectionInactive = 0x40808080;
    public int currentLineHighlight = 0x18FFFFFF;
    public int lineHighlight = 0x40FFD700;
    public int searchHighlight = 0x444D9BFF;
    public int searchHighlightActive = 0x66FFAA00;

    public int caret = 0xFFFFFFFF;
    public boolean invertCaret = true;
    public int caretWidth = 1;
    public int caretMargin = 6;
    public int caretBlinkIntervalMs = 1000;

    public int gutterBackground = 0xFF101010;
    public int gutterText = 0xFF707070;
    public int gutterTextActive = 0xFFD0D0D0;
    public int gutterPadding = 4;

    public int scrollBarSize = 7;
    public int tooltipMaxWidth = 200;
    public float lineBreakSelectionRatio = 0.5F;

    /**
     * The border color for the given widget state.
     */
    public int borderOf(boolean enabled, boolean focused) {
        if (!enabled) {
            return borderDisabled;
        }
        return focused ? borderFocused : border;
    }
}
