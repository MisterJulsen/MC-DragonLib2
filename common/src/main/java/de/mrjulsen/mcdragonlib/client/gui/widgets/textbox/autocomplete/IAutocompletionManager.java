package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.autocomplete;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox;
import de.mrjulsen.mcdragonlib.util.math.Point;

@FunctionalInterface
public interface IAutocompletionManager<T> {

    void configureWindow(DLAutocompleteWindow<T> window, DLTextBox textBox);

    default DLAutocompleteWindow<T> createWindow(DLWindowManager windowManager, DLTextBox textBox) {
        DLAutocompleteWindow<T> win = new DLAutocompleteWindow<>(windowManager, textBox);
        Point pos = textBox.toScreenCoordinates();
        win.setPosition(pos.x(), pos.y() + textBox.height());
        return win;
    }

    default void closeWindow(DLAutocompleteWindow<T> window, DLTextBox textBox) {
    }
}
