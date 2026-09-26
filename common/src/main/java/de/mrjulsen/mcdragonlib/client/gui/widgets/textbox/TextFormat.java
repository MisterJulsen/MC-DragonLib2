package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ComponentJsonParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ComponentTextParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkdownFormats;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.PlainTextParser;

public final class TextFormat {

    private static final Map<String, TextFormat> REGISTRY = new LinkedHashMap<>();

    public static final TextFormat PLAIN = register("plain", PlainTextParser.INSTANCE);
    public static final TextFormat MARKDOWN = register("markdown", MarkdownFormats.STANDARD);
    public static final TextFormat MARKDOWN_EXTENDED = register("markdown_extended", MarkdownFormats.EXTENDED);
    public static final TextFormat MINECRAFT_TEXT = register("minecraft_text", ComponentTextParser.SECTION);
    public static final TextFormat MINECRAFT_TEXT_AMPERSAND = register("minecraft_text_ampersand", ComponentTextParser.AMPERSAND);
    public static final TextFormat COMPONENT_JSON = register("component_json", ComponentJsonParser.INSTANCE);

    private final String id;
    private final ITextFormatParser parser;

    private TextFormat(String id, ITextFormatParser parser) {
        this.id = id;
        this.parser = parser;
    }

    public static TextFormat register(String id, ITextFormatParser parser) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("A text format with the id '" + id + "' is already registered.");
        }
        TextFormat format = new TextFormat(id, parser);
        REGISTRY.put(id, format);
        return format;
    }

    public static TextFormat byId(String id) {
        return REGISTRY.get(id);
    }

    public static TextFormat byIdOrDefault(String id, TextFormat fallback) {
        return REGISTRY.getOrDefault(id, fallback);
    }

    public static Collection<TextFormat> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static TextFormat of(ITextFormatParser parser) {
        for (TextFormat format : REGISTRY.values()) {
            if (format.parser == parser) {
                return format;
            }
        }
        return null;
    }

    public String id() {
        return id;
    }

    public ITextFormatParser parser() {
        return parser;
    }

    public String displayName() {
        return parser.name();
    }

    public boolean isMarkup() {
        return parser.isMarkup();
    }

    public boolean supportsStyleBlocks() {
        return parser.supportsStyleBlocks();
    }

    @Override
    public String toString() {
        return id;
    }
}
