package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.Collections;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class PlainTextParser implements ITextFormatParser {

    public static final PlainTextParser INSTANCE = new PlainTextParser();

    private PlainTextParser() {
    }

    @Override
    public ParsedLine parseLine(CharSequence line, int incomingState) {
        List<StyledSpan> spans = line.isEmpty()
                ? Collections.emptyList()
                : List.of(new StyledSpan(0, line.length(), TextStyle.DEFAULT));
        return ParsedLine.paragraph(spans);
    }

    @Override
    public int nextState(CharSequence line, int incomingState) {
        return INITIAL_STATE;
    }

    @Override
    public String name() {
        return "Plain Text";
    }

    @Override
    public boolean isMarkup() {
        return false;
    }
}
