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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyMap<T> {

    @FunctionalInterface
    public interface ICommand<T> {
        boolean run(T target, KeyEvent event);
    }

    public record Binding<T>(IKeyStroke stroke, ICommand<T> command) {}

    private final List<Binding<T>> bindings = new ArrayList<>();

    public KeyMap<T> bind(IKeyStroke stroke, ICommand<T> command) {
        bindings.add(new Binding<>(stroke, command));
        return this;
    }

    public boolean unbind(IKeyStroke stroke) {
        return bindings.removeIf(binding -> binding.stroke() == stroke);
    }

    public void clear() {
        bindings.clear();
    }

    public List<Binding<T>> bindings() {
        return Collections.unmodifiableList(bindings);
    }

    public boolean handle(T target, KeyEvent event) {
        for (int i = bindings.size() - 1; i >= 0; i--) {
            Binding<T> binding = bindings.get(i);
            if (binding.stroke().matches(event) && binding.command().run(target, event)) {
                return true;
            }
        }
        return false;
    }
}
