package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.BlockLayoutContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.BlockLayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.BlockDecoration;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class QuoteRule implements IBlockRule, IBlockLayout, IBlockDecorator {

    public static final int TRAITS = BlockKind.TRAIT_INDENTABLE | BlockKind.TRAIT_MARKER;

    public static final BlockKind KIND_QUOTE = BlockKind.register("quote", TRAITS);
    public static final BlockKind KIND_CALLOUT = BlockKind.register("callout", TRAITS);
    public static final BlockKind KIND_CALLOUT_BODY = BlockKind.register("callout_body", TRAITS);

    private static final int STATE_CALLOUT = MarkupState.allocateField(3);

    public static final QuoteRule DEFAULT = new QuoteRule('>');

    public static final int NEUTRAL_ACCENT = 0xFF808080;

    public int textColor = 0xFFA0A0A0;
    public int barColor = 0xFF606060;
    public int barWidth = 2;
    public int calloutPadding = 8;
    public int calloutPaddingV = 3;
    public int calloutBarWidth = 3;

    public int[] calloutAccents = {
            0xFF4493F8,
            0xFF3FB950,
            0xFFAB7DF8,
            0xFFD29922,
            0xFFF85149
    };

    public int[] calloutBackgrounds = {
            0x204493F8,
            0x203FB950,
            0x20AB7DF8,
            0x20D29922,
            0x20F85149
    };

    private final char marker;

    public QuoteRule(char marker) {
        this.marker = marker;
    }

    /*
    @Override
    public List<TextAction> formatActions() {
        return List.of(TextAction.linePrefix("quote", marker + " "));
    }

     */

    @Override
    public String reservedCharacters() {
        return String.valueOf(marker);
    }

    @Override
    public ParsedLine parse(BlockContext context) {
        if (context.isBlank() || context.charAt(context.contentStart()) != marker) {
            return null;
        }

        int cursor = context.contentStart();
        int depth = 0;
        while (cursor < context.length() && context.charAt(cursor) == marker) {
            depth++;
            cursor++;
            if (cursor < context.length() && context.charAt(cursor) == ' ') {
                cursor++;
            }
        }

        int indent = depth - 1;
        CalloutKind opening = CalloutKind.match(context.line(), cursor, context.length());
        if (opening != null) {
            context.emitMarkup(0, context.length());
            return context.buildWithIndent(KIND_CALLOUT, indent, opening.ordinal(), false);
        }

        CalloutKind open = calloutOf(context.state());
        context.emitMarkup(0, cursor);
        if (open != null) {
            context.parseInline(cursor, context.length(), TextStyle.DEFAULT);
            return context.buildWithIndent(KIND_CALLOUT_BODY, indent, open.ordinal(), false);
        }

        context.parseInline(cursor, context.length(), TextStyle.DEFAULT
                .withColor(textColor)
                .with(TextStyles.ITALIC, true));
        return context.buildWithIndent(KIND_QUOTE, indent, 0, false);
    }

    @Override
    public int nextState(CharSequence line, int contentStart, int incomingState) {
        if (contentStart >= line.length() || line.charAt(contentStart) != marker) {
            return STATE_UNCHANGED;
        }
        int cursor = contentStart;
        while (cursor < line.length() && line.charAt(cursor) == marker) {
            cursor++;
            if (cursor < line.length() && line.charAt(cursor) == ' ') {
                cursor++;
            }
        }
        CalloutKind opening = CalloutKind.match(line, cursor, line.length());
        return opening != null ? withCallout(incomingState, opening) : incomingState;
    }

    @Override
    public int resetState(int state) {
        return MarkupState.with(state, STATE_CALLOUT, 0);
    }

    @Override
    public String continuationPrefix(ParsedLine line, String indent) {
        if (!isQuote(line.kind())) {
            return null;
        }
        return indent + marker + " ";
    }

    @Override
    public IBlockLayout layoutOf(BlockKind kind) {
        return isCallout(kind) ? this : null;
    }

    @Override
    public BlockLayoutResult layout(BlockLayoutContext context) {
        if (context.showsMarkup()) {
            return BlockLayoutResult.of(context.textRows());
        }

        List<VisualRow> rows = new ArrayList<>(context.textRows(context.indent() + calloutPadding, calloutPadding));

        if (context.parsed().kind() == KIND_CALLOUT) {
            rows.set(0, context.padded(rows.get(0), calloutPaddingV, 0.0F));
        }
        ParsedLine next = context.parsedAt(context.line() + 1);
        if (next == null || next.kind() != KIND_CALLOUT_BODY) {
            int last = rows.size() - 1;
            rows.set(last, context.padded(rows.get(last), 0.0F, calloutPaddingV));
        }
        return BlockLayoutResult.of(rows);
    }

    @Override
    public IBlockDecorator decoratorOf(BlockKind kind) {
        return isQuote(kind) ? this : null;
    }

    @Override
    public void render(BlockDecoration d) {
        if (d.layout().parsed().kind() == KIND_QUOTE) {
            d.graphics().graphics().fill(d.markerX(), d.top(), d.markerX() + barWidth, d.bottom(), barColor);
            return;
        }
        drawCallout(d);
    }

    private void drawCallout(BlockDecoration d) {
        ParsedLine parsed = d.layout().parsed();
        CalloutKind kind = CalloutKind.byOrdinal(parsed.level());
        int accent = calloutColor(calloutAccents, kind.ordinal());
        int background = calloutColor(calloutBackgrounds, kind.ordinal());

        int barX = d.lineX() - calloutPadding - calloutBarWidth;
        if (d.right() > barX) {
            d.graphics().graphics().fill(barX, d.top(), d.right(), d.bottom(), background);
        }
        d.graphics().graphics().fill(barX, d.top(), barX + calloutBarWidth, d.bottom(), accent);

        if (parsed.kind() == KIND_CALLOUT && d.firstRow()) {
            d.graphics().graphics().drawString(d.measurer().font(), kind.title(), d.lineX(), d.textTop(), accent, false);
        }
    }

    public static int calloutColor(int[] palette, int ordinal) {
        if (palette == null || palette.length == 0) {
            return NEUTRAL_ACCENT;
        }
        return palette[Math.max(0, Math.min(ordinal, palette.length - 1))];
    }

    private static boolean isQuote(BlockKind kind) {
        return kind == KIND_QUOTE || isCallout(kind);
    }

    private static boolean isCallout(BlockKind kind) {
        return kind == KIND_CALLOUT || kind == KIND_CALLOUT_BODY;
    }

    private static int withCallout(int state, CalloutKind kind) {
        return MarkupState.with(state, STATE_CALLOUT, kind == null ? 0 : kind.ordinal() + 1);
    }

    private static CalloutKind calloutOf(int state) {
        int packed = MarkupState.get(state, STATE_CALLOUT);
        return packed == 0 ? null : CalloutKind.byOrdinal(packed - 1);
    }
}
