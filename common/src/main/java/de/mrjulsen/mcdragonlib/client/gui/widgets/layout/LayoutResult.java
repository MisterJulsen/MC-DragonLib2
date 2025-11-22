package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

public record LayoutResult(int contentWidth, int contentHeight) {
    
    public static final LayoutResult EMPTY = new LayoutResult(0, 0);

    public boolean causesOverflowX(int hostWidth) {
        return contentWidth > hostWidth;
    }
    
    public boolean causesOverflowY(int hostHeight) {
        return contentHeight > hostHeight;
    }
}
