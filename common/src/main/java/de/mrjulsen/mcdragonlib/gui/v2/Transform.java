package de.mrjulsen.mcdragonlib.gui.v2;

import de.mrjulsen.mcdragonlib.util.math.Point;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

/**
 * Position, size, scale and scroll offset of a component, plus the screen rectangle derived from
 * them. Every component owns one; they form a chain along the component tree.
 * <p>Screen values are resolved lazily and cached: changing anything marks the transform dirty, and
 * the next read walks up the chain and recomputes only what the parent's version says is stale. All
 * screen coordinates are snapped to whole pixels, which is why {@link #width()} can differ slightly
 * from {@link #requestedWidth()}.</p>
 * <p>Three coordinate spaces show up in the methods here: <i>screen</i> is absolute and scaled,
 * <i>local</i> is relative to this component's top left corner and ignores its own scroll offset,
 * and <i>content</i> is local plus the scroll offset, i.e. the space child components live in.</p>
 */
public final class Transform {

    private static final double SNAP_BIAS = 1.0E-6D;

    private Transform parent;

    private double x;
    private double y;
    private double width;
    private double height;
    private double scale = 1D;
    private double scrollX;
    private double scrollY;

    private double screenX;
    private double screenY;
    private double screenRight;
    private double screenBottom;
    private double contentScale = 1D;
    private double outerScale = 1D;

    private long version = 1L;
    private long resolvedParentVersion = -1L;
    private boolean dirty = true;

    /**
     * Hooks this transform into another one, or detaches it with null. Done automatically when a
     * component is added to or removed from a parent.
     */
    public void setParent(Transform parent) {
        if (this.parent == parent) {
            return;
        }
        this.parent = parent;
        invalidate();
    }

