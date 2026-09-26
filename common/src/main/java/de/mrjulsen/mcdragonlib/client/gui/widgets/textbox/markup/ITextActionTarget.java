package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;

public interface ITextActionTarget {
    boolean isEditable();
    String getSelectedText();
    boolean toggleInlineStyle(StyleFlag flag);
    void toggleInlineMarkup(String prefix, String suffix);
    void toggleLinePrefix(String prefix);
    void applyStyleBlock(String directives);
    void insertText(String text);
    void replaceRange(int from, int to, String text);
    void openLink(String link);
    void copyToClipboard(String text);
}
