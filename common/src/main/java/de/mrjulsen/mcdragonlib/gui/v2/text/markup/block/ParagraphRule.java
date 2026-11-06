package de.mrjulsen.mcdragonlib.gui.v2.text.markup.block;

import de.mrjulsen.mcdragonlib.gui.v2.text.markup.BlockKind;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.gui.v2.text.style.TextStyle;

public final class ParagraphRule implements IBlockRule {

    public static final ParagraphRule DEFAULT = new ParagraphRule();

    @Override
    public ParsedLine parse(BlockContext context) {
        if (context.isBlank()) {
            if (context.length() > 0) {
                context.emit(new StyledSpan(0, context.length(), TextStyle.DEFAULT));
            }
            return context.buildWithIndent(BlockKind.PARAGRAPH, 0, 0, false);
        }

        context.emitMarkup(0, context.contentStart());
        context.parseInline(context.contentStart(), context.length(), TextStyle.DEFAULT);
        return context.buildWithIndent(BlockKind.PARAGRAPH, 0, 0, false);
    }
}
