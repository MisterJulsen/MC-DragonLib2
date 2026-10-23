package de.mrjulsen.mcdragonlib.gui.v2.window;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;

/**
 * A floating piece of content outside the window stack: dropdowns, context menus, tooltips with
 * controls.
 * <p>Popups live on their own pane above all windows, so they are never clipped by the window they
 * belong to. They can be chained, which is how nested menus work: closing a popup closes its
 * children as well. A popup is not a component, it wraps one.</p>
 */
public final class Popup {

    /**
     * Which side of the anchor component a popup opens on. It flips to the opposite side if there is no
     * room, see {@link #keepOnScreen}.
     */
    public enum Placement {
        BELOW,
        ABOVE,
        RIGHT_OF,
        LEFT_OF
    }

    private final GuiComponent content;

    private WindowManager manager;
    private Popup parent;
    private Popup child;
    private GuiComponent owner;
    private Runnable closeHandler;

    /**
     * Whether a click anywhere outside the popup closes it. The usual behaviour for menus.
     */
    public final BooleanProperty lightDismiss = new BooleanProperty(true);
    /**
     * Whether Escape closes this popup. Checked before the window below it gets the key.
     */
    public final BooleanProperty closeOnEscape = new BooleanProperty(true);
    /**
     * Whether the popup is kept inside the screen: it flips to the other side of its anchor and is
     * clamped to the edges.
     */
    public final BooleanProperty keepOnScreen = new BooleanProperty(true);

    /**
     * @param content the component to show; its size is taken from its measurement when opening
     * @throws IllegalArgumentException if the content is null
     */
    public Popup(GuiComponent content) {
        if (content == null) {
            throw new IllegalArgumentException("A popup needs content.");
        }
        this.content = content;
    }

    public GuiComponent content() {
        return content;
    }

    public Popup parent() {
        return parent;
    }

    public Popup child() {
        return child;
    }

    public WindowManager manager() {
        return manager;
    }

    public boolean isOpen() {
        return manager != null;
    }

