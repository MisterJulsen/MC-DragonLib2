package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.client.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.client.gui.v2.theme.WidgetState;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.AbstractContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.ContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.ContextMenuSeparator;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.DLContextMenu;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DLScrollBar extends GuiComponent {

    public interface ScrollBarWidgetRenderer extends WidgetRenderer<DLScrollBar> {
        record CalculationBorder(int top, int bottom) {
            public int sum() {
                return top + bottom;
            }
        }

        Insets getThumbBorder(DLScrollBar component);

        default CalculationBorder getCalculationBounds(DLScrollBar component) {
            Insets border = getThumbBorder(component);
            return switch (component.getOrientation()) {
                case HORIZONTAL -> new CalculationBorder((int)border.left(), (int)border.right());
                default -> new CalculationBorder((int)border.top(), (int)border.bottom());
            };
        }
    }

    public static class Renderer implements ScrollBarWidgetRenderer {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.VANILLA_SCROLLBAR, "inventory", Insets.of(1));
        public static final Renderer SIMPLE = new Renderer(DLTextureSheet.VANILLA_SCROLLBAR, "menu", Insets.of(0));

        protected static final String SPRITE_BACKGROUND = "background";
        protected static final String SPRITE_NORMAL = "normal";
        protected static final String SPRITE_PRESSED = "down_selected";
        protected static final String SPRITE_HOVERED = "selected";
        protected static final String SPRITE_DISABLED = "disabled";

        protected final DLTextureSheet spriteSheet;
        protected final String spritePrefix;
        protected final Insets border;

        private Renderer(DLTextureSheet spriteSheet, String spritePrefix, Insets border) {
            this.spriteSheet = spriteSheet;
            this.spritePrefix = spritePrefix;
            this.border = border;
        }

        @Override
        public Insets getThumbBorder(DLScrollBar component) {
            return border;
        }

        @Override
        public void render(DLGuiGraphics g, DLScrollBar component, double x, double y, double width, double height) {
            GuiUtils.fill(g, x, y, width, height, DLColor.CYAN);
            boolean hasButtons = component.showButtons.get();
            boolean isVertical = component.getOrientation() == Orientation.VERTICAL;
            double areaX = !isVertical && hasButtons ? x + BUTTON_SIZE : x;
            double areaY = isVertical && hasButtons ? y + BUTTON_SIZE : y;
            double areaW = width - (!isVertical && hasButtons ? BUTTON_SIZE * 2 : 0);
            double areaH = height - (isVertical && hasButtons ? BUTTON_SIZE * 2 : 0);

            GuiUtils.sprite(g, spriteSheet.getSprite(spritePrefix + "_" + SPRITE_BACKGROUND), areaX, areaY, areaW, areaH);

            if (!component.canScroll()) {
                return;
            }

            WidgetState state = WidgetState.of(component, component.isPressed(), component.isHovered());
            String spriteName = spritePrefix + "_" + (component.getOrientation() == Orientation.VERTICAL ? "vertical_" : "horizontal_") + switch (state) {
                case DISABLED -> SPRITE_DISABLED;
                case PRESSED -> SPRITE_PRESSED;
                case HOVERED -> SPRITE_HOVERED;
                default -> SPRITE_NORMAL;
            };

            double thumbSize = component.calcAutoThumbSize();
            double offset = component.getThumbOffset();
            Insets border = getThumbBorder(component);

            if (isVertical) {
                GuiUtils.sprite(g, spriteSheet.getSprite(spriteName), x + border.left(), y + offset, width - border.horizontal(), thumbSize);
            } else {
                GuiUtils.sprite(g, spriteSheet.getSprite(spriteName), x + offset, y + border.top(), thumbSize, height - border.vertical());
            }
        }
    }

    public record ValueChangedEvent(double value) implements IEvent {}
    public record MaxValueChangedEvent(int max) implements IEvent {}
    public record ScreenSizeChangedEvent(int size) implements IEvent {}
    public record ThumbSizeChangedEvent(int size) implements IEvent {}

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    public static final Theme.Key<ScrollBarWidgetRenderer> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".scrollbar.renderer", Renderer.STANDARD);
    public static final Theme.Key<WidgetRenderer<DLAbstractButton>> THEME_KEY_BUTTON_RENDERER = DLAbstractButton.THEME_KEY_RENDERER;
    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_TINT = Theme.key(DragonLib.MODID + ".scrollbar.background_tint", DLColor.WHITE);

    public static final int DEFAULT_SCROLLBAR_SIZE = 16;
    public static final int BUTTON_SIZE = 14;

    protected static final int MIN_THUMB_SIZE = 5;

    protected final DLButton scrollUpBtn;
    protected final DLButton scrollDownBtn;

    public final NumberProperty<Integer> thumbSize = new NumberProperty<>(0, 0, Integer.MAX_VALUE);
    public final NumberProperty<Integer> screenSize = new NumberProperty<>(50, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> stepSize = new NumberProperty<>(16, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> gridSize = new NumberProperty<>(0, 0, Integer.MAX_VALUE);
    public final NumberProperty<Integer> max = new NumberProperty<>(100, 0, Integer.MAX_VALUE);
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> 0D, () -> Math.max(0D, max.get() - screenSize.get()));
    public final BooleanProperty showButtons = new BooleanProperty(false);

    protected final Orientation orientation;
    protected int dragOffset;
    private boolean isDragging;

    public DLScrollBar(int x, int y, int length, Orientation orientation) {
        this(x, y, orientation == Orientation.HORIZONTAL ? length : DEFAULT_SCROLLBAR_SIZE, orientation == Orientation.VERTICAL ? length : DEFAULT_SCROLLBAR_SIZE, orientation);
    }

    public DLScrollBar(int x, int y, int w, int h, Orientation orientation) {
        super(x, y, w, h);
        this.orientation = orientation;
        clipsChildren.set(false);
        overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.FLAT_GRAY_RENDERER);
        addBehaviour(new DLContextMenu.Trigger(this::createContextMenu));

        // Buttons
        scrollUpBtn = createButton(true);
        addComponent(scrollUpBtn);
        scrollDownBtn = createButton(false);
        addComponent(scrollDownBtn);

        // Property Listeners
        thumbSize.addChangeListener((o, n) -> invokeEvent(this, new ThumbSizeChangedEvent(n), true));
        screenSize.addChangeListener((o, n) -> {
            clampValue();
            invokeEvent(this, new ScreenSizeChangedEvent(n), true);
        });
        showButtons.addChangeListener((o, n) -> {
            scrollDownBtn.visible.set(n);
            scrollUpBtn.visible.set(n);
        });
        max.addChangeListener((o, n) -> {
            clampValue();
            invokeEvent(this, new MaxValueChangedEvent(n), true);
        });
        value.addChangeListener((o, n) -> invokeEvent(this, new ValueChangedEvent(n), true));

        // Events
        addEventListener(GuiEvents.ScrollEvent.class, (s, e) -> {
            updateScrollValueOnScroll(-e.deltaX(), -e.deltaY());
            if (canScroll()) {
                e.consume();
            }
        });
        addEventListener(GuiEvents.DragEvent.class, (s, e) -> {
            if (e.leftClick()) {
                isDragging = true;
                updateScrollValueOnDrag(e.localX(s), e.localY(s));
                e.consume();
            }
        });
        addEventListener(GuiEvents.DragEndEvent.class, (s, e) -> {
            isDragging = false;
        });
        addEventListener(GuiEvents.MouseDownEvent.class, (s, e) -> {
            if (e.leftClick()) {
                updateScrollValueOnClick(e.localX(s), e.localY(s));
            }
            e.consume();
        });
        addEventListener(GuiEvents.BoundsChangedEvent.class, (src, event) -> {
            if (event.sizeChanged()) {
                layoutButtons();
            }
        });

        layoutButtons();
    }

    protected List<AbstractContextMenuItem> createContextMenu() {
        List<AbstractContextMenuItem> items = new ArrayList<>();
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar.here"))
                .enabled(() -> canScroll() && isEnabled())
                .clickAction((menu, localX, localY, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        updateScrollValueOnClick(localX, localY);
                    }
                })
                .build());
        items.add(new ContextMenuSeparator());
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "top" : "left")))
                .enabled(() -> canScroll() && isEnabled() && value.get() > 0D)
                .clickAction((menu, localX, localY, button) -> {
                    this.value.set(0D);
                })
                .build());
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "bottom" : "right")))
                .enabled(() -> canScroll() && isEnabled() && value.get() < max.get() - screenSize.get())
                .clickAction((menu, localX, localY, button) -> {
                    this.value.set((double)this.max.get());
                })
                .build());
        items.add(new ContextMenuSeparator());
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "page_up" : "page_left")))
                .enabled(() -> canScroll() && isEnabled() && value.get() > 0D)
                .clickAction((menu, localX, localY, button) -> {
                    this.value.set(this.value.get() - this.screenSize.get());
                })
                .build());
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "page_down" : "page_right")))
                .enabled(() -> canScroll() && isEnabled() && value.get() < max.get() - screenSize.get())
                .clickAction((menu, localX, localY, button) -> {
                    this.value.set(this.value.get() + this.screenSize.get());
                })
                .build());
        items.add(new ContextMenuSeparator());
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "step_up" : "step_left")))
                .enabled(() -> canScroll() && isEnabled() && value.get() > 0D)
                .clickAction((menu, localX, localY, button) -> {
                    this.value.set(this.value.get() - this.stepSize.get());
                })
                .build());
        items.add(new ContextMenuItem.Builder()
                .text(TextUtils.translate("gui." + DragonLib.MODID + ".scrollbar." + (orientation == Orientation.VERTICAL ? "step_down" : "step_right")))
                .enabled(() -> canScroll() && isEnabled() && value.get() < max.get() - screenSize.get())
                .clickAction((menu, localX, localY, button) -> {
                    this.value.set(this.value.get() + this.stepSize.get());
                })
                .build());

        return items;
    }

    @Override
    public CursorType getCurrentCursor() {
        if (isDragging) {
            return orientation == Orientation.HORIZONTAL ? CursorType.HRESIZE : CursorType.VRESIZE;
        }
        return super.getCurrentCursor();
    }

    protected void layoutButtons() {
        boolean vertical = orientation == Orientation.VERTICAL;
        double across = vertical ? dWidth() : dHeight();
        double btnW = vertical ? across : BUTTON_SIZE;
        double btnH = vertical ? BUTTON_SIZE : across;

        scrollUpBtn.setBounds(0, 0, btnW, btnH);
        scrollDownBtn.setBounds(vertical ? 0 : dWidth() - BUTTON_SIZE, vertical ? dHeight() - BUTTON_SIZE : 0, btnW, btnH);
    }

    protected DLButton createButton(boolean downBtn) {
        DLButton button = new DLButton(0, 0, BUTTON_SIZE, BUTTON_SIZE); // placeholder size
        button.text.set(TextUtils.empty());
        button.icon.set((orientation == Orientation.VERTICAL ? (downBtn ? GuiIcons.ARROW_UP : GuiIcons.ARROW_DOWN) : (downBtn ? GuiIcons.ARROW_LEFT : GuiIcons.ARROW_RIGHT)).getAsSprite(16, 16));
        button.visible.set(showButtons.get());
        button.addEventListener(GuiEvents.MouseHoldDownEvent.class, (src, event) -> {
            if (event.leftClick()) {
                value.set(value.get() + (downBtn ? -stepSize.get() : stepSize.get()));
            }
        });
        return button;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public DLButton getScrollUpButton() {
        return scrollUpBtn;
    }

    public DLButton getScrollDownButton() {
        return scrollDownBtn;
    }

    public boolean canScroll() {
        return max.get() > screenSize.get();
    }

    protected int getScrollAreaSize() {
        return switch (orientation) {
            case HORIZONTAL -> width() - getThemeValue(THEME_KEY_RENDERER).getCalculationBounds(this).sum() - (showButtons.get() ? BUTTON_SIZE * 2 : 0);
            case VERTICAL -> height() - getThemeValue(THEME_KEY_RENDERER).getCalculationBounds(this).sum() - (showButtons.get() ? BUTTON_SIZE * 2 : 0);
        };
    }

    protected int getScrollAreaOffset() {
        return getThemeValue(THEME_KEY_RENDERER).getCalculationBounds(this).top() + (showButtons.get() ? BUTTON_SIZE : 0);
    }

    public int calcAutoThumbSize() {
        if (!canScroll()) {
            return 0;
        }
        if (thumbSize.get() > 0) {
            return thumbSize.get();
        }

        int area = getScrollAreaSize();
        double ratio = (double)screenSize.get() / (double)Math.max(1, max.get());
        int size = (int)((double)area * ratio);
        return Math.max(MIN_THUMB_SIZE, Math.min(area, size));
    }

    public int getThumbOffset() {
        double range = scrollRange();
        if (range <= 0) {
            return getScrollAreaOffset();
        }
        return (int)Math.ceil((double)(getScrollAreaSize() - calcAutoThumbSize()) / range * value.get()) + getScrollAreaOffset();
    }

    public double scrollRange() {
        return Math.max(0, max.get() - screenSize.get());
    }

    protected void clampValue() {
        double clamped = Math.max(0D, Math.min(value.get(), scrollRange()));
        if (clamped != value.get()) {
            value.set(clamped);
        }
    }

    protected void updateScrollValueOnScroll(double deltaX, double deltaY) {
        if (!canScroll()) {
            value.set(0D);
            return;
        }

        double next = switch (orientation) {
            case VERTICAL -> value.get() + deltaY * stepSize.get();
            case HORIZONTAL -> value.get() + deltaX * stepSize.get();
        };
        value.set(snapToGrid(next));
    }

    protected void updateScrollValueOnDrag(double localX, double localY) {
        if (!canScroll()) {
            value.set(0D);
            return;
        }

        int thumbSize = calcAutoThumbSize();
        int area = getScrollAreaSize();
        if (scrollRange() <= 0 || area - thumbSize <= 0) {
            value.set(0D);
            return;
        }

        double posPixel = switch (orientation) {
            case VERTICAL -> localY - getThemeValue(THEME_KEY_RENDERER).getCalculationBounds(this).top() - dragOffset - getScrollAreaOffset();
            case HORIZONTAL -> localX - getThemeValue(THEME_KEY_RENDERER).getCalculationBounds(this).top() - dragOffset - getScrollAreaOffset();
        };
        value.set(snapToGrid(scrollRange() / (double)(area - thumbSize) * posPixel));
    }

    protected void calculateDragOffset(double localX, double localY) {
        int thumbSize = calcAutoThumbSize();
        int area = getScrollAreaSize();
        double range = scrollRange();
        if (range <= 0 || area - thumbSize <= 0) {
            dragOffset = (int)(thumbSize / 2D);
            return;
        }

        int d = (int)((double)(area - thumbSize) / range * value.get());
        dragOffset = (int)((orientation == Orientation.VERTICAL ? localY : localX) - d - getScrollAreaOffset());
    }

    protected void updateScrollValueOnClick(double localX, double localY) {
        calculateDragOffset(localX, localY);
        int scroller = calcAutoThumbSize();
        if (dragOffset < 0 || dragOffset > scroller) {
            dragOffset = (int)(scroller / 2D);
            scrollTo(orientation == Orientation.VERTICAL ? localY : localX);
        }
    }

    public void scrollTo(double position) {
        if (!canScroll()) {
            value.set(0D);
            return;
        }

        int thumbSize = calcAutoThumbSize();
        int area = getScrollAreaSize();
        if (scrollRange() <= 0 || area - thumbSize <= 0) {
            value.set(0D);
            return;
        }

        double pixelCenter = position - getScrollAreaOffset() - thumbSize * 0.5D;
        value.set(snapToGrid(scrollRange() / (double)(area - thumbSize) * pixelCenter));
    }

    protected double snapToGrid(double raw) {
        return gridSize.get() > 0 ? Math.round(raw / (double)gridSize.get()) * (double)gridSize.get() : raw;
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.withTint(getThemeValue(THEME_KEY_BACKGROUND_TINT), () -> {
            getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
        });
    }
}
