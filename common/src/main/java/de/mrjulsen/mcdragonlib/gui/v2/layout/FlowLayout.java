package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.Alignment;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;

/**
 * Places the children in a row and wraps to the next line once the row is full, like text. Each line
 * is as high as its tallest child.
 */
public class FlowLayout extends AbstractLayout {

    /**
     * Space between two children in the same line.
     */
    public final NumberProperty<Double> horizontalGap = length(0.0D);
    /**
     * Space between two lines.
     */
    public final NumberProperty<Double> verticalGap = length(0.0D);
    /**
     * How each line is aligned horizontally within the available width.
     */
    public final Property<Alignment> lineAlignment = option(Alignment.START);

    private record Line(List<GuiComponent> components, double width, double height) {}

    @Override
    public Size measure(GuiComponent host, double availableWidth, double availableHeight) {
        List<Line> lines = buildLines(host, availableWidth);
        double width = 0;
        double height = 0;
        for (int i = 0; i < lines.size(); i++) {
            width = Math.max(width, lines.get(i).width());
            height += lines.get(i).height();
        }
        height += verticalGap.get() * Math.max(0, lines.size() - 1);
        return Size.of(width, height);
    }

    @Override
    public void arrange(GuiComponent host, Rectangle contentArea) {
        List<Line> lines = buildLines(host, contentArea.width());
        double gapX = horizontalGap.get();
        double gapY = verticalGap.get();
        double cursorY = contentArea.y();
        Alignment alignment = lineAlignment.get();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            double cursorX = contentArea.x() + alignment.offset(contentArea.width(), line.width());
            for (int j = 0; j < line.components().size(); j++) {
                GuiComponent child = line.components().get(j);
                Insets m = child.margin.get();
                Size measured = child.measure(contentArea.width(), contentArea.height());
                double outerWidth = measured.w() + m.horizontal();
                Layout.place(child, cursorX, cursorY, outerWidth, line.height());
                cursorX += outerWidth + gapX;
            }
            cursorY += line.height() + gapY;
        }
    }

    private List<Line> buildLines(GuiComponent host, double availableWidth) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        List<Line> lines = new ArrayList<>();
        double gapX = horizontalGap.get();

        List<GuiComponent> current = new ArrayList<>();
        double lineWidth = 0;
        double lineHeight = 0;

        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Insets m = child.margin.get();
            Size measured = child.measure(availableWidth, Double.POSITIVE_INFINITY);
            double outerWidth = measured.w() + m.horizontal();
            double outerHeight = measured.h() + m.vertical();
            double needed = current.isEmpty() ? outerWidth : lineWidth + gapX + outerWidth;

            if (!current.isEmpty() && !Double.isInfinite(availableWidth) && needed > availableWidth) {
                lines.add(new Line(current, lineWidth, lineHeight));
                current = new ArrayList<>();
                lineWidth = 0;
                lineHeight = 0;
                needed = outerWidth;
            }
            current.add(child);
            lineWidth = needed;
            lineHeight = Math.max(lineHeight, outerHeight);
        }
        if (!current.isEmpty()) {
            lines.add(new Line(current, lineWidth, lineHeight));
        }
        return lines;
    }
}
