package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox.TextBoxState;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public class TextBoxStyle implements IStateRenderer<TextBoxState> {

    public static final TextBoxStyle VANILLA = new TextBoxStyle();

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

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, TextBoxState state) {
        graphics.graphics().fill(x, y, x + w, y + h, borderOf(state));
        graphics.graphics().fill(x + borderWidth, y + borderWidth, x + w - borderWidth, y + h - borderWidth, background);
    }

    public int borderOf(TextBoxState state) {
        return switch (state) {
            case DISABLED -> borderDisabled;
            case FOCUSED -> borderFocused;
            default -> border;
        };
    }
}
