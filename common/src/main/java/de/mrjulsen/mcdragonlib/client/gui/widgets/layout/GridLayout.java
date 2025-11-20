package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import java.util.List;

public class GridLayout implements ILayoutManager {

    public final NumberProperty<Integer> columns = new NumberProperty<>(9, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> slotWidth = new NumberProperty<>(18, 0, Integer.MAX_VALUE);
    public final NumberProperty<Integer> slotHeight = new NumberProperty<>(18, 0, Integer.MAX_VALUE);
    public final NumberProperty<Integer> gap = new NumberProperty<>(0);

    public GridLayout(int columns) {
        this.columns.set(columns);
    }

    @Override
    public void arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        int cols = Math.max(1, columns.get());
        int w = slotWidth.get();
        int h = slotHeight.get();
        int g = gap.get();

        int xOffset = 0;
        int yOffset = 0;

        for (int i = 0; i < children.size(); i++) {
            DLGuiComponent child = children.get(i);
            
            int colIndex = i % cols;
            int rowIndex = i / cols;
            
            int xPos = xOffset + colIndex * (w + g);
            int yPos = yOffset + rowIndex * (h + g);
            
            child.setPosition(xPos, yPos);
        }
    }
}