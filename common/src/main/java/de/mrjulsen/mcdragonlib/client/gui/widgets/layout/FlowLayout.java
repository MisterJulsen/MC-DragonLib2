package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import java.util.List;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

import java.lang.Math; 

public class FlowLayout implements ILayoutManager {
    public enum Direction { VERTICAL, HORIZONTAL }
    public enum FlowConstraint { START, END }

    public final Property<Direction> flowDirection = new Property<>(Direction.HORIZONTAL);
    public final BooleanProperty wrap = new BooleanProperty(true, false);
    public final NumberProperty<Integer> horizontalGap = new NumberProperty<>(0); 
    public final NumberProperty<Integer> verticalGap = new NumberProperty<>(0);
    public final Property<Padding> padding = new Property<>(Padding.ZERO);
    public final BooleanProperty fillCrossAxis = new BooleanProperty(false, false);

    @Override
    public LayoutResult arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        
        if (children.isEmpty()) {
            return LayoutResult.EMPTY;
        }

        Padding p = padding.get();
        
        boolean isHorizontal = flowDirection.get() == Direction.HORIZONTAL;
        boolean shouldFill = fillCrossAxis.get() && !wrap.get();
        
        int gapX = horizontalGap.get();
        int gapY = verticalGap.get();        
        int startX = p.left(); 
        int startY = p.top();        
        int endX = host.width() - p.right();
        int endY = host.height() - p.bottom();        
        int maxAvailableWidth = host.width() - p.right();
        int maxAvailableHeight = host.height() - p.bottom();
        int currentLineMaxThickness = 0;
        int maxContentX = 0;
        int maxContentY = 0;

        for (DLGuiComponent child : children) {
            FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
            
            if (shouldFill) {
                if (isHorizontal) {
                    child.setHeight(host.height() - p.top() - p.bottom());
                    startY = p.top(); 
                } else {
                    child.setWidth(host.width() - p.left() - p.right());
                    startX = p.left();
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
                    if (wrap.get() && startX > p.left() && (startX + cw) > maxAvailableWidth) {
                        startX = p.left();
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
                    if (wrap.get() && startY > p.top() && (startY + ch) > maxAvailableHeight) {
                        startY = p.top();
                        startX += currentLineMaxThickness + gapX;
                        currentLineMaxThickness = 0;
                    }
                    
                    child.setPosition(startX, startY);
                    startY += ch + gapY;
                    currentLineMaxThickness = Math.max(currentLineMaxThickness, cw);
                }
            }

            int childRightEdge = child.x() + child.width();
            int childBottomEdge = child.y() + child.height();

            if (childRightEdge > maxContentX) maxContentX = childRightEdge;
            if (childBottomEdge > maxContentY) maxContentY = childBottomEdge;
        }

        return new LayoutResult(maxContentX + p.right(), maxContentY + p.bottom());
    }
    
    private FlowConstraint getConstraintOrDefault(DLGuiComponent child, FlowConstraint def) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof FlowConstraint) {
            return (FlowConstraint) child.layoutContraint.get();
        }
        return def;
    }
}