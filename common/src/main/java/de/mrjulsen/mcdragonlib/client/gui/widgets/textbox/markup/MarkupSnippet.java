package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

public record MarkupSnippet(String text, int contentStart, int contentLength) {

    public static MarkupSnippet wrapping(String prefix, String content, String suffix) {
        return new MarkupSnippet(prefix + content + suffix, prefix.length(), content.length());
    }

    public int contentEnd() {
        return contentStart + contentLength;
    }
}
