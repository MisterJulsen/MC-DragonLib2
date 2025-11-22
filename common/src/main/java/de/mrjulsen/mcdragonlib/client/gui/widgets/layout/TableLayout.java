package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
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

        int totalWidth = host.width();
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
        double availableForPercent = Math.max(0, totalWidth - usedWidth - totalGaps);

        for (TableColumn col : columns) {
            if (col.mode == ColumnSizeMode.PERCENTAGE) {
                double share = (totalPercentageWeight > 0) ? (col.size / totalPercentageWeight) : 0;
                int w = (int) Math.round(availableForPercent * share);
                calculatedWidths.put(col.name, w);
            }
        }

        int currentX = 0;
        int maxContentY = 0;

        for (TableColumn col : columns) {
            int colW = calculatedWidths.getOrDefault(col.name, 0);
            DLGuiComponent child = columnMap.get(col.name);

            if (child != null) {
                child.setX(currentX);
                child.setY(0); 
                child.setWidth(colW);                
                child.setHeight(host.height());
                maxContentY = Math.max(maxContentY, child.height());
            }

            currentX += colW + gap;
        }

        int contentWidth = (columns.size() > 0) ? (currentX - gap) : 0;

        return new LayoutResult(contentWidth, maxContentY);
    }

    private String getSlotName(DLGuiComponent child) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof String s) {
            return s;
        }
        return null;
    }
}