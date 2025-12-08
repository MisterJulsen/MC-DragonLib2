package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import java.util.ArrayList;
import java.util.List;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

import java.lang.Math; 

public class FlowLayout implements ILayoutManager {
    
    public enum Direction { VERTICAL, HORIZONTAL }
    public enum FlowConstraint { START, END, FILL }

    public final Property<Direction> flowDirection = new Property<>(Direction.HORIZONTAL);
    public final BooleanProperty wrap = new BooleanProperty(true);
    public final NumberProperty<Integer> horizontalGap = new NumberProperty<>(0); 
    public final NumberProperty<Integer> verticalGap = new NumberProperty<>(0);
    public final Property<Padding> padding = new Property<>(Padding.ZERO);
    public final BooleanProperty fillCrossAxis = new BooleanProperty(false);

    @Override
    public LayoutResult arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        
        if (children.isEmpty()) {
            return LayoutResult.EMPTY;
        }

        boolean isHorizontal = flowDirection.get() == Direction.HORIZONTAL;
        
        if (isHorizontal) {
            return arrangeHorizontal(host, children);
        } else {
            return arrangeVertical(host, children);
        }
    }

    private LayoutResult arrangeHorizontal(DLGuiComponent host, List<DLGuiComponent> children) {
        Padding p = padding.get();
        int gapX = horizontalGap.get();
        int gapY = verticalGap.get();
        int maxW = host.width() - p.left() - p.right();
        
        int currentY = p.top();
        int maxContentX = 0;
        int maxContentY = 0;

        int index = 0;
        while (index < children.size()) {
            int currentLineWidth = 0;
            int fixedUsedWidth = 0;
            int fillCount = 0;
            
            List<DLGuiComponent> lineComponents = new ArrayList<>();

            while (index < children.size()) {
                DLGuiComponent child = children.get(index);
                FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
                
                if (fillCrossAxis.get() && !wrap.get()) {
                    child.setHeight(host.height() - p.top() - p.bottom());
                }

                int childW = child.width();
                int currentGap = (lineComponents.isEmpty()) ? 0 : gapX;

                if (wrap.get() && !lineComponents.isEmpty()) {
                    if (currentLineWidth + currentGap + childW > maxW) {
                        break;
                    }
                }

                currentLineWidth += currentGap + childW;
                lineComponents.add(child);
                
                if (constraint == FlowConstraint.FILL) {
                    fillCount++;
                    fixedUsedWidth += currentGap; 
                } else {
                    fixedUsedWidth += currentGap + childW;
                }

                index++;
                if (!wrap.get() && index < children.size()) continue; 
            }

            int lineAvailableSpace = maxW - fixedUsedWidth;
            int fillWidth = 0;
            if (fillCount > 0) {
                fillWidth = Math.max(0, lineAvailableSpace / fillCount);
            }

            int startX = p.left();
            int endX = host.width() - p.right();
            int rowHeight = 0;

            for (DLGuiComponent child : lineComponents) {
                FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
                if (constraint == FlowConstraint.FILL) {
                    child.setWidth(fillWidth);
                }
                rowHeight = Math.max(rowHeight, child.height());
            }

            for (DLGuiComponent child : lineComponents) {
                FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
                if (constraint == FlowConstraint.END) {
                    endX -= child.width();
                    child.setPosition(endX, currentY);
                    endX -= gapX;
                } else {
                    child.setPosition(startX, currentY);
                    startX += child.width() + gapX;
                }
                
                maxContentX = Math.max(maxContentX, child.x() + child.width());
                maxContentY = Math.max(maxContentY, child.y() + child.height());
            }

            currentY += rowHeight + gapY;
        }

        return new LayoutResult(maxContentX + p.right(), maxContentY + p.bottom());
    }

    private LayoutResult arrangeVertical(DLGuiComponent host, List<DLGuiComponent> children) {
        Padding p = padding.get();
        int gapX = horizontalGap.get();
        int gapY = verticalGap.get();
        int maxH = host.height() - p.top() - p.bottom();
        
        int currentX = p.left();
        int maxContentX = 0;
        int maxContentY = 0;

        int index = 0;
        while (index < children.size()) {
            int currentColHeight = 0;
            int fixedUsedHeight = 0;
            int fillCount = 0;
            List<DLGuiComponent> colComponents = new ArrayList<>();

            while (index < children.size()) {
                DLGuiComponent child = children.get(index);
                FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
                
                if (fillCrossAxis.get() && !wrap.get()) {
                    child.setWidth(host.width() - p.left() - p.right());
                }

                int childH = child.height();
                int currentGap = (colComponents.isEmpty()) ? 0 : gapY;

                if (wrap.get() && !colComponents.isEmpty()) {
                    if (currentColHeight + currentGap + childH > maxH) {
                        break;
                    }
                }

                currentColHeight += currentGap + childH;
                colComponents.add(child);
                
                if (constraint == FlowConstraint.FILL) {
                    fillCount++;
                    fixedUsedHeight += currentGap;
                } else {
                    fixedUsedHeight += currentGap + childH;
                }

                index++;
                if (!wrap.get() && index < children.size()) continue;
            }

            // 2. Platzberechnung
            int colAvailableSpace = maxH - fixedUsedHeight;
            int fillHeight = 0;
            if (fillCount > 0) {
                fillHeight = Math.max(0, colAvailableSpace / fillCount);
            }

            // 3. Positionierung
            int startY = p.top();
            int endY = host.height() - p.bottom();
            int colWidth = 0;

            for (DLGuiComponent child : colComponents) {
                FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
                if (constraint == FlowConstraint.FILL) {
                    child.setHeight(fillHeight);
                }
                colWidth = Math.max(colWidth, child.width());
            }

            for (DLGuiComponent child : colComponents) {
                FlowConstraint constraint = getConstraintOrDefault(child, FlowConstraint.START);
                
                if (constraint == FlowConstraint.END) {
                    endY -= child.height();
                    child.setPosition(currentX, endY);
                    endY -= gapY;
                } else {
                    child.setPosition(currentX, startY);
                    startY += child.height() + gapY;
                }
                
                maxContentX = Math.max(maxContentX, child.x() + child.width());
                maxContentY = Math.max(maxContentY, child.y() + child.height());
            }

            currentX += colWidth + gapX;
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