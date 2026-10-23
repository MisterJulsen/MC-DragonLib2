package de.mrjulsen.mcdragonlib.gui.v2.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiDebug;
import de.mrjulsen.mcdragonlib.gui.v2.Transform;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The drawing context handed to every component while rendering. It wraps Minecraft's GuiGraphics
 * and adds what the component tree needs: a clip stack, a depth cursor, the coordinate origin of the
 * component being drawn, and deferred layers.
 * <p>Components draw in their own local coordinates, because the pose stack is already translated
 * and scaled for them. Anything that has to escape the current clip rectangle, like a tooltip, goes
 * through {@link #defer(GuiLayer, Runnable)} instead.</p>
 * <p>One instance lives per window manager and is reused every frame, see
 * {@link #beginFrame(GuiGraphics, Font, float, double, double, double, double)}.</p>
 */
public final class DLGuiGraphics {

    private static final int INITIAL_DEPTH = 16;

    private record Deferred(GuiLayer layer, int order, Runnable action) {}

    private GuiGraphics graphics;
    private Font font;
    private float partialTick;
    private Rectangle screenBounds = Rectangle.EMPTY;
    private double screenMouseX;
    private double screenMouseY;

    private de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics legacy;

    private Rectangle[] clipStack = new Rectangle[INITIAL_DEPTH];
    private boolean[] clipScissored = new boolean[INITIAL_DEPTH];
    private int clipDepth;
    private Rectangle clip = Rectangle.EMPTY;

    private double zStep = 1.0D;
    private double zCursor;
    private double appliedZ;
    private double[] zStack = new double[INITIAL_DEPTH];
    private int zDepth;

    private double[] originStack = new double[INITIAL_DEPTH * 3];
    private int originDepth;
    private double originX;
    private double originY;
    private double originScale = 1.0D;

    private final List<Deferred> deferred = new ArrayList<>();
    private int deferredOrder;

    private final List<Rectangle> debugClips = new ArrayList<>();
    private final List<Rectangle> debugComponents = new ArrayList<>();
    private final List<Rectangle> debugCulled = new ArrayList<>();
    private int culledCount;
    private int renderedCount;

    /**
     * Starts a new frame and resets all stacks, counters and deferred actions. Called by the window
     * manager before it renders the tree.
     */
    public void beginFrame(GuiGraphics graphics, Font font, float partialTick, double screenWidth, double screenHeight, double screenMouseX, double screenMouseY) {
        this.graphics = graphics;
        this.font = font;
        this.partialTick = partialTick;
        this.legacy = null;
        this.screenBounds = Rectangle.withSize(0, 0, screenWidth, screenHeight);
        this.screenMouseX = screenMouseX;
        this.screenMouseY = screenMouseY;
        this.clip = screenBounds;
        this.clipDepth = 0;
        this.zCursor = 0;
        this.appliedZ = 0;
        this.zDepth = 0;
        this.originDepth = 0;
        this.originX = 0;
        this.originY = 0;
        this.originScale = 1.0D;
        this.deferred.clear();
        this.deferredOrder = 0;
        this.debugClips.clear();
        this.debugComponents.clear();
        this.debugCulled.clear();
        this.culledCount = 0;
        this.renderedCount = 0;
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    /**
     * The pose stack, already translated and scaled for the component currently being drawn.
     */
    public PoseStack pose() {
        return graphics.pose();
    }

    public Font font() {
        return font;
    }

    /**
     * Fraction of the current tick that has passed. For smooth animations.
     */
    public float partialTick() {
        return partialTick;
    }

    public Rectangle screenBounds() {
        return screenBounds;
    }

    /**
     * Mouse position in screen coordinates.
     */
    public double screenMouseX() {
        return screenMouseX;
    }

    public double screenMouseY() {
        return screenMouseY;
    }

    /**
     * Mouse position in the local coordinates of the component currently being drawn.
     */
    public double mouseX() {
        return (screenMouseX - originX) / originScale;
    }

    /**
     * Mouse position in the local coordinates of the component currently being drawn.
     */
    public double mouseY() {
        return (screenMouseY - originY) / originScale;
    }

    /**
     * A v1 graphics object sharing this frame's state, so old rendering code can be reused. Created on
     * first use.
     */
    public de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics legacy() {
        if (legacy == null) {
            legacy = new de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics(graphics, graphics.pose(), font, partialTick);
        }
        return legacy;
    }

    /**
     * The clip rectangle currently in effect, in screen coordinates.
     */
    public Rectangle clip() {
        return clip;
    }

    /**
     * Whether the rectangle overlaps the current clip rectangle at all. Used to skip invisible
     * components; also useful inside a component to skip rows that are scrolled out of view.
     */
    public boolean isVisible(Rectangle screenRect) {
        return screenRect.width() > 0 && screenRect.height() > 0 && screenRect.collision(clip);
    }

    /**
     * Whether the rectangle overlaps the current clip rectangle at all.
     */
    public boolean isVisible(double screenX, double screenY, double width, double height) {
        return width > 0 && height > 0
            && screenX < clip.right() && screenX + width > clip.left()
            && screenY < clip.bottom() && screenY + height > clip.top();
    }

    /**
     * Narrows the clip rectangle to the intersection with the given one. Must be paired with
     * {@link #popClip()}; prefer {@link #withClip} where possible.
     * <p>Skips the scissor call if the new rectangle already contains the current one, and honours the
     * debug switches in GuiDebug. A null rectangle pushes a no-op, which still has to be popped.</p>
     */
    public void pushClip(Rectangle screenRect) {
        if (clipDepth == clipStack.length) {
            int size = clipStack.length * 2;
            Rectangle[] nextClips = new Rectangle[size];
            boolean[] nextFlags = new boolean[size];
            System.arraycopy(clipStack, 0, nextClips, 0, clipStack.length);
            System.arraycopy(clipScissored, 0, nextFlags, 0, clipScissored.length);
            clipStack = nextClips;
            clipScissored = nextFlags;
        }
        int index = clipDepth++;
        clipStack[index] = clip;
        clipScissored[index] = false;
        if (screenRect == null) {
            return;
        }
        if (GuiDebug.showClipBounds.get()) {
            debugClips.add(screenRect);
        }
        if (GuiDebug.disableClipping.get() || encloses(screenRect, clip)) {
            return;
        }
        clipScissored[index] = true;
        clip = Rectangle.withPoints(
            Math.max(clip.left(), screenRect.left()),
            Math.max(clip.top(), screenRect.top()),
            Math.min(clip.right(), screenRect.right()),
            Math.min(clip.bottom(), screenRect.bottom())
        );
        graphics.flush();
        graphics.enableScissor(
            (int) Math.floor(screenRect.left()),
            (int) Math.floor(screenRect.top()),
            (int) Math.ceil(screenRect.right()),
            (int) Math.ceil(screenRect.bottom())
        );
    }

    /**
     * Narrows the clip rectangle to an area given in the component's local coordinates.
     */
    public void pushClip(GuiComponent owner, double localX, double localY, double width, double height) {
        pushClip(owner.getTransform().toScreen(localX, localY, width, height));
    }

    /**
     * Restores the clip rectangle from before the matching push.
     */
    public void popClip() {
        int index = --clipDepth;
        if (clipScissored[index]) {
            graphics.flush();
            graphics.disableScissor();
        }
        clip = clipStack[index];
    }

    /**
     * Runs the action with the clip narrowed to that local area and restores it afterwards, even if the
     * action throws.
     */
    public void withClip(GuiComponent owner, double localX, double localY, double width, double height, Runnable action) {
        pushClip(owner, localX, localY, width, height);
        try {
            action.run();
        } finally {
            popClip();
        }
    }

    /**
     * How much depth each component level advances. Raise it if deeply nested custom rendering runs out
     * of depth room.
     */
    public void setZStep(double step) {
        this.zStep = step;
    }

    public double zStep() {
        return zStep;
    }

    /**
     * Remembers the current depth, to be restored by {@link #popZ()}.
     */
    public void pushZ() {
        if (zDepth == zStack.length) {
            double[] next = new double[zStack.length * 2];
            System.arraycopy(zStack, 0, next, 0, zStack.length);
            zStack = next;
        }
        zStack[zDepth++] = appliedZ;
    }

    public void popZ() {
        appliedZ = zStack[--zDepth];
    }

    /**
     * Moves the depth cursor forward, so what follows is drawn on top of what came before. The cursor
     * never goes backwards within a frame, which keeps siblings from fighting over the same depth.
     */
    public void advanceZ(double amount) {
        zCursor += amount;
        pose().translate(0, 0, zCursor - appliedZ);
        appliedZ = zCursor;
    }

    /**
     * Enters a component: moves the pose stack to its position, applies its scale and advances the
     * depth. Called by the render pass, paired with {@link #endComponent(GuiComponent)}.
     */
    public void beginComponent(GuiComponent component) {
        Transform t = component.getTransform();
        PoseStack pose = pose();
        pose.pushPose();
        pose.translate((t.screenX() - originX) / originScale, (t.screenY() - originY) / originScale, 0);
        double scale = t.scale();
        if (scale != 1.0D) {
            pose.scale((float) scale, (float) scale, 1.0F);
        }
        pushOrigin(t.screenX(), t.screenY(), t.contentScale());
        pushZ();
        advanceZ(zStep);
    }

    public void endComponent(GuiComponent component) {
        popZ();
        popOrigin();
        pose().popPose();
    }

    /**
     * Applies the component's own clip rectangle, around its own drawing.
     */
    public void beginSelf(GuiComponent component) {
        pushClip(component.selfClipScreenRect());
    }

    public void endSelf(GuiComponent component) {
        popClip();
    }

    /**
     * Applies the clip rectangle for the children, i.e. the content area.
     */
    public void beginChildren(GuiComponent component) {
        pushClip(component.childClipScreenRect());
    }

    public void endChildren(GuiComponent component) {
        popClip();
    }

    private void pushOrigin(double x, double y, double scale) {
        if (originDepth * 3 == originStack.length) {
            double[] next = new double[originStack.length * 2];
            System.arraycopy(originStack, 0, next, 0, originStack.length);
            originStack = next;
        }
        int index = originDepth++ * 3;
        originStack[index] = originX;
        originStack[index + 1] = originY;
        originStack[index + 2] = originScale;
        originX = x;
        originY = y;
        originScale = scale;
    }

    private void popOrigin() {
        int index = --originDepth * 3;
        originX = originStack[index];
        originY = originStack[index + 1];
        originScale = originStack[index + 2];
    }

    /**
     * Screen position of the current component's origin, i.e. where its local 0/0 sits.
     */
    public double originX() {
        return originX;
    }

    public double originY() {
        return originY;
    }

    /**
     * Total scale the current component's content is drawn at.
     */
    public double originScale() {
        return originScale;
    }

    /**
     * Queues drawing for the end of the frame, on the given layer. The action runs outside all clip
     * rectangles and above the normal content, which is what tooltips, popups and drag ghosts need.
     * <p>Coordinates inside the action are screen coordinates, since the pose stack is no longer at the
     * component by then.</p>
     */
    public void defer(GuiLayer layer, Runnable action) {
        deferred.add(new Deferred(layer, deferredOrder++, action));
    }

    public boolean hasDeferred() {
        return !deferred.isEmpty();
    }

    /**
     * Draws everything that was deferred, layer by layer and within a layer in the order it was queued.
     * Actions that defer again are handled in a further round.
     */
    public void flushDeferred() {
        while (!deferred.isEmpty()) {
            List<Deferred> pending = new ArrayList<>(deferred);
            deferred.clear();
            pending.sort(Comparator.comparing(Deferred::layer).thenComparingInt(Deferred::order));
            for (Deferred val : pending) {
                PoseStack pose = pose();
                pose.pushPose();
                pushZ();
                advanceZ(zStep * 16.0D);
                val.action().run();
                popZ();
                pose.popPose();
            }
        }
    }

    /**
     * Reports a component that was skipped because it is off screen. Feeds the counters and the debug
     * overlay.
     */
    public void notifyCulled(GuiComponent component) {
        culledCount++;
        if (GuiDebug.showComponentBounds.get()) {
            debugCulled.add(component.getTransform().screenBounds());
        }
    }

    /**
     * Reports a component that was actually drawn. Feeds the counters and the debug overlay.
     */
    public void notifyRendered(GuiComponent component) {
        renderedCount++;
        if (GuiDebug.showComponentBounds.get()) {
            debugComponents.add(component.getTransform().screenBounds());
        }
    }

    /**
     * How many components were skipped this frame. Useful next to {@link #renderedCount()} to see
     * whether culling works.
     */
    public int culledCount() {
        return culledCount;
    }

    public int renderedCount() {
        return renderedCount;
    }

    /**
     * Draws the debug outlines collected this frame, on top of everything. Does nothing unless a switch
     * in GuiDebug is on.
     */
    public void flushDebugOverlay() {
        if (debugClips.isEmpty() && debugComponents.isEmpty() && debugCulled.isEmpty()) {
            return;
        }
        PoseStack pose = pose();
        pose.pushPose();
        pushZ();
        advanceZ(zStep * 64.0D);
        drawOutlines(debugCulled, GuiDebug.culledBoundsColor.get());
        drawOutlines(debugComponents, GuiDebug.componentBoundsColor.get());
        drawOutlines(debugClips, GuiDebug.clipBoundsColor.get());
        popZ();
        pose.popPose();
        debugClips.clear();
        debugComponents.clear();
        debugCulled.clear();
    }

    private void drawOutlines(List<Rectangle> rectangles, DLColor color) {
        for (int i = 0; i < rectangles.size(); i++) {
            Rectangle r = rectangles.get(i);
            GuiUtils.border(this, r.x(), r.y(), r.width(), r.height(), 1, color);
        }
    }

    private static boolean encloses(Rectangle outer, Rectangle inner) {
        return outer.left() <= inner.left()
            && outer.top() <= inner.top()
            && outer.right() >= inner.right()
            && outer.bottom() >= inner.bottom();
    }
}
