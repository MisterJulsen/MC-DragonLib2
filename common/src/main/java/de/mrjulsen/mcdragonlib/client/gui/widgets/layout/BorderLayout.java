package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;

import java.util.List;

public class BorderLayout implements ILayoutManager {

    public enum BorderPosition {
        NORTH, SOUTH, WEST, EAST, CENTER
    }
    
    private int hGap = 0;
    private int vGap = 0;

    public BorderLayout(int hGap, int vGap) {
        this.hGap = hGap;
        this.vGap = vGap;
    }

    @Override
    public void arrangeComponents(DLGuiComponent host) {
        List<DLGuiComponent> children = host.getComponents();
        
        int top = 0;
        int bottom = host.height();
        int left = 0;
        int right = host.width();
        
        for (DLGuiComponent child : children) {
            BorderPosition pos = getConstraintOrDefault(child, BorderPosition.CENTER);
            if (pos == null) pos = BorderPosition.CENTER;

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
        }

        for (DLGuiComponent child : children) {
            BorderPosition pos = getConstraintOrDefault(child, BorderPosition.CENTER);
            if (pos == BorderPosition.CENTER || pos == null) {
                child.setPosition(left, top);
                child.setSize(Math.max(0, right - left), Math.max(0, bottom - top));
            }
        }
    }

    private BorderPosition getConstraintOrDefault(DLGuiComponent child, BorderPosition def) {
        if (child.layoutContraint != null && child.layoutContraint.get() instanceof BorderPosition) {
            return (BorderPosition) child.layoutContraint.get();
        }
        return def;
    }
}