    public Transform parent() {
        return parent;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    /**
     * Effective width after pixel snapping, i.e. what is actually on screen.
     */
    public double width() {
        resolve();
        return (screenRight - screenX) / outerScale;
    }

    /**
     * Effective height after pixel snapping, i.e. what is actually on screen.
     */
    public double height() {
        resolve();
        return (screenBottom - screenY) / outerScale;
    }

    /**
     * The width as it was set, without pixel snapping.
     */
    public double requestedWidth() {
        return width;
    }

    /**
     * The height as it was set, without pixel snapping.
     */
    public double requestedHeight() {
        return height;
    }

    /**
     * Local scale factor. Applies to this component's content and to all of its children.
     */
    public double scale() {
        return scale;
    }

    public double scrollX() {
        return scrollX;
    }

    public double scrollY() {
        return scrollY;
    }

    public void setX(double x) {
        if (this.x != x) {
            this.x = x;
            invalidate();
        }
    }

    public void setY(double y) {
        if (this.y != y) {
            this.y = y;
            invalidate();
        }
    }

    public void setWidth(double width) {
        double value = Math.max(width, 0D);
        if (this.width != value) {
            this.width = value;
            invalidate();
        }
    }

    public void setHeight(double height) {
        double value = Math.max(height, 0D);
        if (this.height != value) {
            this.height = value;
            invalidate();
        }
    }

    public void setScale(double scale) {
        if (this.scale != scale) {
            this.scale = scale;
            invalidate();
        }
    }

    /**
     * Shifts the content space horizontally. Not clamped here, see GuiComponent#setScroll.
     */
    public void setScrollX(double scrollX) {
        double snapped = snap(scrollX);
        if (this.scrollX != snapped) {
            this.scrollX = snapped;
            invalidate();
        }
    }

    /**
     * Shifts the content space vertically. Not clamped here, see GuiComponent#setScroll.
     */
    public void setScrollY(double scrollY) {
        double snapped = snap(scrollY);
        if (this.scrollY != snapped) {
            this.scrollY = snapped;
            invalidate();
        }
    }

    public double screenX() {
        resolve();
        return screenX;
    }

    public double screenY() {
        resolve();
        return screenY;
    }

    public double screenRight() {
        resolve();
        return screenRight;
    }

    public double screenBottom() {
        resolve();
        return screenBottom;
    }

    public double screenWidth() {
        resolve();
        return screenRight - screenX;
    }

    public double screenHeight() {
        resolve();
        return screenBottom - screenY;
    }

    /**
     * Total scale of this component's content: the parent chain's scale times its own.
     */
    public double contentScale() {
        resolve();
        return contentScale;
    }

    /**
     * Total scale of the parent chain, without this component's own scale.
     */
    public double outerScale() {
        resolve();
        return outerScale;
    }

    public Rectangle screenBounds() {
        resolve();
        return Rectangle.withPoints(screenX, screenY, screenRight, screenBottom);
    }

    public boolean containsScreenPoint(double pointX, double pointY) {
        resolve();
        return pointX >= screenX && pointX < screenRight && pointY >= screenY && pointY < screenBottom;
    }

    /**
     * This component's own rectangle, with its origin at 0/0.
     */
    public Rectangle localBounds() {
        return Rectangle.withSize(0, 0, width(), height());
    }

    /**
     * Local to screen, ignoring this component's scroll offset.
     */
    public double toScreenX(double localX) {
        resolve();
        return screenX + localX * contentScale;
    }

    public double toScreenY(double localY) {
        resolve();
        return screenY + localY * contentScale;
    }

    /**
     * Screen to local, ignoring this component's scroll offset.
     */
    public double toLocalX(double screenPointX) {
        resolve();
        return (screenPointX - screenX) / contentScale;
    }

    public double toLocalY(double screenPointY) {
        resolve();
        return (screenPointY - screenY) / contentScale;
    }

    /**
     * Content space to screen, i.e. local plus the scroll offset. Use this for anything that scrolls.
     */
    public double contentToScreenX(double contentX) {
        resolve();
        return screenX + (contentX - scrollX) * contentScale;
    }

    public double contentToScreenY(double contentY) {
        resolve();
        return screenY + (contentY - scrollY) * contentScale;
    }

    /**
     * Screen to content space, i.e. local plus the scroll offset. Use this for anything that scrolls.
     */
    public double screenToContentX(double screenPointX) {
        resolve();
        return (screenPointX - screenX) / contentScale + scrollX;
    }

    public double screenToContentY(double screenPointY) {
        resolve();
        return (screenPointY - screenY) / contentScale + scrollY;
    }

    public Point toScreen(double localX, double localY) {
        return Point.of(toScreenX(localX), toScreenY(localY));
    }

    public Point toLocal(double screenPointX, double screenPointY) {
        return Point.of(toLocalX(screenPointX), toLocalY(screenPointY));
    }

    /**
     * Converts a local rectangle to screen space, with all four edges snapped to whole pixels.
     */
    public Rectangle toScreen(double localX, double localY, double localWidth, double localHeight) {
        resolve();
        double left = screenX + localX * contentScale;
        double top = screenY + localY * contentScale;
        return Rectangle.withPoints(
            snap(left),
            snap(top),
            snap(left + localWidth * contentScale),
            snap(top + localHeight * contentScale)
        );
    }

    public Rectangle toScreen(Rectangle local) {
        return toScreen(local.x(), local.y(), local.width(), local.height());
    }

    /**
     * Marks the resolved screen values stale and bumps the version, so children recompute as well.
     */
    public void invalidate() {
        dirty = true;
        version++;
    }

    /**
     * Counter that changes whenever the resolved screen rectangle changes. Handy as a cache key.
     */
    public long version() {
        return version;
    }

    private void resolve() {
        if (parent == null) {
            if (!dirty) {
                return;
            }
            screenX = snap(x);
            screenY = snap(y);
            screenRight = snap(x + width);
            screenBottom = snap(y + height);
            contentScale = scale;
            outerScale = 1D;
            dirty = false;
            return;
        }
        parent.resolve();
        if (!dirty && resolvedParentVersion == parent.version) {
            return;
        }
        resolvedParentVersion = parent.version;
        double outer = parent.contentScale;
        double rawX = parent.screenX + (x - parent.scrollX) * outer;
        double rawY = parent.screenY + (y - parent.scrollY) * outer;

        double nextX = snap(rawX);
        double nextY = snap(rawY);
        double nextRight = snap(rawX + width * outer);
        double nextBottom = snap(rawY + height * outer);
        double nextScale = outer * scale;

        if (nextX != screenX || nextY != screenY || nextRight != screenRight
            || nextBottom != screenBottom || nextScale != contentScale) {
            screenX = nextX;
            screenY = nextY;
            screenRight = nextRight;
            screenBottom = nextBottom;
            contentScale = nextScale;
            version++;
        }
        outerScale = outer;
        dirty = false;
    }

    /**
     * Rounds a screen coordinate to a whole pixel, biased so that values just below .5 still round up.
     */
    public static double snap(double value) {
        return Math.floor(value + 0.5D + SNAP_BIAS);
    }
}
