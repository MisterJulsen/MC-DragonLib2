package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import java.util.List;
import org.joml.Math;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

public class FlowLayout implements ILayoutManager {
    public enum Direction { VERTICAL, HORIZONTAL }
    public enum FlowConstraint { START, END }

    public final Property<Direction> flowDirection = new Property<>(Direction.HORIZONTAL);
    public final BooleanProperty wrap = new BooleanProperty(true, false);
    public final NumberProperty<Integer> horizontalGap = new NumberProperty<>(0); 
    public final NumberProperty<Integer> verticalGap = new NumberProperty<>(0);

    public final BooleanProperty fillCrossAxis = new BooleanProperty(false, false);

    @Override
    public void arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        boolean isHorizontal = flowDirection.get() == Direction.HORIZONTAL;
        boolean shouldFill = fillCrossAxis.get() && !wrap.get();
        
        int gapX = horizontalGap.get();
        int gapY = verticalGap.get();
        
        int startX = 0; 
        int startY = 0;        
        int endX = host.width();
        int endY = host.height();        
        int currentLineMaxThickness = 0;

        for (DLGuiComponent child : children) {
            FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
            
            if (shouldFill) {
                if (isHorizontal) {
                    child.setHeight(host.height());
                    startY = 0; 
                } else {
                    child.setWidth(host.width());
                    startX = 0;
                }
            }

            int cw = child.width();
            int ch = child.height();

            if (isHorizontal) {
                if (constraint == FlowConstraint.END) {
                    endX -= cw;
                    child.setPosition(endX, startY);
                    endX -= gapX;
                } else {
                    if (wrap.get() && startX > 0 && (startX + cw) > host.width()) {
                        startX = 0;
                        startY += currentLineMaxThickness + gapY;
                        currentLineMaxThickness = 0;
                    }
                    
                    child.setPosition(startX, startY);
                    startX += cw + gapX;
                    currentLineMaxThickness = Math.max(currentLineMaxThickness, ch);
                }
            } else {
                if (constraint == FlowConstraint.END) {
                    endY -= ch;
                    child.setPosition(startX, endY);
                    endY -= gapY;
                } else {
                    if (wrap.get() && startY > 0 && (startY + ch) > host.height()) {
                        startY = 0;
                        startX += currentLineMaxThickness + gapX;
                        currentLineMaxThickness = 0;
                    }
                    
                    child.setPosition(startX, startY);
                    startY += ch + gapY;
                    currentLineMaxThickness = Math.max(currentLineMaxThickness, cw);
                }
            }
        }
    }
    
    private FlowConstraint getConstraintOrDefault(DLGuiComponent child, FlowConstraint def) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof FlowConstraint) {
            return (FlowConstraint) child.layoutContraint.get();
        }
        return def;
    }
}