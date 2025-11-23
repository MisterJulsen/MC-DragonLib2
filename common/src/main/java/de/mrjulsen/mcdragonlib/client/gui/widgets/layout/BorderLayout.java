package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding; // Import hinzugefügt

import java.util.List;
import java.lang.Math;

public class BorderLayout implements ILayoutManager {

    public enum BorderPosition {
        NORTH, SOUTH, WEST, EAST, CENTER
    }
    
    private int hGap = 0;
    private int vGap = 0;
    
    private Padding padding = Padding.ZERO;

    public BorderLayout(int hGap, int vGap) {
        this.hGap = hGap;
        this.vGap = vGap;
    }
    
    public void setPadding(Padding padding) {
        this.padding = padding;
    }

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