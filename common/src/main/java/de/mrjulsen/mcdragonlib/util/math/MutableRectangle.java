package de.mrjulsen.mcdragonlib.util.math;

public class MutableRectangle extends Rectangle {

    protected MutableRectangle(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }

    public void setX1(int x) {
        this.x1 = x;
    }

    public void setY1(int y) {
        this.y1 = y;
    }

    public void setX2(int x) {
        this.x2 = x;
    }

    public void setY2(int y) {
        this.y1 = y;
    }

    public void setWidth(int w) {
        this.x2 = x1 + w;
    }

    public void setHeight(int h) {
        this.y2 = y1 + h;
    }
    
}
