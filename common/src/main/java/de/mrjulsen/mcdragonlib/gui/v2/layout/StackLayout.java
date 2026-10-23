package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.Alignment;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;

/**
 * Places the children in a row or a column, one after the other.
 * <p>This is the layout that implements {@link SizeMode#WEIGHT} on its
 * main axis: children that grow share the space left over after the fixed ones, in proportion to
 * their width or height factor. As soon as one child grows, the main alignment no longer matters,
 * because there is nothing left to distribute.</p>
 */
public class StackLayout extends AbstractLayout {

    /**
     * The axis the children are stacked along.
     */
    public enum Direction {
        VERTICAL,
        HORIZONTAL
    }

    /**
     * Whether the children are stacked downwards or to the right.
     */
    public final Property<Direction> direction = option(Direction.VERTICAL);
    /**
     * Space between two children.
     */
    public final NumberProperty<Double> gap = length(0.0D);
    /**
     * Where the children sit along the stacking axis when there is room left. Ignored once a child grows.
     */
    public final Property<Alignment> mainAlignment = option(Alignment.START);
    /**
     * How the children are aligned across the stacking axis. STRETCH widens all of them to the full
     * width, and a child that grows on that axis stretches anyway.
     */
    public final Property<Alignment> crossAlignment = option(Alignment.START);

    public StackLayout() {
    }

    public StackLayout(Direction direction) {
        this.direction.set(direction);
    }

    public StackLayout(Direction direction, double gap) {
        this.direction.set(direction);
        this.gap.set(gap);
    }

    private boolean isVertical() {
        return direction.get() == Direction.VERTICAL;
    }

    @Override
    public Size measure(GuiComponent host, double availableWidth, double availableHeight) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        if (children.isEmpty()) {
            return Size.EMPTY;
        }
        boolean vertical = isVertical();
        double mainTotal = 0;
        double crossMax = 0;
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Size measured = child.measure(
                vertical ? availableWidth : Double.POSITIVE_INFINITY,
                vertical ? Double.POSITIVE_INFINITY : availableHeight
            );
            Insets m = child.margin.get();
            mainTotal += vertical ? measured.h() + m.vertical() : measured.w() + m.horizontal();
            crossMax = Math.max(crossMax, vertical ? measured.w() + m.horizontal() : measured.h() + m.vertical());
        }
        mainTotal += gap.get() * (children.size() - 1);
        return vertical ? Size.of(crossMax, mainTotal) : Size.of(mainTotal, crossMax);
    }

    @Override
    public void arrange(GuiComponent host, Rectangle contentArea) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        if (children.isEmpty()) {
            return;
        }
        boolean vertical = isVertical();
        double availableMain = vertical ? contentArea.height() : contentArea.width();
        double availableCross = vertical ? contentArea.width() : contentArea.height();
        double spacing = gap.get();

        double[] mainSizes = new double[children.size()];
        double[] crossSizes = new double[children.size()];
        double fixedTotal = 0;
        double totalWeight = 0;

        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Insets m = child.margin.get();
            Size measured = child.measure(
                vertical ? availableCross : Double.POSITIVE_INFINITY,
                vertical ? Double.POSITIVE_INFINITY : availableCross
            );
            crossSizes[i] = vertical ? measured.w() : measured.h();
            boolean grows = vertical ? Layout.growsVertically(child) : Layout.growsHorizontally(child);
            if (grows) {
                double factor = vertical ? child.heightFactor.get() : child.widthFactor.get();
                totalWeight += Math.max(factor, 0);
                mainSizes[i] = -1;
                fixedTotal += vertical ? m.vertical() : m.horizontal();
            } else {
                mainSizes[i] = vertical ? measured.h() : measured.w();
                fixedTotal += mainSizes[i] + (vertical ? m.vertical() : m.horizontal());
            }
        }

        double gaps = spacing * (children.size() - 1);
        double leftover = Math.max(0, availableMain - fixedTotal - gaps);

        for (int i = 0; i < children.size(); i++) {
            if (mainSizes[i] < 0) {
                GuiComponent child = children.get(i);
                double factor = Math.max(vertical ? child.heightFactor.get() : child.widthFactor.get(), 0);
                mainSizes[i] = totalWeight <= 0 ? 0 : leftover * (factor / totalWeight);
            }
        }

        double usedMain = gaps;
        for (int i = 0; i < children.size(); i++) {
            Insets m = children.get(i).margin.get();
            usedMain += mainSizes[i] + (vertical ? m.vertical() : m.horizontal());
        }

        double cursor = (vertical ? contentArea.y() : contentArea.x())
            + (totalWeight > 0 ? 0 : mainAlignment.get().offset(availableMain, usedMain));

        Alignment cross = crossAlignment.get();
        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Insets m = child.margin.get();
            double outerMain = mainSizes[i] + (vertical ? m.vertical() : m.horizontal());
            boolean stretch = cross == Alignment.STRETCH
                || (vertical ? Layout.growsHorizontally(child) : Layout.growsVertically(child));
            double outerCross = stretch ? availableCross : crossSizes[i] + (vertical ? m.horizontal() : m.vertical());
            double crossOffset = stretch ? 0 : cross.offset(availableCross, outerCross);

            if (vertical) {
                Layout.place(child, contentArea.x() + crossOffset, cursor, outerCross, outerMain);
            } else {
                Layout.place(child, cursor, contentArea.y() + crossOffset, outerMain, outerCross);
            }
            cursor += outerMain + spacing;
        }
    }
}
