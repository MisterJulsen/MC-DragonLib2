package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.ModalId;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.WindowBuilder;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class DLContextMenu extends DLAbstractCollectionComponent<DLContextMenu.ItemEntry, DLContextMenu.DLContextMenuItem> {

    @FunctionalInterface
    public static interface MenuBuilder {
        List<ItemEntry> buildContextMenuContents(int x, int y);
    }

    public static record ItemEntry(Component text, DLSprite icon, boolean enabled, Runnable action, MenuBuilder subMenu) {
        public static final ItemEntry SEPARATOR = new ItemEntry(TextUtils.empty(), DLSprite.empty(), false, () -> {}, null);
    }

    public static class DLContextMenuWindow extends DLWindow {
        public DLContextMenuWindow(DLWindowManager manager, DLContextMenu menu) {
            super(manager);
            setSize(menu.width(), menu.height());
            anchor.set(EAlign.values());
            addEventListener(DLWindow.WindowFocusEvent.class, (src, e) -> {
                if (getWindowManager() != null && getAssignedModal().isPresent() && !e.focus()) {
                    for (DLWindow win : getWindowManager().getWindows(this.getAssignedModal().get())) {
                        if (win instanceof DLContextMenuWindow && (getWindowManager() == null || getWindowManager().getFocusedWindow() == win))
                            return false;
                    }
                    getWindowManager().closeModal(this.getAssignedModal().get());
                }
                return false;
            });
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x22FFFFFF));
        }
    }

    protected static final int BORDER_SIZE = 1;
    protected static final int ITEM_TOP_MARGIN = 2;
    protected static final int ITEM_BOTTOM_MARGIN = 2;

    protected final DLPanel contentPanel;
    protected final DLContextMenu rootMenu;
    protected final DLContextMenu parentMenu;

    protected ModalId windowId;
    protected DLGuiComponent hoveredItem;
    protected DLContextMenu subMenu;
    protected DLWindow window;
    private int posX, posY;

    public final MenuBuilder menuBuilder;
    
    public DLContextMenu(MenuBuilder builder) {
        this(builder, null, null);
    }

    protected DLContextMenu(MenuBuilder builder, DLContextMenu rootMenu, DLContextMenu parentMenu) {
        super(0, 0, 80, 100);
        this.menuBuilder = builder;
        this.rootMenu = rootMenu;
        this.parentMenu = parentMenu;

        this.contentPanel = new DLPanel(BORDER_SIZE, BORDER_SIZE, width() - BORDER_SIZE * 2, height() - BORDER_SIZE * 2);
        this.contentPanel.anchor.set(EAlign.values());
        this.addComponent(this.contentPanel);
    }
    
    public void open(DLWindowManager windowManager) {
        open(windowManager, (int)windowManager.mouseXOnScreen(), (int)windowManager.mouseYOnScreen());
    }
    
    public void open(DLWindowManager windowManager, int x, int y) {
        this.posX = x;
        this.posY = y;
        contentPanel.clearComponents();
        createComponents();
        layoutComponents();

        WindowBuilder<?> builder = (root) -> {
            DLContextMenuWindow win = new DLContextMenuWindow(root, this);
            win.addComponent(this);
            int pX = x;
            if (parentMenu != null && pX + width() > windowManager.getScreenWidth()) {
                pX = (int)parentMenu.getXOnScreen() - width();
            }
            win.setPosition(MathUtils.clamp(pX, 0, windowManager.getScreenWidth() - width()), MathUtils.clamp(y, 0, windowManager.getScreenHeight() - height()));
            this.window = win;
            return win;
        };
        windowId = rootMenu == null ? windowManager.createModal(builder) : windowManager.createWindow(builder);
    }

    @Override
    protected void createComponents() {
        contentPanel.clearComponents();
        int h = 0;
        int w = 5;
        for (ItemEntry item : menuBuilder.buildContextMenuContents(posX, posY)) {
            final ItemEntry itm = item;
            DLContextMenuItem listItem = itemBuilder.get().apply(itm);
            listItem.addEventListener(DLGuiStandardEvents.MouseEnterEvent.class, (src, event) -> {
                if (hoveredItem == src) {
                    return false;
                }

                if (subMenu != null) {
                    subMenu.closeSubMenu();
                    subMenu = null;
                    hoveredItem = null;
                }
              
                hoveredItem = src;
                if (itm.subMenu() != null) {
                    subMenu = new DLContextMenu(itm.subMenu(), getRootMenu(), this);
                    subMenu.open(getWindowManager(), (int)(listItem.getXOnScreen() + listItem.width() - 2), (int)(listItem.getYOnScreen() - BORDER_SIZE - ITEM_TOP_MARGIN));
                }
                return false;
            });
            contentPanel.addComponent(listItem);
            setItemHeight(listItem, listItem.requiredHeight());
            h += listItem.height();
            w = Math.max(listItem.requiredWidth(), w);
        }        
        setSize(w + BORDER_SIZE * 2, h + ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN + BORDER_SIZE * 2);
    }

    @Override
    protected void layoutComponents() {
        int currentY = ITEM_TOP_MARGIN;
        for (DLContextMenuItem item : contentPanel.getComponentsOfType(DLContextMenuItem.class, true)) {
            setItemX(item, 0);
            setItemY(item, currentY);
            setItemWidth(item, contentPanel.width());
            currentY += item.height();
        }
        currentY += ITEM_BOTTOM_MARGIN;
    }

    public void closeSubMenu() {
        if (subMenu != null) {
            subMenu.closeSubMenu();
        }
        if (getWindowManager() != null && window != null) {
            getWindowManager().closeWindow(window);
        }
    }

    public void closeMenu() {
        if (getWindowManager() != null) {
            getWindowManager().closeModal(getRootMenu().windowId);
            this.window = null;
            this.windowId = null;
        }
    }

    public DLContextMenu getRootMenu() {
        return rootMenu == null ? this : rootMenu;
    }

    @Override
    protected DLContextMenuItem defaultItemBuilder(ItemEntry item) {
        return item == ItemEntry.SEPARATOR ? new DLContextMenuSeparator(this) : new DLContextMenuItem(this, item);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.fill(graphics, 0, 0, width(), height(), DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR);
        GuiUtils.fill(graphics, BORDER_SIZE, BORDER_SIZE, width() - BORDER_SIZE * 2, height() - BORDER_SIZE * 2, DLColor.BLACK);
    }

    public static class DLContextMenuItem extends DLAbstractCollectionComponent.DLCollectionItem<ItemEntry, DLContextMenu> {

        protected static final int DEFAULT_HEIGHT = 12;
        protected static final int SUB_MENU_ARROW = 10;
        protected static final int ICON_MARGIN = 2;
        protected static final int TEXT_TO_ICON_MARGIN = 4;
        protected static final int RIGHT_MARGIN = 2;

        protected DLColor color = DLColor.TRANSPARENT;

        protected DLContextMenuItem(DLContextMenu collectionComponentRef, ItemEntry item) {
            super(collectionComponentRef, item, 1, 0);
            this.enabled.set(item.enabled());
            addEventListener(DLGuiStandardEvents.ClickEvent.class, (src, event) -> {
                collectionComponentRef.closeMenu();
                DLUtils.doIfNotNull(item.action(), Runnable::run);
                return false;
            });
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            if (!item.icon.isEmpty()) {
                item.icon.render(graphics, ICON_MARGIN, ITEM_TOP_MARGIN);
            }
            if (isSelected()) {
                GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x44FFFFFF));
            }
            GuiUtils.drawString(graphics, Minecraft.getInstance().font, ICON_MARGIN + item.icon().getWidth() + TEXT_TO_ICON_MARGIN, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, item.text(), isSelected() ? DragonLib.VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR : (enabled.get() ? DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR : DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR), ETextAlignment.LEFT, false);

            if (item.subMenu() != null) {
                GuiUtils.drawString(graphics, Minecraft.getInstance().font, width() - RIGHT_MARGIN, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, ">", DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.RIGHT, false);
            }

            GuiUtils.fill(graphics, 0, 0, width(), height(), color);
        }

        public int requiredWidth() {
            return ICON_MARGIN + item.icon().getWidth() + TEXT_TO_ICON_MARGIN + Minecraft.getInstance().font.width(item.text()) + SUB_MENU_ARROW + RIGHT_MARGIN;
        }

        public int requiredHeight() {
            return Math.max(DEFAULT_HEIGHT, Math.max(item.icon().getHeight() + ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN, Minecraft.getInstance().font.lineHeight + 2));
        }
    }

    public static final class DLContextMenuSeparator extends DLContextMenuItem {

        protected DLContextMenuSeparator(DLContextMenu collectionComponentRef) {
            super(collectionComponentRef, ItemEntry.SEPARATOR);
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            GuiUtils.fill(graphics, 5, 1, width() - 10, 1, DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR);
        }

        @Override
        public int requiredHeight() {
            return 3;
        }

        @Override
        public int requiredWidth() {
            return 1;
        }
        
    }
}
