package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class DLContextMenuItem extends DLButton {

    private static final int DEFAULT_PADDING = 2;
    private static final int MIN_ICON_SIZE = 12;

    private static final Component ARROW_TEXT = TextUtils.text(">");

    private final DLContextMenu parentMenu;
    private final Sprite icon;

    public DLContextMenuItem(DLContextMenu parent, int x, int y, int w, Component text, Sprite icon, Consumer<DLContextMenuItem> pOnPress) {
        super(x, y, w, Math.max(MIN_ICON_SIZE, icon.getHeight()), text, pOnPress);
        this.icon = icon;
        this.parentMenu = parent;
    }

    @SuppressWarnings("resource")
    @Override
    public void onHoverChange(int mouseX, int mouseY, boolean isHovering) {
        if (getContextMenu() != null && isHovering && isActive()) {
            new ArrayList<>(parentMenu.children()).forEach(x -> {
                if (x instanceof DLContextMenuItem item && item.getContextMenu() != null) {
                    item.getContextMenu().close();
                }
            });
            
            getContextMenu().open(mouseX, mouseY, (menu) -> {
                Screen s = Minecraft.getInstance().screen;
                int pX = getX() + width - 3 + menu.getWidth() > s.width ? getX() - menu.getWidth() : getX() + width - 3;
                int pY = Math.min(getY() - 3, s.height - menu.getHeight());
                return new Vec2(pX, pY);
            });
        } else if (getContextMenu() != null && !getContextMenu().isHovered()) {
            getContextMenu().close();
        }
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        if (menu != null) {
            menu.setParentMenu(parentMenu);
        }
        super.setMenu(menu);
    }

    @Override
    public int getContextMenuOpenButton() {
        return NO_CONTEXT_MENU_BUTTON;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int color = active ? (isHoveredOrFocused() ? DragonLib.NATIVE_BUTTON_FONT_COLOR_HIGHLIGHT : 0xFFFFFFFF) : 0xFF404040;

        if ((this.isHoveredOrFocused() || (getContextMenu() != null && getContextMenu().isOpen())) && active) {
            GuiUtils.fill(graphics, getX(), getY(), width, height, 0x30FFFFFF);
        }

        int iconW = Math.max(MIN_ICON_SIZE, icon.getWidth());

        GuiUtils.setTint(color);
        icon.render(graphics, getX() + DEFAULT_PADDING, getY() + height / 2 - icon.getHeight() / 2);        
        GuiUtils.setTint(1.0F, 1.0F, 1.0F, this.alpha);
        GuiUtils.drawString(graphics, font, getX() + DEFAULT_PADDING * 3 + iconW, getY() + height / 2 - font.lineHeight / 2, getMessage(), color, EAlignment.LEFT, false);

        if (getContextMenu() != null) {
            GuiUtils.drawString(graphics, font, getX() + width - DEFAULT_PADDING, getY() + height / 2 - font.lineHeight / 2, ARROW_TEXT, color, EAlignment.RIGHT, false);
        }
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
    }
    
    @SuppressWarnings("resource")
    public static int calcWidth(Component text, Sprite icon) {
        if (text == null || icon == null) {
            return MIN_ICON_SIZE;
        }
        return DEFAULT_PADDING * 8 + Math.max(MIN_ICON_SIZE, icon.getWidth()) + Minecraft.getInstance().font.width(text) + 4;
    }

    public static int calcHeight(Sprite icon) {
        if (icon == null) {
            return 3;
        }
        return Math.max(MIN_ICON_SIZE, icon.getHeight());
    }

    public static class Builder {
        private final Collection<ContextMenuItemData> rawItems = new ArrayList<>();

        public Builder add(ContextMenuItemData item) {
            rawItems.add(item);
            return this;
        }

        public Builder addSeparator() {
            rawItems.add(new ContextMenuItemData(null, null, false, null, null));
            return this;
        }

        public void applySize(DLContextMenu menu) {
            int w = rawItems.stream()
                .mapToInt(x -> DLContextMenuItem.calcWidth(x.text(), x.icon())).max().orElse(MIN_ICON_SIZE);

            int h = rawItems.stream()
                .mapToInt(x -> DLContextMenuItem.calcHeight(x.icon())).sum();
                
            menu.setSize(w, h);
        }

        public Collection<DLContextMenuItem> build(DLContextMenu menu) {
            int w = rawItems.stream()
                .mapToInt(x -> DLContextMenuItem.calcWidth(x.text(), x.icon())).max().orElse(MIN_ICON_SIZE);

            int y = menu.getAreaY();
            Collection<DLContextMenuItem> items = new ArrayList<>();
            for (ContextMenuItemData data : rawItems) {
                DLContextMenuItem itm;
                if (data.text() == null) {
                    itm = new DLContextMenuItemSeparator(menu, menu.getAreaX(), y, w);
                } else {
                    itm = new DLContextMenuItem(menu, menu.getAreaX(), y, w, data.text(), data.icon(), data.click());
                    itm.active = data.enabled();
                    itm.setMenu(data.subMenu() == null ? null : data.subMenu().apply(itm));                    
                }

                items.add(itm);
                y += itm.getHeight();
            }
            return items;
        }
    }

    public static record ContextMenuItemData(Component text, Sprite icon, boolean enabled, Consumer<DLContextMenuItem> click, Function<DLContextMenuItem, DLContextMenu> subMenu) {}
    
}
 