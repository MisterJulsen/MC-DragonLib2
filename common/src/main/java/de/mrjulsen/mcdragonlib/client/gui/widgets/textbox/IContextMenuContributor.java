package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;

@FunctionalInterface
public interface IContextMenuContributor<T> {
    void contribute(T source, TextContext context, List<DLContextMenu.ItemEntry> entries);
}
