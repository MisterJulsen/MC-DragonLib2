package de.mrjulsen.mcdragonlib.gui.v2.text.markup.block;

import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.text.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.BlockKind;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.TextAction;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.TextContext;
import de.mrjulsen.mcdragonlib.gui.v2.text.render.IBlockDecorator;
import net.minecraft.network.chat.Component;

public interface IBlockRule {

    int STATE_UNCHANGED = -1;

    ParsedLine parse(BlockContext context);

    default int nextState(CharSequence line, int contentStart, int incomingState) {
        return STATE_UNCHANGED;
    }

    default int resetState(int state) {
        return state;
    }

    default String continuationPrefix(ParsedLine line, String indent) {
        return null;
    }

    default IBlockLayout layoutOf(BlockKind kind) {
        return null;
    }

    default IBlockDecorator decoratorOf(BlockKind kind) {
        return null;
    }

    default List<TextAction> formatActions() {
        return List.of();
    }

    default List<Component> contextTooltip(TextContext context) {
        return List.of();
    }

    default String reservedCharacters() {
        return "";
    }
}
