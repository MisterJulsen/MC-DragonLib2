package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding; // Import hinzugefügt

import java.util.List;
import java.lang.Math;

/**
 * A layout manager that arranges child components in five logical regions:
 * NORTH, SOUTH, WEST, EAST and CENTER.
 *
 * <p>Behavior summary:
 * <ul>
 * <li>Children with a BorderPosition constraint are placed into the requested region.</li>
 * <li>NORTH and SOUTH components span the available horizontal space between the left and right
 *     padding and are stacked from the top / bottom inward.</li>
 * <li>WEST and EAST components span the available vertical space between the top and bottom
 *     padding and are stacked from the left / right inward.</li>
 * <li>The CENTER component (or any child without an explicit BorderPosition constraint) occupies
 *     the remaining rectangle after the other regions are laid out.</li>
 * </ul>
 *
 * <p>Spacing and padding:
 * <ul>
 * <li>hGap and vGap control the spacing between WEST/EAST and NORTH/SOUTH regions respectively.</li>
 * <li>A Padding instance may be set to reserve space on each side of the host area.</li>
 * </ul>
 *
 * <p>Constraints:
 * <ul>
 * <li>Each child may provide a constraint via its {@code layoutContraint} property.
 *     If that value is an instance of {@link BorderPosition} it will be used; otherwise the
 *     child is treated as CENTER.</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 * <li>This layout mutates child positions and sizes directly via DLGuiComponent setters.</li>
 * <li>The returned {@link LayoutResult} reports the computed content width/height (including
 *     applied right/bottom padding) which can be used to detect overflow.</li>
 * </ul>
 */
public class BorderLayout implements ILayoutManager {

    /**
     * Enumeration of the five supported border positions.
     *
     * <p>Semantics:
     * <ul>
     * <li>NORTH: placed at the top, width stretched to available width.</li>
     * <li>SOUTH: placed at the bottom, width stretched to available width.</li>
     * <li>WEST: placed at left, height stretched to available height.</li>
     * <li>EAST: placed at right, height stretched to available height.</li>
     * <li>CENTER: occupies remaining area after all others are positioned.</li>
     * </ul>
     */
    public enum BorderPosition {
        NORTH, SOUTH, WEST, EAST, CENTER
    }
    
    private int hGap = 0;
    private int vGap = 0;
    
    private Padding padding = Padding.ZERO;

    /**
     * Create a BorderLayout with the given horizontal and vertical gaps.
     *
     * @param hGap horizontal gap in pixels inserted between WEST/EAST columns and adjacent regions.
     * @param vGap vertical gap in pixels inserted between NORTH/SOUTH rows and adjacent regions.
     */
    public BorderLayout(int hGap, int vGap) {
        this.hGap = hGap;
        this.vGap = vGap;
    }
    
    /**
     * Set the padding which reserves space on the host's edges before laying out children.
     *
     * <p>The padding's top/left reduce the starting coordinates for layout and its bottom/right
     * are taken into account when computing the layout result size.
     *
     * @param padding the padding to apply; must not be null (use {@code Padding.ZERO} if none).
     */
    public void setPadding(Padding padding) {
        this.padding = padding;
    }

    /**
     * Arrange all direct child components inside the given host according to the border regions.
     *
     * <p>Algorithm summary:
     * 1. Initialize available rectangle using host size minus padding.
     * 2. Iterate children and place NORTH/SOUTH/WEST/EAST components, shrinking the available
     *    rectangle inward as each component consumes space.
     * 3. After the edges are placed, place the CENTER child(ren) to fill the final remaining area.
     *
     * <p>Important details:
     * - When placing NORTH or SOUTH children their width is set to the current available width.
     * - When placing WEST or EAST children their height is set to the current available height.
     * - Children without an explicit BorderPosition are treated as CENTER.
     * - The method returns a {@link LayoutResult} whose contentWidth and contentHeight include the
     *   right and bottom padding values to reflect the full required content area.
     *
     * @param host the parent component whose children will be arranged; layout reads host width/height.
     * @return a {@link LayoutResult} describing the content size produced by the layout.
     */
    @Override
    public LayoutResult arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        
        if (children.isEmpty()) {
            return LayoutResult.EMPTY;
        }
        
        int top = padding.top();
        int bottom = host.height() - padding.bottom();
        int left = padding.left();
        int right = host.width() - padding.right();
        
        int maxContentX = 0;
        int maxContentY = 0;

        for (DLGuiComponent child : children) {
            BorderPosition pos = getConstraintOrDefault(child, BorderPosition.CENTER);
            if (pos == null) pos = BorderPosition.CENTER; 

            if (pos == BorderPosition.CENTER) continue;

            if (pos == BorderPosition.NORTH) {
                child.setPosition(left, top);
                child.setWidth(right - left);
                top += child.height() + vGap;
                
            } else if (pos == BorderPosition.SOUTH) {
                int h = child.height();
                child.setPosition(left, bottom - h);
                child.setWidth(right - left);
                bottom -= (h + vGap);
                
            } else if (pos == BorderPosition.WEST) {
                child.setPosition(left, top);
                child.setHeight(bottom - top);
                left += child.width() + hGap;
                
            } else if (pos == BorderPosition.EAST) {
                int w = child.width();
                child.setPosition(right - w, top);
                child.setHeight(bottom - top);
                right -= (w + hGap);
            }

            maxContentX = Math.max(maxContentX, child.x() + child.width());
            maxContentY = Math.max(maxContentY, child.y() + child.height());
        }

        for (DLGuiComponent child : children) {
            BorderPosition pos = getConstraintOrDefault(child, BorderPosition.CENTER);
            
            if (pos == BorderPosition.CENTER || pos == null) {
                int remainingW = Math.max(0, right - left);
                int remainingH = Math.max(0, bottom - top);

                child.setPosition(left, top);
                child.setSize(remainingW, remainingH);

                maxContentX = Math.max(maxContentX, child.x() + child.width());
                maxContentY = Math.max(maxContentY, child.y() + child.height());
            }
        }

        return new LayoutResult(maxContentX + padding.right(), maxContentY + padding.bottom());
    }

    private BorderPosition getConstraintOrDefault(DLGuiComponent child, BorderPosition def) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof BorderPosition) {
            return (BorderPosition) child.layoutContraint.get();
        }
        return def;
    }
}