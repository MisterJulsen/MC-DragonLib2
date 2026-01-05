package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import java.util.ArrayList;
import java.util.List;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

import java.lang.Math; 

/**
 * A flexible flow layout that arranges children either horizontally or vertically.
 *
 * <p>Features:
 * <ul>
 * <li>Direction: controls whether components flow horizontally (rows) or vertically (columns).</li>
 * <li>Wrap: when enabled, components wrap to the next line/column when they exceed the available
 *         primary-axis space; when disabled all components are laid out on a single line/column.</li>
 * <li>FlowConstraint: per-child constraint (provided via the child's {@code layoutContraint})
 *                   that controls per-line alignment and fill behavior.</li>
 * <li>fillCrossAxis: when true and wrap is disabled, children will be stretched across the cross axis
 *                  to fill the host's available cross-axis space.</li>
 * </ul>
 *
 * <p>Spacing and padding:
 * <ul>
 * <li>horizontalGap and verticalGap control spacing between components along the respective axes.</li>
 * <li>padding reserves outer space and reduces the available layout rectangle.</li>
 * </ul>
 *
 * <p>Per-child constraints:
 * <ul>
 * <li>START: the default; components are placed at the current "start" coordinate for the line/col.</li>
 * <li>END: the component is placed flush to the "end" side of the current line/col (right or bottom
 *        for horizontal/vertical flows respectively) and subsequent positioning accounts for the gap.</li>
 * <li>FILL: the component requests the remaining free space in the current line/column; when multiple
 *         FILL components exist they split free space evenly. FILL components keep their cross-axis
 *         size unless {@code fillCrossAxis} is enabled.</li>
 * </ul>
 *
 * <p>Notes on constraints:
 * <ul>
 * <li>A child's constraint must be an instance of {@link FlowConstraint} accessible via its
 *     {@code layoutContraint} property. If no valid constraint is present the constraint defaults to
 *     {@link FlowConstraint#START}.</li>
 * </ul>
 *
 * <p>Return:
 * <ul>
 * <li>The method returns a {@link LayoutResult} describing the bounding rectangle used by the
 *     arranged children, including padding right/bottom to reflect final required content area.</li>
 * </ul>
 */
public class FlowLayout implements ILayoutManager {
    
    /**
     * Flow direction: horizontal rows or vertical columns.
     */
    public enum Direction { VERTICAL, HORIZONTAL }

    /**
     * Per-child constraint controlling alignment and fill behavior within a line/column.
     *
     * <ul>
     * <li>START: normal placement at the beginning of the line/column.</li>
     * <li>END: placement anchored to the end of the current line/column.</li>
     * <li>FILL: component receives an allocated portion of remaining primary-axis space.</li>
     * </ul>
     */
    public enum FlowConstraint { START, END, FILL }

    /**
     * Primary flow direction; defaults to HORIZONTAL.
     */
    public final Property<Direction> flowDirection = new Property<>(Direction.HORIZONTAL);

    /**
     * Whether children wrap onto new lines/columns when space runs out.
     */
    public final BooleanProperty wrap = new BooleanProperty(true);

    /**
     * Horizontal gap in pixels between adjacent components.
     */
    public final NumberProperty<Integer> horizontalGap = new NumberProperty<>(0); 

    /**
     * Vertical gap in pixels between adjacent components.
     */
    public final NumberProperty<Integer> verticalGap = new NumberProperty<>(0);

    /**
     * Outer padding applied around the flow area.
     */
    public final Property<Padding> padding = new Property<>(Padding.ZERO);

    /**
     * When true and wrapping is disabled, children will be stretched across the cross axis
     * to fill the host cross-axis available space.
     */
    public final BooleanProperty fillCrossAxis = new BooleanProperty(false);

    /**
     * Arrange children according to the configured flow direction and constraints.
     *
     * <p>When horizontal, children are grouped into rows; when vertical, into columns. Within each
     * row/column the algorithm:
     * <ul>
     * <li>collects components for the line (respecting wrap and available space),</li>
     * <li>calculates space to allocate for FILL components,</li>
     * <li>assigns positions for START and END components, and</li>
     * <li>updates the running primary and cross axis offsets for the next line/column.</li>
     * </ul>
     *
     * @param host the parent container providing available width/height for layout.
     * @return a {@link LayoutResult} with the width and height required by arranged children.
     */
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