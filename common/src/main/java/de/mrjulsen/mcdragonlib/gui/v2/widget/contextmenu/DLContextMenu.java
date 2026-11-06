package de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.AbstractBehaviour;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.input.Hotkey;
import de.mrjulsen.mcdragonlib.gui.v2.input.Hotkeys;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.gui.v2.window.Popup;
import de.mrjulsen.mcdragonlib.gui.v2.window.WindowManager;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DLContextMenu extends GuiComponent {

    public static final List<AbstractContextMenuItem> NO_ITEMS = List.of();

    public static class Trigger extends AbstractBehaviour {

        private final Supplier<List<AbstractContextMenuItem>> entries;
        private DLContextMenu menu;

        public Trigger(Supplier<List<AbstractContextMenuItem>> entries) {
            this.entries = entries;
        }

        @Override
        protected void onAttached(GuiComponent host) {
            listen(GuiEvents.RightClickEvent.class, (s, e) -> {
                closeMenu();
                menu = DLContextMenu.openMenu(host, e.screenX(), e.screenY(), menu -> {
                    menu.items.addAll(entries.get());
                });
            });
        }

        @Override
        protected void onDetached(GuiComponent host) {
            closeMenu();
        }

        private void closeMenu() {
            if (menu == null) {
                return;
            }
            menu.dismiss();
            menu = null;
        }
    }

    public static class Renderer implements WidgetRenderer<DLContextMenu> {

        @Override
        public void render(DLGuiGraphics g, DLContextMenu component, double x, double y, double width, double height) {
            renderMenu(g, component, x, y, width, height);

            int borderSize = component.getThemeValue(THEME_KEY_BORDER_SIZE);
            Insets margin = component.getThemeValue(THEME_KEY_ITEM_MARGIN);

            double itemsX = borderSize + margin.left();
            double itemsY = component.itemsTop();
            double itemsW = width - (margin.horizontal() + borderSize * 2);
            double itemsH = component.getViewportHeight();

            if (itemsW <= 0 || itemsH <= 0) {
                return;
            }

            g.withClip(component, itemsX, itemsY, itemsW, itemsH, () -> {
                double rowY = itemsY;
                for (int i = component.getIndexOffset(); i < component.getVisibleEndIndex(); i++) {
                    AbstractContextMenuItem item = component.items.get(i);
                    double itemH = component.itemHeight(item);
                    g.pushLocalSpace(itemsX, rowY);
                    renderItem(g, component, item, i, component.getItemLayout(), itemsW, itemH);
                    g.popLocalSpace();
                    rowY += itemH;
                }
            });
        }

        public void renderMenu(DLGuiGraphics g, DLContextMenu component, double x, double y, double width, double height) {
            int borderSize = component.getThemeValue(THEME_KEY_BORDER_SIZE);
            Insets margin = component.getThemeValue(THEME_KEY_ITEM_MARGIN);

            GuiUtils.box(g, x, y, width, height, component.getThemeValue(THEME_KEY_BACKGROUND_COLOR), component.getThemeValue(THEME_KEY_BORDER_COLOR));

            if (component.isScrollable()) {
                double topY = borderSize + margin.top();
                double bottomY = height - borderSize - margin.bottom() - SCROLL_AREA_SIZE;
                DLColor active = component.getThemeValue(Theme.TEXT);
                DLColor inactive = component.getThemeValue(Theme.TEXT_INVERTED);
                int iconX = (int)(width - GuiIcons.ICON_SIZE) / 2;

                GuiUtils.withTint(component.canScrollUp() ? active : inactive, () -> {
                    GuiIcons.ARROW_UP.render(g.legacy(), iconX, (int) (topY + (double) (SCROLL_AREA_SIZE - GuiIcons.ICON_SIZE) / 2));
                });
                GuiUtils.withTint(component.canScrollDown() ? active : inactive, () -> {
                    GuiIcons.ARROW_DOWN.render(g.legacy(), iconX, (int)(bottomY + (double)(SCROLL_AREA_SIZE - GuiIcons.ICON_SIZE) / 2));
                });
            }
        }

        public void renderItem(DLGuiGraphics g, DLContextMenu component, AbstractContextMenuItem item, int itemIndex, ItemLayout layout, double width, double height) {
            boolean isSelected = itemIndex == component.getSelectedIndex();
            if (item.isEnabled() && isSelected) {
                GuiUtils.fill(g, 1, 0, width - 2, 1, component.getThemeValue(DLContextMenu.THEME_KEY_HIGHLIGHT_COLOR));
                GuiUtils.fill(g, 0, 1, width, height - 2, component.getThemeValue(DLContextMenu.THEME_KEY_HIGHLIGHT_COLOR));
                GuiUtils.fill(g, 1, height - 1, width - 2, 1, component.getThemeValue(DLContextMenu.THEME_KEY_HIGHLIGHT_COLOR));
            }

            if (item instanceof ContextMenuItem i) {
                boolean checked = i.getChecked().getAsBoolean();
                boolean showIcon = i.getIcon() != null && !i.getIcon().isEmpty() && (!checked || layout.hasCheckMargin());
                GuiUtils.withTint(item.isEnabled() ? component.getThemeValue(Theme.TEXT) : component.getThemeValue(Theme.TEXT_INVERTED), () -> {
                    if (showIcon) {
                        i.getIcon().render(g.legacy(), layout.iconX() + ((layout.iconMarginW() - i.getIcon().getWidth()) / 2), (int)((height - i.getIcon().getHeight()) / 2));
                    }
                    if (checked) {
                        GuiIcons icon = switch (i.getCheckType()) {
                            case RADIO_BUTTON -> GuiIcons.BULLET_S;
                            default -> GuiIcons.CHECK_S;
                        };
                        icon.render(g.legacy(), layout.checkMarkX() + ((layout.checkMarkMarginW() - GuiIcons.ICON_SIZE) / 2), (int)((height - GuiIcons.ICON_SIZE) / 2));
                    }
                    if (i.getSubItems().length > 0) {
                        Insets padding = component.getThemeValue(THEME_KEY_ITEM_PADDING);
                        GuiIcons.OPEN_ARROW_RIGHT.render(g.legacy(), (int)(width - padding.right() - SUB_ITEM_ARROW_SIZE + (double)(SUB_ITEM_ARROW_SIZE - GuiIcons.ICON_SIZE) / 2), (int)((height - GuiIcons.ICON_SIZE) / 2));
                    }
                });
                GuiUtils.text(g, i.getText(), layout.textX(), TextUtils.centerTextY(g.font(), height), item.isEnabled() ? component.getThemeValue(Theme.TEXT) : component.getThemeValue(Theme.TEXT_INVERTED), false);
                renderHotkey(g, component, i, layout, height);
            } else if (item instanceof ContextMenuSeparator i) {
                GuiUtils.fill(g, 3, component.getThemeValue(DLContextMenu.THEME_KEY_ITEM_MARGIN).top(), width - 6, 1, component.getThemeValue(Theme.TEXT_INVERTED));
            }
        }

        public void renderHotkey(DLGuiGraphics g, DLContextMenu component, ContextMenuItem item, ItemLayout layout, double height) {
            String hotkey = component.hotkeyText(item);
            if (hotkey == null) {
                return;
            }
            double x = layout.textX() + layout.textMarginW() - TextUtils.textWidth(g.font(), hotkey);
            DLColor color = component.getThemeValue(item.isEnabled() ? THEME_KEY_HOTKEY_COLOR : Theme.TEXT_INVERTED);
            GuiUtils.text(g, hotkey, x, TextUtils.centerTextY(g.font(), height), color, false);
        }
    }

    public record ItemLayout(int checkX, int iconX, int textX, int checkMarginW, int iconMarginW, int textMarginW, int arrowMarginW, int totalWidth) {

        public boolean hasCheckMargin() {
            return checkMarginW > 0;
        }

        public int checkMarkX() {
            return hasCheckMargin() ? checkX : iconX;
        }

        public int checkMarkMarginW() {
            return hasCheckMargin() ? checkMarginW : iconMarginW;
        }
    }


    public static final int INVALID = -1;
    public static final int SUB_ITEM_ARROW_SIZE = 8;
    public static final int SCROLL_AREA_SIZE = 16;
    public static final int SCROLL_UP = -1;
    public static final int SCROLL_NONE = 0;
    public static final int SCROLL_DOWN = 1;
    public static final Renderer RENDERER = new Renderer();

    public static final Theme.Key<WidgetRenderer<DLContextMenu>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".contextmenu.renderer", RENDERER);
    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_COLOR = Theme.key(DragonLib.MODID + ".contextmenu.background_color", DLColor.fromInt(0xF416161A));
    public static final Theme.Key<DLColor> THEME_KEY_BORDER_COLOR = Theme.key(DragonLib.MODID + ".contextmenu.border_color", DLColor.fromInt(0xFF606068));
    public static final Theme.Key<DLColor> THEME_KEY_HIGHLIGHT_COLOR = Theme.key(DragonLib.MODID + ".contextmenu.highlight_color", DLColor.fromInt(0xFF3A548C));
    public static final Theme.Key<Integer> THEME_KEY_BORDER_SIZE = Theme.key(DragonLib.MODID + ".contextmenu.border_size", 1);
    public static final Theme.Key<Insets> THEME_KEY_ITEM_MARGIN = Theme.key(DragonLib.MODID + ".contextmenu.item_margin", Insets.of(2, 2));
    public static final Theme.Key<Insets> THEME_KEY_ITEM_PADDING = Theme.key(DragonLib.MODID + ".contextmenu.item_padding", Insets.of(2, 3));
    public static final Theme.Key<Integer> THEME_KEY_SUBMENU_OVERLAP = Theme.key(DragonLib.MODID + ".contextmenu.submenu_overlap", 3);
    public static final Theme.Key<Integer> THEME_KEY_SCROLL_DELAY = Theme.key(DragonLib.MODID + ".contextmenu.scroll_delay", 6);
    public static final Theme.Key<Integer> THEME_KEY_SCROLL_INTERVAL = Theme.key(DragonLib.MODID + ".contextmenu.scroll_interval", 2);
    public static final Theme.Key<Integer> THEME_KEY_TEXT_GAP = Theme.key(DragonLib.MODID + ".contextmenu.text_gap", 12);
    public static final Theme.Key<Integer> THEME_KEY_CHECK_MARGIN_SIZE = Theme.key(DragonLib.MODID + ".contextmenu.check_margin_size", 12);
    public static final Theme.Key<Integer> THEME_KEY_MIN_ICON_MARGIN_SIZE = Theme.key(DragonLib.MODID + ".contextmenu.min_icon_margin_size", 12);
    public static final Theme.Key<DLColor> THEME_KEY_HOTKEY_COLOR = Theme.key(DragonLib.MODID + ".contextmenu.hotkey_color", DLColor.fromInt(0xFF969696));

    public final ListProperty<AbstractContextMenuItem> items = new ListProperty<AbstractContextMenuItem>(NO_ITEMS)
        .withUpdateCallback((value, operation) -> onItemsChanged());
    public final BooleanProperty showCheckMargin = new BooleanProperty(false);

    private final Popup popup;
    private final GuiComponent host;
    private final GuiComponent scope;

    private int selectedIndex = INVALID;
    private DLContextMenu childMenu = null;
    private int selectedSubMenuIndex = INVALID;
    private ItemLayout itemLayout = new ItemLayout(0, 0, 0, 0, 0, 0, 0, 0);

    private boolean scrollable = false;
    private int indexOffset = 0;
    private int maxIndexOffset = 0;
    private int visibleEndIndex = 0;
    private double viewportHeight = 0;
    private double heightLimit = Double.POSITIVE_INFINITY;
    private boolean pointerInside = false;
    private double pointerLocalX = 0;
    private double pointerLocalY = 0;
    private int scrollDirection = SCROLL_NONE;
    private int scrollTicks = 0;

    public DLContextMenu(GuiComponent host) {
        super(0, 0, 80, 100);
        this.host = host;
        this.scope = host instanceof DLContextMenu parentMenu ? parentMenu.scope : host;
        this.popup = new Popup(this);

        themeAnchor.set(host);
        focusable.set(true);
        widthMode.set(SizeMode.AUTO);
        heightMode.set(SizeMode.AUTO);

        addEventListener(GuiEvents.MouseEnterEvent.class, (s, e) -> setPointer(e.localX(s), e.localY(s)));
        addEventListener(GuiEvents.MouseMoveEvent.class, (s, e) -> {
            setPointer(e.localX(s), e.localY(s));
            int idx = indexAt(pointerLocalY);
            if (idx != INVALID || childMenu == null) {
                select(idx);
            }
            manageSubMenu();
        });
        addEventListener(GuiEvents.MouseLeaveEvent.class, (s, e) -> {
            pointerInside = false;
            stopAutoScroll();
            if (childMenu == null) {
                selectedIndex = INVALID;
            }
        });
        addEventListener(GuiEvents.TickEvent.class, (s, e) -> tickAutoScroll());
        addEventListener(GuiEvents.KeyPressEvent.class, (s, e) -> {
            if (triggerHotkey(e.keyCode(), e.modifiers())) {
                e.consume();
            }
        });
        addEventListener(GuiEvents.ScrollEvent.class, (s, e) -> {
            if (scrollBy(e.deltaY() > 0 ? SCROLL_UP : SCROLL_DOWN)) {
                e.consume();
            }
        });
        addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
            double localMouseX = e.localX(s);
            double localMouseY = e.localY(s);
            if (!isMouseInBounds(localMouseX, localMouseY)) {
                return;
            }

            int zone = scrollZoneAt(localMouseX, localMouseY);
            if (zone != SCROLL_NONE) {
                scrollBy(zone);
                return;
            }

            int idx = indexAt(localMouseY);
            if (idx == INVALID) {
                return;
            }
            AbstractContextMenuItem item = items.get(idx);
            if (item == null || !item.isEnabled()) {
                return;
            }

            DLUtils.doIfNotNull(item.getClickAction(), x -> x.click(this, host.getTransform().toLocalX(popup.content().x()), host.getTransform().toLocalY(popup.content().y()), GLFW.GLFW_MOUSE_BUTTON_LEFT));
            if (item.shouldCloseOnClick() && (item.getSubItems().length <= 0 || item.getClickAction() != null)) {
                dismiss();
            }
        });
    }


    public static DLContextMenu openMenu(GuiComponent host, double x, double y, Consumer<DLContextMenu> factory) {
        DLContextMenu menu = new DLContextMenu(host);
        factory.accept(menu);
        menu.popup.owner(host).openAt(host.getWindowManager(), x, y);
        menu.requestFocus();
        return menu;
    }

    private void manageSubMenu() {
        if (selectedIndex < 0 || selectedIndex >= items.size()) {
            return;
        }
        AbstractContextMenuItem item = items.get(selectedIndex);
        if (!item.isEnabled() || item.getSubItems().length <= 0) {
            return;
        }
        if (childMenu != null && childMenu.selectedSubMenuIndex == selectedIndex) {
            return;
        }
        WindowManager manager = getWindowManager();
        if (manager == null || popup == null) {
            return;
        }
        closeSubMenu();

        DLContextMenu menu = new DLContextMenu(this);
        menu.showCheckMargin.set(showCheckMargin.get());
        menu.items.addAll(item.getSubItems());
        menu.popup.onClosed(() -> {
            if (childMenu == menu) {
                childMenu = null;
            }
        });

        Popup child = menu.popup.prepare(manager);
        double popupX = getTransform().screenRight() - getThemeValue(THEME_KEY_SUBMENU_OVERLAP);
        double popupY = getTransform().toScreenY(rowY(selectedIndex)) - getThemeValue(THEME_KEY_BORDER_SIZE) - getThemeValue(THEME_KEY_ITEM_MARGIN).top();
        if (popupX + menu.width() > manager.screenWidth()) {
            popupX = getTransform().screenX() + getThemeValue(THEME_KEY_SUBMENU_OVERLAP) - menu.width();
        }
        popup.openChildAt(child, popupX, popupY);
        menu.requestFocus();

        childMenu = menu;
        childMenu.selectedSubMenuIndex = selectedIndex;
    }

    private void closeSubMenu() {
        if (childMenu != null) {
            DLContextMenu menu = childMenu;
            childMenu = null;
            menu.closeMenu();
        }
    }


    public void dismiss() {
        if (popup != null) {
            popup.closeCascade();
        }
    }

    public void closeMenu() {
        if (popup != null) {
            popup.close();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public ItemLayout getItemLayout() {
        return itemLayout;
    }


    public Hotkey hotkeyOf(ContextMenuItem item) {
        if (item == null) {
            return null;
        }
        if (item.getHotkey() != null) {
            return item.getHotkey();
        }
        if (item.getHotkeyId() != null && scope != null) {
            return Hotkeys.lookup(scope, item.getHotkeyId());
        }
        return null;
    }

    public String hotkeyText(ContextMenuItem item) {
        Hotkey hotkey = hotkeyOf(item);
        return hotkey == null ? null : hotkey.displayName();
    }

    public boolean triggerHotkey(int keyCode, int modifiers) {
        for (AbstractContextMenuItem item : items) {
            if (!(item instanceof ContextMenuItem i) || !i.isEnabled()) {
                continue;
            }
            Hotkey hotkey = hotkeyOf(i);
            if (hotkey == null || !hotkey.matches(keyCode, modifiers)) {
                continue;
            }
            DLUtils.doIfNotNull(i.getClickAction(), x -> x.click(this, INVALID, INVALID, INVALID));
            if (i.shouldCloseOnClick() && (i.getSubItems().length <= 0 || i.getClickAction() != null)) {
                dismiss();
            }
            return true;
        }
        return false;
    }

    private void onItemsChanged() {
        selectedIndex = INVALID;
        closeSubMenu();
        invalidateLayout();
    }

    private void setPointer(double localX, double localY) {
        this.pointerInside = true;
        this.pointerLocalX = localX;
        this.pointerLocalY = localY;
    }

    private void select(int index) {
        if (selectedIndex == index) {
            return;
        }
        selectedIndex = index;
        closeSubMenu();
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public int getIndexOffset() {
        return indexOffset;
    }

    public int getVisibleEndIndex() {
        return visibleEndIndex;
    }

    public double getViewportHeight() {
        return viewportHeight;
    }

    public boolean canScrollUp() {
        return scrollable && indexOffset > 0;
    }

    public boolean canScrollDown() {
        return scrollable && indexOffset < maxIndexOffset;
    }

    public boolean scrollBy(int delta) {
        if (!scrollable || delta == 0) {
            return false;
        }
        int next = Math.max(0, Math.min(maxIndexOffset, indexOffset + delta));
        if (next == indexOffset) {
            return false;
        }
        indexOffset = next;
        updateScrollWindow();
        if (pointerInside) {
            select(indexAt(pointerLocalY));
        }
        return true;
    }

    protected int scrollZoneAt(double localX, double localY) {
        if (!scrollable || !isMouseInBounds(localX, localY)) {
            return SCROLL_NONE;
        }
        if (localY < itemsTop()) {
            return SCROLL_UP;
        }
        if (localY >= itemsBottom()) {
            return SCROLL_DOWN;
        }
        return SCROLL_NONE;
    }

    private void stopAutoScroll() {
        scrollDirection = SCROLL_NONE;
        scrollTicks = 0;
    }

    private void tickAutoScroll() {
        int direction = pointerInside ? scrollZoneAt(pointerLocalX, pointerLocalY) : SCROLL_NONE;
        if (direction != scrollDirection) {
            scrollDirection = direction;
            scrollTicks = 0;
        }
        if (direction == SCROLL_NONE) {
            return;
        }

        int delay = Math.max(1, getThemeValue(THEME_KEY_SCROLL_DELAY));
        int interval = Math.max(1, getThemeValue(THEME_KEY_SCROLL_INTERVAL));
        if (scrollTicks == 0 || (scrollTicks >= delay && (scrollTicks - delay) % interval == 0)) {
            scrollBy(direction);
        }
        scrollTicks++;
    }

    protected void updateScrollWindow() {
        int borderSize = getThemeValue(THEME_KEY_BORDER_SIZE);
        Insets margin = getThemeValue(THEME_KEY_ITEM_MARGIN);

        double chrome = borderSize * 2 + margin.vertical();
        double available = Double.isInfinite(heightLimit) ? Double.POSITIVE_INFINITY : Math.max(0, heightLimit - chrome);

        double contentHeight = 0;
        for (int i = 0; i < items.size(); i++) {
            contentHeight += itemHeight(i);
        }

        scrollable = contentHeight > available;
        viewportHeight = scrollable ? Math.max(0, available - SCROLL_AREA_SIZE * 2) : contentHeight;

        maxIndexOffset = 0;
        if (scrollable) {
            double h = 0;
            int i = items.size() - 1;
            for (; i >= 0; i--) {
                double itemHeight = itemHeight(i);
                if (h + itemHeight > viewportHeight) {
                    break;
                }
                h += itemHeight;
            }
            maxIndexOffset = i + 1;
        }

        indexOffset = Math.max(0, Math.min(maxIndexOffset, indexOffset));

        double h = 0;
        visibleEndIndex = indexOffset;
        for (int i = indexOffset; i < items.size(); i++) {
            double itemHeight = itemHeight(i);
            if (h + itemHeight > viewportHeight) {
                break;
            }
            h += itemHeight;
            visibleEndIndex = i + 1;
        }
    }

    protected double itemHeight(int index) {
        if (index < 0 || index >= items.size()) {
            return 0;
        }
        return itemHeight(items.get(index));
    }

    protected double itemHeight(AbstractContextMenuItem item) {
        if (item == null) {
            return 0;
        }
        Insets padding = getThemeValue(THEME_KEY_ITEM_PADDING);

        if (item instanceof ContextMenuItem i) {
            boolean hasIcon = i.getIcon() != null && !i.getIcon().isEmpty();
            return Math.max(hasIcon ? i.getIcon().getHeight() : 0, TextUtils.font().lineHeight) + padding.vertical();
        } else if (item instanceof ContextMenuSeparator i) {
            return getThemeValue(DLContextMenu.THEME_KEY_ITEM_MARGIN).vertical() + 1;
        }
        return 0;
    }

    protected double itemsTop() {
        int borderSize = getThemeValue(THEME_KEY_BORDER_SIZE);
        Insets margin = getThemeValue(THEME_KEY_ITEM_MARGIN);

        return borderSize + margin.top() + (scrollable ? SCROLL_AREA_SIZE : 0);
    }

    protected double itemsBottom() {
        return itemsTop() + viewportHeight;
    }

    protected double rowY(int index) {
        double y = itemsTop();
        for (int i = indexOffset; i < index && i < items.size(); i++) {
            y += itemHeight(i);
        }
        return y;
    }

    protected boolean isMouseInBounds(double x, double y) {
        int borderSize = getThemeValue(THEME_KEY_BORDER_SIZE);
        Insets margin = getThemeValue(THEME_KEY_ITEM_MARGIN);
        return x >= borderSize + margin.left() && x <= dWidth() - borderSize - margin.right() && y >= borderSize + margin.top() && y <= dHeight() - borderSize - margin.bottom();
    }

    protected int indexAt(double localY) {
        double y = itemsTop();
        if (localY < y || localY >= itemsBottom()) {
            return INVALID;
        }
        for (int i = indexOffset; i < visibleEndIndex; i++) {
            double next = y + itemHeight(i);
            if (localY >= y && localY < next) {
                return i;
            }
            y = next;
        }
        return INVALID;
    }

    @Override
    protected Size measureContent(double availableWidth, double availableHeight) {
        WindowManager manager = getWindowManager();
        heightLimit = Double.isInfinite(availableHeight)
            ? (manager == null ? Double.POSITIVE_INFINITY : manager.screenHeight())
            : availableHeight;
        updateScrollWindow();

        int borderSize = getThemeValue(THEME_KEY_BORDER_SIZE);
        Insets margin = getThemeValue(THEME_KEY_ITEM_MARGIN);
        Insets padding = getThemeValue(THEME_KEY_ITEM_PADDING);

        double checkWidth = showCheckMargin.get() ? getThemeValue(THEME_KEY_CHECK_MARGIN_SIZE) : 0;
        double iconWidth = Math.max(0, getThemeValue(THEME_KEY_MIN_ICON_MARGIN_SIZE));
        double textWidth = 0;
        double arrowWidth = 0;
        int textGap = Math.max(0, getThemeValue(THEME_KEY_TEXT_GAP));

        for (AbstractContextMenuItem item : items) {
            if (item instanceof ContextMenuItem itm) {
                boolean hasIcon = itm.getIcon() != null && !itm.getIcon().isEmpty();
                iconWidth = Math.max(iconWidth, hasIcon ? itm.getIcon().getWidth() : 0);
                String hotkey = hotkeyText(itm);
                double rowWidth = TextUtils.textWidth(TextUtils.font(), itm.getText()) + textGap;
                if (hotkey != null) {
                    rowWidth += TextUtils.textWidth(TextUtils.font(), hotkey);
                }
                textWidth = Math.max(textWidth, rowWidth);

                if (itm.getSubItems().length > 0) {
                    arrowWidth = SUB_ITEM_ARROW_SIZE;
                }
            }
        }

        double checkX = padding.left();
        double iconX = checkX + checkWidth + (checkWidth > 0 ? padding.left() : 0);
        double textX = iconX + iconWidth + (iconWidth > 0 ? padding.left() : 0);
        double totalW = textX + textWidth + (arrowWidth > 0 ? padding.left() + arrowWidth : 0) + padding.right();

        itemLayout = new ItemLayout((int)checkX, (int)iconX, (int)textX, (int)checkWidth, (int)iconWidth, (int)textWidth, (int)arrowWidth, (int)totalW);

        double w = borderSize * 2 + margin.horizontal() + totalW;
        double h = borderSize * 2 + margin.vertical() + viewportHeight + (scrollable ? SCROLL_AREA_SIZE * 2 : 0);
        return Size.of(w, h);
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
    }
}
