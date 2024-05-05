package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public class DLContextMenuItemSeparator extends DLContextMenuItem {

    public DLContextMenuItemSeparator(DLContextMenu parent, int x, int y, int w) {
        super(parent, x, y, w, TextUtils.empty(), Sprite.empty(), (b) -> {});
        active = false;
        height = 3;
    }

    @Override
    public void onHoverChange(int mouseX, int mouseY, boolean isHovering) {}

    @Override
    public void setMenu(DLContextMenu menu) {}

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        GuiUtils.fill(graphics, getX() + 12, getY() + 1, width - 16, 1, 0xFF404040);
    }
}
