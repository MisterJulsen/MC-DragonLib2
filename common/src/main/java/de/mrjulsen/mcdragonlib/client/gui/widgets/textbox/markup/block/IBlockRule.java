package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;
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

    default List<TextAction> contextActions(TextContext context) {
        return List.of();
    }

    default List<Component> contextTooltip(TextContext context) {
        return List.of();
    }

    default String reservedCharacters() {
        return "";
    }
}
