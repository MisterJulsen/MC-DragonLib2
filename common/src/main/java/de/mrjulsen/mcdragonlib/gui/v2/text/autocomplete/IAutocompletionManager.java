package de.mrjulsen.mcdragonlib.gui.v2.text.autocomplete;

import de.mrjulsen.mcdragonlib.gui.v2.widget.DLTextBox;
import de.mrjulsen.mcdragonlib.gui.v2.window.WindowManager;

/**
 * Supplies the suggestions a {@link DLTextBox} offers, see {@code DLTextBox#autocompleteManager}.
 * <p>{@link #configureWindow} is called whenever the text changes, so that is where the item list and
 * the filter belong.</p>
 */
@FunctionalInterface
public interface IAutocompletionManager<T> {

    /**
     * Fills the dropdown with the suggestions for whatever is currently typed.
     */
    void configureWindow(DLAutocompleteWindow<T> window, DLTextBox textBox);

    /**
     * Creates the dropdown, positioned below the text box. Override to use a window of your own.
     *
     * @return the new window, not opened yet; the text box opens it
     */
    default DLAutocompleteWindow<T> createWindow(WindowManager windowManager, DLTextBox textBox) {
        DLAutocompleteWindow<T> win = new DLAutocompleteWindow<>(textBox);
        win.positionBelowParent();
        return win;
    }

    /**
     * Called before the dropdown is closed, for whatever cleanup a manager needs.
     */
    default void closeWindow(DLAutocompleteWindow<T> window, DLTextBox textBox) {
    }
}
