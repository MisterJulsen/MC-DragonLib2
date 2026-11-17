/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.gui.v2.text.autocomplete;

import de.mrjulsen.mcdragonlib.client.gui.v2.widget.DLTextBox;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLWindowManager;

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
    default DLAutocompleteWindow<T> createWindow(DLWindowManager windowManager, DLTextBox textBox) {
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
