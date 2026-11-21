/*
 * DragonLib 3 - 1.21.1
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

package de.mrjulsen.mcdragonlib.client.gui.v2.input;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The kinds of input a component can consume, see GuiComponent#acceptedInput.
 * <p>During hit testing a component that does not accept the kind in question is still added to the
 * stack, but it does not stop the search, so the event also reaches the components behind it. That
 * is how a list inside a list can let the outer one scroll.</p>
 */
public enum InputKind {
    /**
     * Mouse movement, hovering, enter and leave.
     */
    POINTER,
    /**
     * Pressing, releasing and clicking.
     */
    CLICK,
    /**
     * The mouse wheel.
     */
    SCROLL,
    /**
     * Dragging with a button held down.
     */
    DRAG,
    /**
     * Being a target for drag and drop.
     */
    DROP,
    /**
     * Files dropped onto the game window from outside.
     */
    FILES;

    public static final Set<InputKind> ALL = Collections.unmodifiableSet(EnumSet.allOf(InputKind.class));
    public static final Set<InputKind> NONE = Collections.unmodifiableSet(EnumSet.noneOf(InputKind.class));

    /**
     * A set with exactly these kinds, e.g. for a component that should only be clickable.
     */
    public static Set<InputKind> only(InputKind... kinds) {
        return Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(kinds)));
    }

    /**
     * Everything but these kinds, e.g. allExcept(SCROLL) to let the parent keep scrolling.
     */
    public static Set<InputKind> allExcept(InputKind... kinds) {
        EnumSet<InputKind> set = EnumSet.allOf(InputKind.class);
        set.removeAll(Arrays.asList(kinds));
        return Collections.unmodifiableSet(set);
    }
}