    /**
     * The outermost popup of this chain, i.e. the one that was opened first.
     */
    public Popup root() {
        Popup current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * The component this popup belongs to. When that component's subtree goes away, so does the popup.
     */
    public GuiComponent owner() {
        return owner;
    }

    /**
     * Sets the owning component, which is done automatically when opening relative to an anchor.
     *
     * @return this popup, for chaining
     */
    public Popup owner(GuiComponent value) {
        this.owner = value;
        return this;
    }

    /**
     * Measures and lays out the content so its size is known before it is placed. Called by the open
     * methods; only needed by hand if the size is read before opening.
     *
     * @return this popup, for chaining
     */
    public Popup prepare(WindowManager manager) {
        if (manager == null) {
            return this;
        }
        Size size = content.measure(manager.screenWidth(), manager.screenHeight());
        content.setSize(size);
        content.applyLayout();
        return this;
    }

    /**
     * Sets a callback that runs once the popup closes, no matter how.
     *
     * @return this popup, for chaining
     */
    public Popup onClosed(Runnable handler) {
        this.closeHandler = handler;
        return this;
    }

    /**
     * Opens the popup at a fixed screen position, e.g. at the mouse for a context menu.
     *
     * @return this popup, for chaining
     */
    public Popup openAt(WindowManager manager, double screenX, double screenY) {
        if (manager == null) {
            return this;
        }
        prepare(manager);
        this.manager = manager;
        manager.addPopup(this);
        place(screenX, screenY);
        return this;
    }

    /**
     * Opens the popup below another component, the usual placement for a dropdown.
     *
     * @return this popup, for chaining
     */
    public Popup openBelow(GuiComponent anchor, double offsetX, double offsetY) {
        return openRelativeTo(anchor, Placement.BELOW, offsetX, offsetY);
    }

    /**
     * Opens the popup to the right of another component, the usual placement for a submenu.
     *
     * @return this popup, for chaining
     */
    public Popup openRightOf(GuiComponent anchor, double offsetX, double offsetY) {
        return openRelativeTo(anchor, Placement.RIGHT_OF, offsetX, offsetY);
    }

    /**
     * Opens the popup next to another component and makes that component its owner. Flips to the
     * opposite side if there is not enough room, as long as {@link #keepOnScreen} is set.
     *
     * @return this popup, for chaining
     */
    public Popup openRelativeTo(GuiComponent anchor, Placement placement, double offsetX, double offsetY) {
        WindowManager target = anchor.getWindowManager();
        if (target == null) {
            return this;
        }
        prepare(target);
        this.manager = target;
        if (owner == null) {
            owner = anchor;
        }
        target.addPopup(this);

        double left = anchor.getTransform().screenX();
        double top = anchor.getTransform().screenY();
        double right = anchor.getTransform().screenRight();
        double bottom = anchor.getTransform().screenBottom();

        double x;
        double y;
        switch (placement) {
            case ABOVE -> {
                x = left + offsetX;
                y = top - content.dHeight() - offsetY;
            }
            case RIGHT_OF -> {
                x = right + offsetX;
                y = top + offsetY;
            }
            case LEFT_OF -> {
                x = left - content.dWidth() - offsetX;
                y = top + offsetY;
            }
            default -> {
                x = left + offsetX;
                y = bottom + offsetY;
            }
        }
        placeRelative(x, y, placement, left, top, right, bottom);
        return this;
    }

    /**
     * Opens another popup as a child of this one, closing a previous child first. Used for submenus.
     *
     * @return the child popup
     */
    public Popup openChild(Popup popup, GuiComponent anchor, Placement placement, double offsetX, double offsetY) {
        closeChild();
        popup.parent = this;
        popup.inherit(this);
        this.child = popup;
        popup.openRelativeTo(anchor, placement, offsetX, offsetY);
        return popup;
    }

    /**
     * Opens another popup as a child of this one at a fixed screen position.
     *
     * @return the child popup
     */
    public Popup openChildAt(Popup popup, double screenX, double screenY) {
        closeChild();
        popup.parent = this;
        popup.inherit(this);
        this.child = popup;
        popup.openAt(manager, screenX, screenY);
        return popup;
    }

    private void inherit(Popup parent) {
        if (owner == null) {
            owner = parent.owner;
        }
    }

    private void place(double screenX, double screenY) {
        if (manager == null) {
            return;
        }
        content.setPosition(clampX(screenX), clampY(screenY));
    }

    private void placeRelative(double screenX, double screenY, Placement placement,
                               double anchorLeft, double anchorTop, double anchorRight, double anchorBottom) {
        if (manager == null) {
            return;
        }
        double x = screenX;
        double y = screenY;
        if (keepOnScreen.get()) {
            if (placement == Placement.RIGHT_OF && x > maxX()) {
                x = anchorLeft - content.dWidth();
            } else if (placement == Placement.LEFT_OF && x < 0) {
                x = anchorRight;
            }
            if (placement == Placement.BELOW && y > maxY()) {
                y = anchorTop - content.dHeight();
            } else if (placement == Placement.ABOVE && y < 0) {
                y = anchorBottom;
            }
        }
        content.setPosition(clampX(x), clampY(y));
    }

    private double maxX() {
        return Math.max(0, manager.screenWidth() - content.dWidth());
    }

    private double maxY() {
        return Math.max(0, manager.screenHeight() - content.dHeight());
    }

    private double clampX(double x) {
        return keepOnScreen.get() ? Math.min(Math.max(x, 0), maxX()) : x;
    }

    private double clampY(double y) {
        return keepOnScreen.get() ? Math.min(Math.max(y, 0), maxY()) : y;
    }

    /**
     * Closes this popup and its children, but leaves its parent open.
     */
    public void close() {
        closeChild();
        WindowManager host = manager;
        if (host == null) {
            return;
        }
        manager = null;
        if (parent != null && parent.child == this) {
            parent.child = null;
        }
        parent = null;
        host.removePopup(this);
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    /**
     * Closes the whole chain, starting at its root. What a menu entry does once it was clicked.
     */
    public void closeCascade() {
        root().close();
    }

    /**
     * Closes the child popup, if there is one, e.g. when another submenu is opened.
     */
    public void closeChild() {
        if (child != null) {
            Popup current = child;
            child = null;
            current.close();
        }
    }
}
