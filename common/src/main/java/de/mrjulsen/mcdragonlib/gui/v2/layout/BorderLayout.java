package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;

/**
 * Splits the host into five regions: the edges take as much room as their children need, the center
 * gets whatever is left.
 * <p>A child picks its region through its layoutConstraint, see {@link #regionOf(GuiComponent)};
 * without one it ends up in the center. Several children in the same edge region stack up in that
 * direction, and the order they are placed in is top, bottom, left, right, so the top and bottom
 * span the full width.</p>
 */
public class BorderLayout extends AbstractLayout {

    /**
     * The five regions of a border layout. Set it as a child's layoutConstraint.
     */
    public enum Region {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        CENTER
    }

    /**
     * Space between two regions.
     */
    public final NumberProperty<Double> gap = length(0.0D);

    /**
     * The region a child belongs to, taken from its layoutConstraint.
     *
     * @return the region, or {@link Region#CENTER} if the constraint is not a region
     */
    public static Region regionOf(GuiComponent child) {
        return child.layoutConstraint.get() instanceof Region region ? region : Region.CENTER;
    }

    @Override
    public Size measure(GuiComponent host, double availableWidth, double availableHeight) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        double spacing = gap.get();
        double topHeight = 0;
        double bottomHeight = 0;
        double leftWidth = 0;
        double rightWidth = 0;
        double centerWidth = 0;
        double centerHeight = 0;

        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Insets m = child.margin.get();
            Size measured = child.measure(availableWidth, availableHeight);
            double w = measured.w() + m.horizontal();
            double h = measured.h() + m.vertical();
            switch (regionOf(child)) {
                case TOP -> topHeight += h + spacing;
                case BOTTOM -> bottomHeight += h + spacing;
                case LEFT -> leftWidth += w + spacing;
                case RIGHT -> rightWidth += w + spacing;
                default -> {
                    centerWidth = Math.max(centerWidth, w);
                    centerHeight = Math.max(centerHeight, h);
                }
            }
        }
        return Size.of(leftWidth + rightWidth + centerWidth, topHeight + bottomHeight + centerHeight);
    }

    @Override
    public void arrange(GuiComponent host, Rectangle contentArea) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        double spacing = gap.get();
        double top = contentArea.y();
        double bottom = contentArea.y() + contentArea.height();
        double left = contentArea.x();
        double right = contentArea.x() + contentArea.width();

        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            if (regionOf(child) != Region.TOP) {
                continue;
            }
            Insets m = child.margin.get();
            double h = child.measure(right - left, bottom - top).h() + m.vertical();
            Layout.place(child, left, top, right - left, h);
            top += h + spacing;
        }
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            if (regionOf(child) != Region.BOTTOM) {
                continue;
            }
            Insets m = child.margin.get();
            double h = child.measure(right - left, bottom - top).h() + m.vertical();
            Layout.place(child, left, bottom - h, right - left, h);
            bottom -= h + spacing;
        }
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            if (regionOf(child) != Region.LEFT) {
                continue;
            }
            Insets m = child.margin.get();
            double w = child.measure(right - left, bottom - top).w() + m.horizontal();
            Layout.place(child, left, top, w, bottom - top);
            left += w + spacing;
        }
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            if (regionOf(child) != Region.RIGHT) {
                continue;
            }
            Insets m = child.margin.get();
            double w = child.measure(right - left, bottom - top).w() + m.horizontal();
            Layout.place(child, right - w, top, w, bottom - top);
            right -= w + spacing;
        }
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            if (regionOf(child) == Region.CENTER) {
                Layout.place(child, left, top, Math.max(0, right - left), Math.max(0, bottom - top));
            }
        }
    }
}
