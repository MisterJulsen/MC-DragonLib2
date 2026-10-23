package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;

/**
 * Arranges the children of a component. A layout runs in two passes, matching GuiComponent: first
 * {@link #measure(GuiComponent, double, double)} to find out how much room the children need, then
 * {@link #arrange(GuiComponent, Rectangle)} to put them in place.
 * <p>Layouts are stateless with regard to the host and may be shared between components, but the
 * properties of a shared instance of course apply to all of them. The static helpers here take
 * margins and the size modes off the implementer's hands.</p>
 */
public interface Layout {

    /**
     * How much room the host's children need in total, without the host's padding.
     *
     * @param availableWidth room for the children, may be infinite, which means unconstrained
     * @param availableHeight room for the children, may be infinite, which means unconstrained
     */
    Size measure(GuiComponent host, double availableWidth, double availableHeight);

    /**
     * Gives every child its final bounds via {@link #place(GuiComponent, double, double, double, double)}.
     *
     * @param contentArea the host's area inside its padding, in local coordinates
     */
    void arrange(GuiComponent host, Rectangle contentArea);

    /**
     * Called when this layout is assigned to a component. Override to remember the host, e.g. to
     * invalidate it when a property changes, see {@link AbstractLayout}.
     */
    default void attachHost(GuiComponent host) {
    }

    /**
     * Called when this layout is no longer used by that component.
     */
    default void detachHost(GuiComponent host) {
    }

    /**
     * The children that take part in layout, i.e. the visible ones. Use this instead of getComponents.
     */
    static List<GuiComponent> laidOutChildren(GuiComponent host) {
        List<GuiComponent> result = new ArrayList<>(host.getComponentsCount());
        List<GuiComponent> children = host.getComponents();
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            if (child.visible.get()) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * The measured size plus the child's margin, i.e. the room it really occupies.
     */
    static Size outerSize(GuiComponent child, Size measured) {
        Insets m = child.margin.get();
        return Size.of(measured.w() + m.horizontal(), measured.h() + m.vertical());
    }

    /**
     * Puts a child into the given outer rectangle, i.e. the margin is subtracted here. The way for a
     * layout to assign bounds.
     */
    static void place(GuiComponent child, double x, double y, double width, double height) {
        Insets m = child.margin.get();
        child.setBounds(x + m.left(), y + m.top(), Math.max(0, width - m.horizontal()), Math.max(0, height - m.vertical()));
    }

    /**
     * Whether the child wants more width than its content needs, i.e. its width mode is FILL or WEIGHT.
     * Layouts use this to decide who gets the leftover space.
     */
    static boolean growsHorizontally(GuiComponent child) {
        SizeMode mode = child.widthMode.get();
        return mode == SizeMode.FILL || mode == SizeMode.WEIGHT;
    }

    /**
     * Whether the child wants more height than its content needs, i.e. its height mode is FILL or WEIGHT.
     */
    static boolean growsVertically(GuiComponent child) {
        SizeMode mode = child.heightMode.get();
        return mode == SizeMode.FILL || mode == SizeMode.WEIGHT;
    }
}
