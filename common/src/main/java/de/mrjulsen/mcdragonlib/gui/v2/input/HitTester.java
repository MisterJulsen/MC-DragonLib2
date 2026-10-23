package de.mrjulsen.mcdragonlib.gui.v2.input;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

/**
 * Finds the components under a screen position.
 */
public final class HitTester {

    private HitTester() {
    }

    /**
     * All components under the point, innermost first. Events are delivered along this list.
     * <p>The search descends into the children in reverse order, so what is drawn last is hit first, and
     * respects clipping, visibility and hitTestVisible. It stops at the first component that accepts the
     * given input kind, so everything in the list before that one may still see the event, but nothing
     * behind it.</p>
     *
     * @param kind the kind of input to look for, which decides where the search stops
     * @return the components under the point, or an empty list if there is nothing there
     */
    public static List<GuiComponent> hitStack(GuiComponent root, double screenX, double screenY, InputKind kind) {
        List<GuiComponent> stack = new ArrayList<>(2);
        if (root != null) {
            collect(root, screenX, screenY, kind, stack);
        }
        return stack;
    }

    public static List<GuiComponent> hitStack(GuiComponent root, double screenX, double screenY) {
        return hitStack(root, screenX, screenY, InputKind.POINTER);
    }

    /**
     * The innermost component under the point, or null if there is none.
     */
    public static GuiComponent topmostAt(GuiComponent root, double screenX, double screenY, InputKind kind) {
        List<GuiComponent> stack = hitStack(root, screenX, screenY, kind);
        return stack.isEmpty() ? null : stack.get(0);
    }

    public static GuiComponent topmostAt(GuiComponent root, double screenX, double screenY) {
        return topmostAt(root, screenX, screenY, InputKind.POINTER);
    }

    /**
     * Whether the point is inside the area the component's children are clipped to. True for components
     * that do not clip.
     */
    public static boolean withinChildClip(GuiComponent component, double screenX, double screenY) {
        Rectangle clip = component.childClipScreenRect();
        return clip == null
            || (screenX >= clip.left() && screenX < clip.right()
            && screenY >= clip.top() && screenY < clip.bottom());
    }

    private static boolean collect(GuiComponent component, double screenX, double screenY, InputKind kind, List<GuiComponent> out) {
        if (!component.visible.get()) {
            return false;
        }
        boolean inside = component.containsScreenPoint(screenX, screenY);
        if (component.clipsChildren.get() && !inside) {
            return false;
        }
        if (withinChildClip(component, screenX, screenY)) {
            List<GuiComponent> children = component.getComponents();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (collect(children.get(i), screenX, screenY, kind, out)) {
                    return true;
                }
            }
        }
        if (!inside || !component.hitTestVisible.get()) {
            return false;
        }
        out.add(component);
        return component.acceptedInput.get().contains(kind);
    }
}
