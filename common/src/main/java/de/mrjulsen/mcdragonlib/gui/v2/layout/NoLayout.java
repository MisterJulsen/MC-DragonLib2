package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.Anchor;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
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
