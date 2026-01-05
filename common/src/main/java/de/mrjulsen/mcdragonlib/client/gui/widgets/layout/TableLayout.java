package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import java.lang.Math;

/**
 * A simple table-style layout that positions children in named columns.
 *
 * <p>Columns:
 * <ul>
 * <li>Columns are defined by calling {@link #addColumn(String, double, ColumnSizeMode)} in order.</li>
 * <li>Each column has a name which is matched against a child's layout constraint to identify the
 *     component that should be placed into that column.</li>
 * </ul>
 *
 * <p>Column sizing modes:
 * <ul>
 * <li>FIXED: the column consumes an absolute pixel width equal to the supplied size.</li>
 * <li>AUTO: the column width is derived from the width of the component assigned to that column (or 0
 *         if no component is present).</li>
 * <li>PERCENTAGE: the column participates in distributing the remaining width according to its
 *               relative percentage weight; sizes given for PERCENTAGE columns are treated as
 *               weights that are normalized among all PERCENTAGE columns.</li>
 * </ul>
 *
 * <p>Layout process:
 * <ol>
 * <li>Compute used width by summing FIXED and AUTO column widths and all gaps.</li>
 * <li>The remaining available width (host width minus padding and used width) is distributed among
 *     PERCENTAGE columns proportional to their weights.</li>
 * <li>Each named child is positioned at the left edge of its column and sized to the calculated
 *     column width and the available height (host height minus vertical padding).</li>
 * </ol>
 *
 * <p>Matching children to columns:
 * <ul>
 * <li>A child provides its desired column via {@code layoutContraint} containing the column name
 *     (a {@link String}). If no child matches a column name, that column remains empty.</li>
 * </ul>
 *
 * <p>Return value:
 * <ul>
 * <li>The returned {@link LayoutResult} reports the right-most used pixel coordinate plus right padding
 *     and the maximum content height plus bottom padding, allowing callers to detect overflow.</li>
 * </ul>
 */
public class TableLayout implements ILayoutManager {

    /**
     * Size calculation modes for a column.
     *
     * <p>FIXED and AUTO produce absolute pixel widths; PERCENTAGE columns receive a proportional
     * share of the remaining available width after fixed and auto columns are allocated.
     */
    public enum ColumnSizeMode {
        FIXED,
        PERCENTAGE,
        AUTO
    }

    /**
     * Lightweight descriptor for a column definition.
     *
     * <p>Fields:
     * <ul>
     * <li>name: identifies the column and is used to match a child component's layout constraint.</li>
     * <li>size: interpretation depends on {@link ColumnSizeMode} (absolute pixels or percentage weight).</li>
     * <li>mode: the sizing mode applied to the column.</li>
     * </ul>
     */
    public static class TableColumn {
        String name;
        double size;
        ColumnSizeMode mode;

        public TableColumn(String name, double size, ColumnSizeMode mode) {
            this.name = name;
            this.size = size;
            this.mode = mode;
        }
    }
    
    private final List<TableColumn> columns = new ArrayList<>();   

    /**
     * Gap in pixels between columns. Defaults to 0.
     */
    public final NumberProperty<Integer> columnGap = new NumberProperty<>(0);

    /**
     * Padding applied around the table area; it reduces available width and height for the columns.
     */
    public final Property<Padding> padding = new Property<>(Padding.ZERO);

    /**
     * Add a column definition to the layout.
     *
     * @param name unique name used to match a child component's layout constraint (String).
     * @param size interpretation depends on {@code mode}: absolute pixels for FIXED or AUTO,
     *             weight value for PERCENTAGE.
     * @param mode the sizing mode used for this column.
     * @return this TableLayout instance to allow fluent column additions.
     */
    public TableLayout addColumn(String name, double size, ColumnSizeMode mode) {
        this.columns.add(new TableColumn(name, size, mode));
        return this;
    }

    /**
     * Arrange children into the defined columns and compute the resulting content size.
     *
     * <p>Each child that provides a {@code String} constraint (via {@code layoutContraint}) is
     * assigned to the column with the same name. Only one component per column is used; if multiple
     * children use the same name the last one encountered will be stored in the internal map used
     * for layout.
     *
     * @param host the parent component whose dimension provide the available layout area.
     * @return a {@link LayoutResult} that contains the width/height the content requires.
     */
    @Override
    public LayoutResult arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        
        if (columns.isEmpty()) {
             return LayoutResult.EMPTY;
        }

        Padding p = padding.get();

        int totalAvailableWidth = Math.max(0, host.width() - p.left() - p.right());
        int gap = columnGap.get();
        
        Map<String, DLGuiComponent> columnMap = new HashMap<>();
        for (DLGuiComponent child : children) {
            String slotName = getSlotName(child);
            if (slotName != null) {
                columnMap.put(slotName, child);
            }
        }

        double usedWidth = 0;
        double totalPercentageWeight = 0;

        Map<String, Integer> calculatedWidths = new HashMap<>();

        for (TableColumn col : columns) {
            if (col.mode == ColumnSizeMode.FIXED) {
                usedWidth += col.size;
                calculatedWidths.put(col.name, (int) col.size);
                
            } else if (col.mode == ColumnSizeMode.AUTO) {
                DLGuiComponent c = columnMap.get(col.name);
                int autoW = (c != null) ? c.width() : 0;
                usedWidth += autoW;
                calculatedWidths.put(col.name, autoW);
                
            } else if (col.mode == ColumnSizeMode.PERCENTAGE) {
                totalPercentageWeight += col.size;
            }
        }

        int totalGaps = Math.max(0, (columns.size() - 1) * gap);
        double availableForPercent = Math.max(0, totalAvailableWidth - usedWidth - totalGaps);

        for (TableColumn col : columns) {
            if (col.mode == ColumnSizeMode.PERCENTAGE) {
                double share = (totalPercentageWeight > 0) ? (col.size / totalPercentageWeight) : 0;
                int w = (int) Math.round(availableForPercent * share);
                calculatedWidths.put(col.name, w);
            }
        }

        int currentX = p.left();
        int maxContentY = 0;
        int childHeight = Math.max(0, host.height() - p.top() - p.bottom());

        for (TableColumn col : columns) {
            int colW = calculatedWidths.getOrDefault(col.name, 0);
            DLGuiComponent child = columnMap.get(col.name);

            if (child != null) {
                child.setX(currentX);
                child.setY(p.top()); 
                child.setWidth(colW);                
                child.setHeight(childHeight);
                maxContentY = Math.max(maxContentY, child.y() + child.height());
            }

            currentX += colW + gap;
        }

        int finalRightEdge = (columns.size() > 0) ? (currentX - gap) : p.left();

        return new LayoutResult(finalRightEdge + p.right(), maxContentY + p.bottom());
    }

    /**
     * Helper that extracts the slot/column name from a child's layout constraint.
     *
     * @param child the component to query.
     * @return the slot name if the child's constraint is a String, otherwise null.
     */
    private String getSlotName(DLGuiComponent child) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof String s) {
            return s;
        }
        return null;
    }
}