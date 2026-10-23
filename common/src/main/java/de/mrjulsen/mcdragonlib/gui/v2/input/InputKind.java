package de.mrjulsen.mcdragonlib.gui.v2.input;

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
