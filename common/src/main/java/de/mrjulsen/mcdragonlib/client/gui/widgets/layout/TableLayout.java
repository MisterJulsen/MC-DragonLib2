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

public class TableLayout implements ILayoutManager {

    public enum ColumnSizeMode {
        FIXED,
        PERCENTAGE,
        AUTO
    }

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
    public final NumberProperty<Integer> columnGap = new NumberProperty<>(0);
    public final Property<Padding> padding = new Property<>(Padding.ZERO);

    public TableLayout addColumn(String name, double size, ColumnSizeMode mode) {
        this.columns.add(new TableColumn(name, size, mode));
        return this;
    }

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

    private String getSlotName(DLGuiComponent child) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof String s) {
            return s;
        }
        return null;
    }
}