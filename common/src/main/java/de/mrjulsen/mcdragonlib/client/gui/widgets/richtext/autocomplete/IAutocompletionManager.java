package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.autocomplete;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.util.math.Point;

@FunctionalInterface
public interface IAutocompletionManager<T> {

    /**
     * Called multiple times when a new window is opened or the suggestions change (e.g. due to modified text).
     * @param window The current {@link DLAutocompleteWindow} instance.
     * @param textBox The textbox for which autocompletion should be used.
     */
    void configureWindow(DLAutocompleteWindow<T> window, DLRichTextEditBox textBox);
    
    /**
     * Creates a new autocomplete popup window.
     * @param windowManager The {@link DLWindowManager} for window management.
     * @param textBox The textbox for which autocompletion should be used.
     * @return The new autocomplete window instance.
     */
    default DLAutocompleteWindow<T> createWindow(DLWindowManager windowManager, DLRichTextEditBox textBox) {
        DLAutocompleteWindow<T> win = new DLAutocompleteWindow<>(windowManager, textBox);
        Point pos = textBox.toScreenCoordinates();
        win.setPosition(pos.x(), pos.y() + textBox.height());
        return win;
    }
    
    /**
     * Run additional tasks after the autocomplete has been closed.
     * @param window The current {@link DLAutocompleteWindow} instance.
     * @param textBox The textbox for which autocompletion should be used.
     */
    default void closeWindow(DLAutocompleteWindow<T> window, DLRichTextEditBox textBox) {
    }
}
