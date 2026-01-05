package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import java.util.List;
import java.lang.Math;
import java.util.Set;
import java.util.HashSet;

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
 * <p>Placement behavior:
 * <ul>
 * <li>Default (no per-child constraint): children are placed in row-major order (left-to-right,
 *     top-to-bottom) into successive grid cells. The first child occupies cell index 0 (col 0, row 0),
 *     the next child occupies the next cell, and so on.</li>
 * <li>With per-child constraint: a child may provide a {@link GridConstraint} via its
 *     {@code layoutContraint} property to request placement into a specific cell identified by
 *     column (x) and row (y). When a valid {@link GridConstraint} is present the layout will attempt
 *     to place the child at the requested column/row.</li>
 * </ul>
 *
 * <p>Constraint semantics and conflict resolution:
 * <ul>
 * <li>Requested column values are clamped to the valid range [0, columns - 1]. Requested rows are
 *     clamped to a minimum of 0 (negative row values are treated as 0).</li>
 * <li>If the requested cell is already occupied by a previously placed child, the layout will find
 *     the next free cell using row-major order starting from the requested cell index. That is,
 *     it increments the linear cell index (row * columns + col) until an unoccupied cell is found
 *     and places the child there.</li>
 * <li>If multiple children request the same cell, the first processed child receives the requested
 *     cell and later ones are shifted to the next available cells as described above.</li>
 * <li>Children without a {@link GridConstraint} fill the remaining free cells in row-major order,
 *     skipping cells already consumed by constrained children.</li>
 * </ul>
 *
 * <p>Sizing and result:
 * <ul>
 * <li>Children are positioned at the computed cell x/y but are not resized by the layout; their
 *     sizes are independent of the layout (the layout uses {@code slotWidth} and {@code slotHeight}
 *     only to compute cell positions and the returned bounding area).</li>
 * <li>The layout computes the bounding content area from the last occupied cell (right and bottom
 *     edges) and returns it in the {@link LayoutResult}. Padding's right and bottom values are
 *     included in the returned size to reflect the required content area.</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 * <li>The algorithm processes children in the order provided by the host's component list. This
 *     ordering affects which child wins a requested cell when multiple children request the same
 *     position.</li>
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
     * Optional layout constraint to place a child into a specific grid cell (col x, row y).
     *
     * <p>Semantics:
     * <ul>
     * <li>x = desired column (will be clamped to [0, columns-1]).</li>
     * <li>y = desired row (will be clamped to >= 0).</li>
     * </ul>
     *
     * <p>Conflict resolution:
     * <ul>
     * <li>If the requested cell is already occupied by an earlier child, the layout will place the
     *     constrained child into the next free cell in row-major order starting from the requested
     *     index.</li>
     * </ul>
     */
    public static class GridConstraint {
        public final int x;
        public final int y;
        public GridConstraint(int x, int y) { this.x = x; this.y = y; }
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

        // Belegungs-Set für Zellen (index = row*cols + col)
        Set<Integer> occupied = new HashSet<>();
        int nextFreeIndex = 0;

        for (int i = 0; i < children.size(); i++) {
            DLGuiComponent child = children.get(i);
            
            // versuche Constraint auszulesen (ähnlich FlowLayout)
            GridConstraint gc = null;
            if (child.layoutContraint != null && child.layoutContraint.get() instanceof GridConstraint) {
                gc = (GridConstraint) child.layoutContraint.get();
            }

            int assignedIndex;
            if (gc != null) {
                // clamp x,y sinnvoll in Bereich (x in [0, cols-1], y >= 0)
                int colIdx = Math.max(0, Math.min(gc.x, cols - 1));
                int rowIdx = Math.max(0, gc.y);
                int desiredIndex = rowIdx * cols + colIdx;
                // falls belegt -> nächster freier (row-major)
                while (occupied.contains(desiredIndex)) {
                    desiredIndex++;
                }
                assignedIndex = desiredIndex;
                occupied.add(assignedIndex);
                // falls der nextFreeIndex vorliegt, bring ihn ggf. voran
                if (assignedIndex >= nextFreeIndex) {
                    nextFreeIndex = assignedIndex + 1;
                }
            } else {
                // normales Verhalten: nächster freier Slot
                while (occupied.contains(nextFreeIndex)) {
                    nextFreeIndex++;
                }
                assignedIndex = nextFreeIndex;
                occupied.add(assignedIndex);
                nextFreeIndex++;
            }

            int colIndex = assignedIndex % cols;
            int rowIndex = assignedIndex / cols;
            
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