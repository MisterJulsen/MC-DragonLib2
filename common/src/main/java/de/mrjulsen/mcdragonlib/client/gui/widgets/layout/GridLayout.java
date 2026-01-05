package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import java.util.List;
import java.lang.Math;

/**
 * A simple fixed-cell grid layout.
 *
 * <p>Configuration:
 * <ul>
 * <li>columns: number of columns in the grid (minimum 1).</li>
 * <li>slotWidth / slotHeight: cell dimensions used for every grid slot.</li>
 * <li>gap: pixel spacing between adjacent cells.</li>
 * <li>padding: outer padding around the grid area.</li>
 * </ul>
 *
 * <p>Placement:
 * <ul>
 * <li>Children are placed in row-major order (left-to-right, top-to-bottom).</li>
 * <li>Each child is positioned at the computed cell x/y; children are not resized by the layout.</li>
 * <li>The layout computes the bounding content area from the last occupied cell and returns it in
 *     the {@link LayoutResult}.</li>
 * </ul>
 */
public class GridLayout implements ILayoutManager {

    /**
     * Number of columns in the grid; enforced to be at least 1.
     */
    public final NumberProperty<Integer> columns = new NumberProperty<>(9, 1, Integer.MAX_VALUE);

    /**
     * Width in pixels for every slot/cell.
     */
    public final NumberProperty<Integer> slotWidth = new NumberProperty<>(18, 0, Integer.MAX_VALUE);

    /**
     * Height in pixels for every slot/cell.
     */
    public final NumberProperty<Integer> slotHeight = new NumberProperty<>(18, 0, Integer.MAX_VALUE);

    /**
     * Gap in pixels between cells.
     */
    public final NumberProperty<Integer> gap = new NumberProperty<>(0);

    /**
     * Padding around the grid area.
     */
    public final Property<Padding> padding = new Property<>(Padding.ZERO);

    /**
     * Construct a GridLayout with the specified column count.
     *
     * @param columns number of columns to layout; values less than 1 will be clamped to 1.
     */
    public GridLayout(int columns) {
        this.columns.set(columns);
    }

    /**
     * Arrange children in a regular grid and return the required content size.
     *
     * @param host the parent component used to compute available area and padding.
     * @return a {@link LayoutResult} reflecting the area used by the arranged children.
     */
    @Override
    public LayoutResult arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        
        if (children.isEmpty()) {
            return LayoutResult.EMPTY;
        }

        Padding p = padding.get();

        int cols = Math.max(1, columns.get());
        int w = slotWidth.get();
        int h = slotHeight.get();
        int g = gap.get();

        int xOffset = p.left();
        int yOffset = p.top();

        int maxGridX = 0;
        int maxGridY = 0;

        for (int i = 0; i < children.size(); i++) {
            DLGuiComponent child = children.get(i);
            
            int colIndex = i % cols;
            int rowIndex = i / cols;
            
            int xPos = xOffset + colIndex * (w + g);
            int yPos = yOffset + rowIndex * (h + g);
            
            child.setPosition(xPos, yPos);
            int currentRightEdge = xPos + w;
            int currentBottomEdge = yPos + h;

            if (currentRightEdge > maxGridX) maxGridX = currentRightEdge;
            if (currentBottomEdge > maxGridY) maxGridY = currentBottomEdge;
        }

        return new LayoutResult(maxGridX + p.right(), maxGridY + p.bottom());
    }
}