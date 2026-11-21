/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup;

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
