package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;

/**
 * Places the children in a grid with a fixed number of columns, filled row by row. All cells of a
 * row share the same width.
 */
public class GridLayout extends AbstractLayout {

    /**
     * Number of columns. The number of rows follows from the number of children.
     */
    public final NumberProperty<Integer> columns = count(2, 1, Integer.MAX_VALUE);
    /**
     * Space between two columns.
     */
    public final NumberProperty<Double> horizontalGap = length(0.0D);
    /**
     * Space between two rows.
     */
    public final NumberProperty<Double> verticalGap = length(0.0D);
    /**
     * Whether all rows get the same height, dividing up the host's height. When off, each row is only as
     * high as its own children need.
     */
    public final BooleanProperty uniformRowHeight = flag(true);

    public GridLayout() {
    }

    public GridLayout(int columns) {
        this.columns.set(columns);
    }

    @Override
    public Size measure(GuiComponent host, double availableWidth, double availableHeight) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        if (children.isEmpty()) {
            return Size.EMPTY;
        }
        int cols = Math.max(1, columns.get());
        int rows = (children.size() + cols - 1) / cols;
        double cellWidth = cellWidth(availableWidth, cols);

        double maxCellWidth = 0;
        double totalHeight = 0;
        double rowHeight = 0;

        for (int i = 0; i < children.size(); i++) {
            GuiComponent child = children.get(i);
            Insets m = child.margin.get();
            Size measured = child.measure(cellWidth, Double.POSITIVE_INFINITY);
            maxCellWidth = Math.max(maxCellWidth, measured.w() + m.horizontal());
            rowHeight = Math.max(rowHeight, measured.h() + m.vertical());
            if ((i + 1) % cols == 0 || i == children.size() - 1) {
                totalHeight += rowHeight;
                rowHeight = 0;
            }
        }
        double width = Double.isInfinite(availableWidth)
            ? maxCellWidth * cols + horizontalGap.get() * (cols - 1)
            : availableWidth;
        return Size.of(width, totalHeight + verticalGap.get() * (rows - 1));
    }

    @Override
    public void arrange(GuiComponent host, Rectangle contentArea) {
        List<GuiComponent> children = Layout.laidOutChildren(host);
        if (children.isEmpty()) {
            return;
        }
        int cols = Math.max(1, columns.get());
        double gapX = horizontalGap.get();
        double gapY = verticalGap.get();
        double cellWidth = cellWidth(contentArea.width(), cols);

        double cursorY = contentArea.y();
        int index = 0;
        while (index < children.size()) {
            int rowEnd = Math.min(index + cols, children.size());
            double rowHeight = 0;
            for (int i = index; i < rowEnd; i++) {
                GuiComponent child = children.get(i);
                Insets m = child.margin.get();
                Size measured = child.measure(cellWidth, Double.POSITIVE_INFINITY);
                rowHeight = Math.max(rowHeight, measured.h() + m.vertical());
            }
            if (uniformRowHeight.get()) {
                int rows = (children.size() + cols - 1) / cols;
                double available = contentArea.height() - gapY * (rows - 1);
                rowHeight = Math.max(rowHeight, available / rows);
            }
            for (int i = index; i < rowEnd; i++) {
                double cellX = contentArea.x() + (i - index) * (cellWidth + gapX);
                Layout.place(children.get(i), cellX, cursorY, cellWidth, rowHeight);
            }
            cursorY += rowHeight + gapY;
            index = rowEnd;
        }
    }

    private double cellWidth(double availableWidth, int cols) {
        if (Double.isInfinite(availableWidth)) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.max(0, (availableWidth - horizontalGap.get() * (cols - 1)) / cols);
    }
}
