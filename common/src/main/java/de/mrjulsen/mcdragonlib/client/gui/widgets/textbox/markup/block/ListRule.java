package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.BlockDecoration;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class ListRule implements IBlockRule, IBlockDecorator {

    public static final int TRAITS = BlockKind.TRAIT_INDENTABLE | BlockKind.TRAIT_MARKER | BlockKind.TRAIT_HANGING_INDENT;

    public static final BlockKind KIND_BULLET = BlockKind.register("bullet_list_item", TRAITS);
    public static final BlockKind KIND_ORDERED = BlockKind.register("ordered_list_item", TRAITS);
    public static final BlockKind KIND_TASK = BlockKind.register("task_list_item", TRAITS);

    public static final ListRule DEFAULT = new ListRule("-*+", ".)", "xX", 100000);

    public int markerColor = 0xFFA0A0A0;
    public int markerGap = 2;
    public int bulletSize = 3;
    public int checkBoxSize = 7;
    public int checkBoxBorder = 1;
    public int checkBoxBackground = 0xFF000000;
    public int checkMarkInset = 2;
    public int checkMarkColor = 0xFF5C9DFF;

    private final String bulletChars;
    private final String ordinalTerminators;
    private final String checkedMarks;
    private final int maxOrdinal;

    public ListRule(String bulletChars, String ordinalTerminators, String checkedMarks, int maxOrdinal) {
        this.bulletChars = bulletChars;
        this.ordinalTerminators = ordinalTerminators;
        this.checkedMarks = checkedMarks;
        this.maxOrdinal = maxOrdinal;
    }

    /*
    @Override
    public List<TextAction> formatActions() {
        String bullet = bulletChars.charAt(0) + " ";
        return List.of(
                TextAction.linePrefix("bullet_list", bullet),
                TextAction.linePrefix("task_list", bullet + "[ ] ")
        );
    }

     */

    @Override
    public String reservedCharacters() {
        return bulletChars;
    }

    @Override
    public List<TextAction> contextActions(TextContext context) {
        if (!context.isKind(KIND_TASK)) {
            return List.of();
        }
        int marker = markerColumn(context.lineText());
        if (marker < 0) {
            return List.of();
        }

        int offset = context.offsetOf(marker);
        String replacement = context.parsed().checked() ? " " : String.valueOf(checkedMarks.charAt(0));
        return List.of(TextAction.menu("toggle_task", target -> target.replaceRange(offset, offset + 1, replacement)));
    }

    private int markerColumn(String lineText) {
        int open = lineText.indexOf('[');
        if (open < 0 || open + 2 >= lineText.length() || lineText.charAt(open + 2) != ']') {
            return -1;
        }
        return open + 1;
    }

    @Override
    public ParsedLine parse(BlockContext context) {
        if (context.isBlank()) {
            return null;
        }
        int cursor = context.contentStart();
        int length = context.length();
        char first = context.charAt(cursor);

        if (bulletChars.indexOf(first) >= 0 && cursor + 1 < length && context.charAt(cursor + 1) == ' ') {
            int contentStart = cursor + 2;
            ParsedLine task = parseTask(context, contentStart);
            if (task != null) {
                return task;
            }
            context.emitMarkup(0, contentStart);
            context.parseInline(contentStart, length, TextStyle.DEFAULT);
            return context.build(KIND_BULLET, 0, false);
        }

        if (!Character.isDigit(first)) {
            return null;
        }
        int digits = cursor;
        while (digits < length && Character.isDigit(context.charAt(digits))) {
            digits++;
        }
        if (digits + 1 >= length || ordinalTerminators.indexOf(context.charAt(digits)) < 0 || context.charAt(digits + 1) != ' ') {
            return null;
        }

        int ordinal = MarkupText.parseInt(context.line(), cursor, digits, maxOrdinal);
        int contentStart = digits + 2;
        context.emitMarkup(0, contentStart);
        context.parseInline(contentStart, length, TextStyle.DEFAULT);
        return context.build(KIND_ORDERED, ordinal, false);
    }

    private ParsedLine parseTask(BlockContext context, int contentStart) {
        int length = context.length();
        if (contentStart + 3 >= length || context.charAt(contentStart) != '[' || context.charAt(contentStart + 2) != ']' || context.charAt(contentStart + 3) != ' ') {
            return null;
        }
        char mark = context.charAt(contentStart + 1);
        if (mark != ' ' && checkedMarks.indexOf(mark) < 0) {
            return null;
        }

        int textStart = contentStart + 4;
        context.emitMarkup(0, textStart);
        context.parseInline(textStart, length, TextStyle.DEFAULT);
        return context.build(KIND_TASK, 0, mark != ' ');
    }

    @Override
    public String continuationPrefix(ParsedLine line, String indent) {
        BlockKind kind = line.kind();
        if (kind == KIND_BULLET) {
            return indent + bulletChars.charAt(0) + " ";
        }
        if (kind == KIND_TASK) {
            return indent + bulletChars.charAt(0) + " [ ] ";
        }
        if (kind == KIND_ORDERED) {
            return indent + (line.level() + 1) + ordinalTerminators.charAt(0) + " ";
        }
        return null;
    }

    @Override
    public IBlockDecorator decoratorOf(BlockKind kind) {
        return kind == KIND_BULLET || kind == KIND_ORDERED || kind == KIND_TASK ? this : null;
    }

    @Override
    public void render(BlockDecoration d) {
        if (!d.firstRow()) {
            return;
        }
        BlockKind kind = d.layout().parsed().kind();
        if (kind == KIND_BULLET) {
            drawBullet(d);
        } else if (kind == KIND_ORDERED) {
            drawOrdinal(d);
        } else if (kind == KIND_TASK) {
            drawCheckBox(d);
        }
    }

    private void drawBullet(BlockDecoration d) {
        int x = d.markerX() + markerGap;
        int y = d.middle() - bulletSize / 2;
        d.graphics().graphics().fill(x, y, x + bulletSize, y + bulletSize, markerColor);
    }

    private void drawOrdinal(BlockDecoration d) {
        String ordinal = d.layout().parsed().level() + String.valueOf(ordinalTerminators.charAt(0));
        int width = d.measurer().font().width(ordinal);
        d.graphics().graphics().drawString(d.measurer().font(), ordinal,
                d.lineX() - width - markerGap, d.textTop(), markerColor, false);
    }

    private void drawCheckBox(BlockDecoration d) {
        int x = d.markerX();
        int y = d.textTop() + checkBoxBorder;
        int inner = checkBoxSize - checkBoxBorder;

        d.graphics().graphics().fill(x, y, x + checkBoxSize, y + checkBoxSize, markerColor);
        d.graphics().graphics().fill(x + checkBoxBorder, y + checkBoxBorder, x + inner, y + inner, checkBoxBackground);
        if (d.layout().parsed().checked()) {
            d.graphics().graphics().fill(x + checkMarkInset, y + checkMarkInset, x + checkBoxSize - checkMarkInset,
                    y + checkBoxSize - checkMarkInset, checkMarkColor);
        }
    }
}
