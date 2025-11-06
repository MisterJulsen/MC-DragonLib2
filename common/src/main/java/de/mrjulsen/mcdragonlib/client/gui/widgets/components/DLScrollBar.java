package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaContainerScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.InheritableProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

/**
 * DLScrollBar is a GUI component that provides scroll functionality similar to
 * traditional scrollbars.
 * <p>
 * This component can be used to enable vertical or horizontal scrolling in UI
 * environments where full control over appearance and behavior is desired.
 * It offers standard scrollbar functionality, including a draggable thumb,
 * increment/decrement buttons, and scroll event handling, all implemented independently.
 * </p>
 *
 * <h4>Key Characteristics:</h4>
 * <ul>
 *   <li>Custom implementation — does not inherit from any existing scrollbar class.</li>
 *   <li>Supports both vertical and horizontal orientation.</li>
 *   <li>Scroll range, thumb size, and position are fully controllable.</li>
 * </ul>
 */
@SupportsEvents({
    DLScrollBar.BackgroundColorChangedEvent.class,
    DLScrollBar.ValueChangedEvent.class,
    DLScrollBar.MaxValueChangedEvent.class,
    DLScrollBar.ScreenSizeChangedEvent.class,
    DLScrollBar.ScrollerSizeChangedEvent.class,
})
public class DLScrollBar extends DLGuiComponent {

    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}
    public record ValueChangedEvent(double value) implements IEvent {}
    public record MaxValueChangedEvent(int max) implements IEvent {}
    public record ScreenSizeChangedEvent(int size) implements IEvent {}
    public record ScrollerSizeChangedEvent(int size) implements IEvent {}

    /**
     * The orientation of the scrollbar.
     */
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    public static enum ScrollBarState {
        BACKGROUND,
        SCROLLER_VERTICAL_NORMAL,
        SCROLLER_VERTICAL_SELECTED,
        SCROLLER_VERTICAL_DOWN_SELECTED,
        SCROLLER_VERTICAL_DISABLED,
        SCROLLER_HORIZONTAL_NORMAL,
        SCROLLER_HORIZONTAL_SELECTED,
        SCROLLER_HORIZONTAL_DOWN_SELECTED,
        SCROLLER_HORIZONTAL_DISABLED,
    }

    /**
     * The default width/height (depending on the orientation) of the scrollbar if {@link DLScrollBar#DLScrollBar(int, int, int, Orientation)} is used.
     */
    public static final int DEFAULT_SCROLLBAR_SIZE = 16;
    public static final int SCROLL_AREA_BORDER = 0;
    public static final int BUTTON_SIZE = 14;

    /**
     * The direction of the scrollbar.
     */
    protected final Orientation orientation;
    protected int dragOffset;

    protected DLButton scrollUpBtn;
    protected DLButton scrollDownBtn;

    /**
     * The size of the scroller in pixels. Set to {@code 0} for automatic scroller height depending on {@link #max} and the {@link #screenSize} and component size.
     */
    public final NumberProperty<Integer> scrollerSize = new NumberProperty<Integer>(0, 0, Integer.MAX_VALUE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new ScrollerSizeChangedEvent(a.intValue()), true));
    /**
     * The size of one screen in pixels.
     */
    public final NumberProperty<Integer> screenSize = new NumberProperty<Integer>(50, 1, Integer.MAX_VALUE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new ScreenSizeChangedEvent(a.intValue()), true));
    /**
     * The amount of pixels scrolled with each mouse scroll input.
     */
    public final NumberProperty<Integer> scrollSteps = new NumberProperty<Integer>(16, 1, Integer.MAX_VALUE);
    /**
     * The grid in which is scrolled. In other words, if the grid size is set to {@code x}, the scroll values can only be a multiple of {@code x}.
     */
    public final NumberProperty<Integer> gridSteps = new NumberProperty<Integer>(0, 0, Integer.MAX_VALUE);
    /**
     * The maximum scroll value. This value is <b>NOT</b> the maximum size of the container {@code x} that is scrolled, but the amount of pixels that do not fit into one {@link #screenSize} ({@code x - screenSize}). 
     */
    public final NumberProperty<Integer> max = new NumberProperty<Integer>(100, 0, Integer.MAX_VALUE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new MaxValueChangedEvent(a.intValue()), true));
    /**
     * The current scroll value.
     */
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> 0D, () -> (double)max.get())
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new ValueChangedEvent(a.doubleValue()), true));
    /**
     * Shows two arrow buttons on both ends of the scrollbar, which can be used to change the value gradually.
     */
    public final BooleanProperty showButtons = new BooleanProperty(false, false)
        .withAfterPropertyChangedCallback((o, a) -> {
            if (scrollUpBtn != null) scrollUpBtn.visible.set(a);
            if (scrollDownBtn != null) scrollDownBtn.visible.set(a);
        });
    /**
     * The background tint color.
     */
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty backgroundTint = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLScrollBar.BackgroundColorChangedEvent(a), true));

    public final Property<IStateRenderer<ScrollBarState>> componentRenderer = new Property<>(VanillaContainerScrollBarRenderer.VANILLA_SCROLLBAR);
    public final Property<IStateRenderer<DLButton.ButtonState>> buttonsComponentRenderer = new Property<>(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);

    /**
     * Creates a new {@link DLScrollBar} with default width/height (depending on the orientation).
     * @param x The {@code x} position of the component.
     * @param y The {@code y} position of the component.
     * @param s The {@code size} of the component.
     * @param orientation The variant of the scrollbar.
     */
    public DLScrollBar(int x, int y, int s, Orientation orientation) {
        this(x, y, orientation == Orientation.HORIZONTAL ? s : DEFAULT_SCROLLBAR_SIZE, orientation == Orientation.VERTICAL ? s : DEFAULT_SCROLLBAR_SIZE, orientation);
    }

    /**
     * Creates a new {@link DLScrollBar}.
     * @param x The {@code x} position of the component.
     * @param y The {@code y} position of the component.
     * @param w The {@code width} position of the component.
     * @param h The {@code height} position of the component.
     * @param orientation The variant of the scrollbar.
     */
    public DLScrollBar(int x, int y, int w, int h, Orientation orientation) {
        super(x, y, w, h);
        this.orientation = orientation;

        DLContextMenu contextMenu = new DLContextMenu((pX, pY) -> {
            List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar.here"), DLSprite.empty(), true, () -> {
                scrollTo(pY - getYOnScreen());
            }, null));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "top" : "left")), DLSprite.empty(), true, () -> {
                this.value.set(0D);
            }, null));
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "bottom" : "right")), DLSprite.empty(), true, () -> {
                this.value.set(this.max.get().doubleValue());
            }, null));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "page_up" : "page_left")), DLSprite.empty(), true, () -> {
                this.value.set(this.value.get() - this.screenSize.get());
            }, null));
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "page_down" : "page_right")), DLSprite.empty(), true, () -> {
                this.value.set(this.value.get() + this.screenSize.get());
            }, null));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "step_up" : "step_left")), DLSprite.empty(), true, () -> {
                this.value.set(this.value.get() - this.scrollSteps.get());
            }, null));
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "step_down" : "step_right")), DLSprite.empty(), true, () -> {
                this.value.set(this.value.get() + this.scrollSteps.get());
            }, null));
            return entries;
        });

        addEventListener(DLGuiStandardEvents.RightClickEvent.class, (src, event) -> {
            contextMenu.open(getWindowManager(), (int)getWindowManager().mouseXOnScreen(), (int)getWindowManager().mouseYOnScreen());
            return false;
        });

        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (src, event) -> {
            this.updateScrollValueOnScroll(event.deltaX(), event.deltaY());
            return false;
        });
        addEventListener(DLGuiStandardEvents.DragEvent.class, (src, event) -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.updateScrollValueOnDrag(event.mouseX(), event.mouseY());
            }
            return false;
        });
        addEventListener(DLGuiStandardEvents.MouseDownEvent.class, (src, event) -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.updateScrollValueOnClick(event.mouseX(), event.mouseY());
            }
            return false;
        });

        scrollUpBtn = new DLButton(0, 0, orientation == Orientation.HORIZONTAL ? BUTTON_SIZE : w, orientation == Orientation.VERTICAL ? BUTTON_SIZE : h);
        scrollUpBtn.text.set(TextUtils.text(orientation == Orientation.VERTICAL ? "▲" : "◀"));
        scrollUpBtn.componentRenderer.set(buttonsComponentRenderer.get());
        scrollUpBtn.visible.set(showButtons.get());
        scrollUpBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.value.set(this.value.get() - scrollSteps.get());
            }
            return false;
        });
        addComponent(scrollUpBtn);

        scrollDownBtn = new DLButton(orientation == Orientation.HORIZONTAL ? width() - BUTTON_SIZE : 0, orientation == Orientation.VERTICAL ? height() - BUTTON_SIZE : 0, orientation == Orientation.HORIZONTAL ? BUTTON_SIZE : w, orientation == Orientation.VERTICAL ? BUTTON_SIZE : h);
        scrollDownBtn.text.set(TextUtils.text(orientation == Orientation.VERTICAL ? "▼" : "▶"));
        scrollDownBtn.componentRenderer.set(buttonsComponentRenderer.get());
        scrollDownBtn.visible.set(showButtons.get());
        scrollDownBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.value.set(this.value.get() + scrollSteps.get());
            }
            return false;
        });
        addComponent(scrollDownBtn);

        buttonsComponentRenderer.withAfterPropertyChangedCallback((o, val) -> {
            scrollUpBtn.componentRenderer.set(val);
            scrollDownBtn.componentRenderer.set(val);
        });
    }

    protected int getScrollAreaSize() {
        return switch (orientation) {
            case HORIZONTAL -> width() - (SCROLL_AREA_BORDER * 2) - (showButtons.get() ? BUTTON_SIZE * 2 : 0);
            case VERTICAL -> height() - (SCROLL_AREA_BORDER * 2) - (showButtons.get() ? BUTTON_SIZE * 2 : 0);
        };
    }

    protected int getScrollAreaOffset() {
        return SCROLL_AREA_BORDER + (showButtons.get() ? BUTTON_SIZE : 0);
    }

    protected int calculateAutoScrollerSize() {
        int scrollerSize = this.scrollerSize.get();
        if (scrollerSize > 0) {
            return scrollerSize;
        }
        double screenSizes = Math.min((double)screenSize.get() / (double)max.get(), 1);
        int area = getScrollAreaSize();
        switch (orientation) {
            case VERTICAL -> {
                if (scrollerSize <= 0) {
                    scrollerSize = (int)((double)area * screenSizes);
                }
            }
            case HORIZONTAL -> {
                if (scrollerSize <= 0) {
                    scrollerSize = (int)((double)area * screenSizes);
                }
            }
        }
        return Math.max(5, scrollerSize);
    }

    protected void updateScrollValueOnScroll(double deltaX, double deltaY) {        
        this.value.set(switch (orientation) {
            case VERTICAL -> this.value.get() + deltaY * scrollSteps.get();
            case HORIZONTAL -> this.value.get() + deltaX * scrollSteps.get();
        });
    }

    protected void updateScrollValueOnDrag(double mouseX, double mouseY) {
        int scrollerSize = calculateAutoScrollerSize();
        int area = getScrollAreaSize();
        switch (orientation) {
            case VERTICAL -> {
                double value = (double)max.get() / (area - scrollerSize) * (mouseY - SCROLL_AREA_BORDER - dragOffset - getScrollAreaOffset());
                if (gridSteps.get() > 0) {
                    value = Math.round(value / gridSteps.get().doubleValue()) * gridSteps.get().doubleValue();
                }
                this.value.set(value);
            }
            case HORIZONTAL -> {
                double value = (double)max.get() / (area - scrollerSize) * (mouseX - SCROLL_AREA_BORDER - dragOffset - getScrollAreaOffset());
                if (gridSteps.get() > 0) {
                    value = Math.round(value / gridSteps.get().doubleValue()) * gridSteps.get().doubleValue();
                }
                this.value.set(value);
            }
        }
    }

    protected void calculateDragOffset(double mouseX, double mouseY) {
        int scrollerSize = calculateAutoScrollerSize();
        int area = getScrollAreaSize();
        switch (orientation) {
            case VERTICAL -> {
                int d = (int)((double)(area - scrollerSize) / max.get() * value.get());
                dragOffset = (int)(mouseY - d - getScrollAreaOffset());
            }
            case HORIZONTAL -> {
                int d = (int)((double)(area - scrollerSize) / max.get() * value.get());
                dragOffset = (int)(mouseX - d - getScrollAreaOffset());
            }
        }
    }

    protected void updateScrollValueOnClick(double mouseX, double mouseY) {
        calculateDragOffset(mouseX, mouseY);
        int scrollerSize = calculateAutoScrollerSize();
        if (dragOffset < 0 || dragOffset > scrollerSize) {
            this.dragOffset = (int)(scrollerSize / 2D);
            scrollTo(orientation == Orientation.VERTICAL ? mouseY : mouseX);
        }
    }

    public void scrollTo(double position) {
        int scrollerSize = calculateAutoScrollerSize();
        int area = getScrollAreaSize();
        switch (orientation) {
            case VERTICAL -> {
                double value = (double)max.get() / (area - scrollerSize) * (position - getScrollAreaOffset() - scrollerSize * 0.5D);
                if (gridSteps.get() > 0) {
                    value = Math.round(value / gridSteps.get().doubleValue()) * gridSteps.get().doubleValue();
                }
                this.value.set(value);
            }
            case HORIZONTAL -> {
                double value = (double)max.get() / (area - scrollerSize) * (position - getScrollAreaOffset() - scrollerSize * 0.5D);
                if (gridSteps.get() > 0) {
                    value = Math.round(value / gridSteps.get().doubleValue()) * gridSteps.get().doubleValue();
                }
                this.value.set(value);
            }
        }
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.setTint(backgroundTint.get());
        componentRenderer.get().renderSprite(
            graphics,
            (orientation == Orientation.HORIZONTAL && showButtons.get()) ? BUTTON_SIZE : 0,
            (orientation == Orientation.VERTICAL && showButtons.get()) ? BUTTON_SIZE : 0,
            width() - ((orientation == Orientation.HORIZONTAL && showButtons.get()) ? BUTTON_SIZE * 2 : 0),
            height() - ((orientation == Orientation.VERTICAL && showButtons.get()) ? BUTTON_SIZE * 2 : 0),
            this,
            ScrollBarState.BACKGROUND
        );

        int scrollerSize = calculateAutoScrollerSize();
        int area = getScrollAreaSize();

        ScrollBarState state;

        switch (orientation) {
            case VERTICAL -> {
                state = ScrollBarState.SCROLLER_VERTICAL_NORMAL;
                if (!enabled.get()) {
                    state = ScrollBarState.SCROLLER_VERTICAL_DISABLED;
                } else if (isMouseDown() && getWindowManager().getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    state = ScrollBarState.SCROLLER_VERTICAL_DOWN_SELECTED;
                } else if (isSelected()) {
                    state = ScrollBarState.SCROLLER_VERTICAL_SELECTED;
                }
                int d = (int)((double)(area - scrollerSize) / max.get() * value.get());
                componentRenderer.get().renderSprite(graphics, SCROLL_AREA_BORDER, d + getScrollAreaOffset(), width() - SCROLL_AREA_BORDER * 2, scrollerSize, this, state);
            }
            case HORIZONTAL -> {
                state = ScrollBarState.SCROLLER_HORIZONTAL_NORMAL;
                if (!enabled.get()) {
                    state = ScrollBarState.SCROLLER_HORIZONTAL_DISABLED;
                } else if (isMouseDown() && getWindowManager().getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    state = ScrollBarState.SCROLLER_HORIZONTAL_DOWN_SELECTED;
                } else if (isSelected()) {
                    state = ScrollBarState.SCROLLER_HORIZONTAL_SELECTED;
                }
                int d = (int)((double)(area - scrollerSize) / max.get() * value.get());
                componentRenderer.get().renderSprite(graphics, d + getScrollAreaOffset(), SCROLL_AREA_BORDER, scrollerSize, height() - SCROLL_AREA_BORDER * 2, this, state);
            }
        }

        GuiUtils.resetTint();
    }
}
