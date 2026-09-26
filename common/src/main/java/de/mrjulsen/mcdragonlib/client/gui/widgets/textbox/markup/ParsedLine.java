package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.List;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public record ParsedLine(
        BlockKind kind,
        int indent,
        int level,
        boolean checked,
        List<StyledSpan> spans,
        String meta,
        ETextAlignment align,
        Object data
) {

    public ParsedLine(BlockKind kind, int indent, int level, boolean checked, List<StyledSpan> spans,
                      String meta, Object data) {
        this(kind, indent, level, checked, spans, meta, null, data);
    }

    public static ParsedLine paragraph(List<StyledSpan> spans) {
        return new ParsedLine(BlockKind.PARAGRAPH, 0, 0, false, spans, null, null);
    }

    public ParsedLine withAlign(ETextAlignment alignment) {
        return alignment == align ? this : new ParsedLine(kind, indent, level, checked, spans, meta, alignment, data);
    }

    public <T> T dataAs(Class<T> type) {
        return type.isInstance(data) ? type.cast(data) : null;
    }

    public boolean isMarkupOnly() {
        if (spans.isEmpty()) {
            return false;
        }
        for (StyledSpan span : spans) {
            if (!span.isMarkup()) {
                return false;
            }
        }
        return true;
    }
}
