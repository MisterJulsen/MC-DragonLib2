package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.plugin;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.DLRichTextEditBox;

public class RichTextBoxExtensionContext {

    private  final DLRichTextEditBox textbox;

    public RichTextBoxExtensionContext(DLRichTextEditBox textbox) {
        this.textbox = textbox;
    }

    public DLRichTextEditBox getEditBox() {
        return textbox;
    }
}
