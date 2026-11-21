/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.gui.v2.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.Anchor;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;

/**
 * The default layout: leaves the children at the position they were given and only applies their
 * anchors when the host is resized. The content size is derived from the children anchored to the
 * top or left edge, so a stack of absolutely positioned children can still scroll.
 */
public final class NoLayout implements Layout {

    /**
     * The single shared instance, since this layout has no state.
     */
    public static final NoLayout INSTANCE = new NoLayout();

    private NoLayout() {
    }

    @Override
    public Size measure(GuiComponent host, double availableWidth, double availableHeight) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        double width = 0;
        double height = 0;
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Size measured = child.measure(availableWidth, availableHeight);
            Insets m = child.margin.get();
            if (child.anchor.has(Anchor.LEFT) && !child.anchor.has(Anchor.RIGHT)) {
                width = Math.max(width, child.dX() + measured.w() + m.right());
            }
            if (child.anchor.has(Anchor.TOP) && !child.anchor.has(Anchor.BOTTOM)) {
                height = Math.max(height, child.dY() + measured.h() + m.bottom());
            }
        }
        return Size.of(width, height);
    }

    @Override
    public void arrange(GuiComponent host, Rectangle contentArea) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            child.applyAnchor(contentArea, child.measure(contentArea.width(), contentArea.height()));
        }
    }
}
