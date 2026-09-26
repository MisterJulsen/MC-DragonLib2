package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

public final class EscapeRule implements IInlineRule {

    public static final EscapeRule BACKSLASH = new EscapeRule('\\');

    private final char escape;

    public EscapeRule(char escape) {
        this.escape = escape;
    }

    @Override
    public int parse(InlineContext context, int at) {
        if (context.charAt(at) != escape || at + 1 >= context.limit()) {
            return 0;
        }
        context.flushTo(at);
        context.emitMarkup(at, at + 1);
        context.emitStyled(at + 1, at + 2, context.style());
        return 2;
    }

    @Override
    public String reservedCharacters() {
        return String.valueOf(escape);
    }
}
