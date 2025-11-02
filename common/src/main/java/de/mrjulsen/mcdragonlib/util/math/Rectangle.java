package de.mrjulsen.mcdragonlib.util.math;

import java.util.Collection;
import java.util.Objects;

public class Rectangle {

    public static final double MAX_DOUBLE = Double.MAX_VALUE / 2D;
    public static final Rectangle INFINITE = new Rectangle(-MAX_DOUBLE, -MAX_DOUBLE, MAX_DOUBLE, MAX_DOUBLE);
    public static final Rectangle EMPTY = new Rectangle(0, 0, 0, 0);

    protected double x1;
    protected double y1;
    protected double x2;
    protected double y2;

    protected Rectangle(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public static Rectangle withPoints(double x1, double y1, double x2, double y2) {        
        if (Double.isInfinite(x1) || Double.isInfinite(y1) ||
            Double.isInfinite(x2) || Double.isInfinite(y2)) {
            throw new IllegalArgumentException("Rectangle does not support infinite values.");
        }
        return new Rectangle(x1, y1, x2, y2);
    }

    public static Rectangle withSize(double x, double y, double w, double h) {
        return withPoints(x, y, Math.max(x + w, 0), Math.max(y + h, 0));
    }
    
    public static Rectangle surrounding(Collection<Rectangle> rectangles) {
        return surrounding(rectangles.toArray(Rectangle[]::new));
    }


    public Rectangle scale(double factor) {
        return new Rectangle(x() * factor, y() * factor, width() * factor, height() * factor);
    }


    public static Rectangle offset(Rectangle rect, double dx, double dy) {
        double newX = rect.x() + dx;
        double newY = rect.y() + dy;
        double width = rect.width();
        double height = rect.height();

        if (newX < -MAX_DOUBLE) {
            double overflow = -MAX_DOUBLE - newX;
            newX = -MAX_DOUBLE;
            width = Math.max(0, width - overflow);
        } else if (newX + width > MAX_DOUBLE) {
            double overflow = (newX + width) - MAX_DOUBLE;
            width = Math.max(0, width - overflow);
        }

        if (newY < -MAX_DOUBLE) {
            double overflow = -MAX_DOUBLE - newY;
            newY = -MAX_DOUBLE;
            height = Math.max(0, height - overflow);
        } else if (newY + height > MAX_DOUBLE) {
            double overflow = (newY + height) - MAX_DOUBLE;
            height = Math.max(0, height - overflow);
        }

        return Rectangle.withSize(newX, newY, width, height);
    }

    public static Rectangle surroundingBase(Rectangle base, Rectangle... rectangles) {
        Rectangle[] rects = new Rectangle[rectangles.length + 1];
        System.arraycopy(rectangles, 0, rects, 1, rectangles.length);
        rects[0] = base;
        return surrounding(rects);
    }

    public static Rectangle surrounding(Rectangle... rectangles) {
        double x1 = MAX_DOUBLE;
        double y1 = MAX_DOUBLE;
        double x2 = -MAX_DOUBLE;
        double y2 = -MAX_DOUBLE;
        for (Rectangle rect : rectangles) {
            x1 = Math.min(x1, rect.x1);
            y1 = Math.min(y1, rect.y1);
            x2 = Math.max(x2, rect.x2);
            y2 = Math.max(y2, rect.y2);
        }
        return withPoints(x1, y1, x2, y2);
    }

    public static Rectangle intersection(Collection<Rectangle> rectangles) {
        return intersection(rectangles.toArray(Rectangle[]::new));
    }

    public static Rectangle intersectionBase(Rectangle base, Rectangle... rectangles) {
        Rectangle[] rects = new Rectangle[rectangles.length + 1];
        System.arraycopy(rectangles, 0, rects, 1, rectangles.length);
        rects[0] = base;
        return intersection(rects);
    }

    public static Rectangle intersection(Rectangle... rectangles) {
        double x1 = -MAX_DOUBLE;
        double y1 = -MAX_DOUBLE;
        double x2 = MAX_DOUBLE;
        double y2 = MAX_DOUBLE;
        for (Rectangle rect : rectangles) {
            x1 = Math.max(x1, rect.x1);
            y1 = Math.max(y1, rect.y1);
            x2 = Math.min(x2, rect.x2);
            y2 = Math.min(y2, rect.y2);
        }

        if (x2 < x1 || y2 < y1) {
            return Rectangle.EMPTY;
        }
        return withPoints(x1, y1, x2, y2);
    }

    public boolean collision(Point point) {
        return collision(point.x(), point.y());
    }

    public boolean collision(double x, double y) {
        if (this == EMPTY) {
            return false;
        }
        return left() <= x && right() >= x && top() <= y && bottom() >= y;
    }

    public boolean collision(Rectangle rectangle) {
        if (this == EMPTY || rectangle == EMPTY) {
            return false;
        }
        return right() >= rectangle.left() && left() <= rectangle.right() && bottom() >= rectangle.top() && top() <= rectangle.bottom();
    }

    public double left() {
        return x1;
    }

    public double top() {
        return y1;
    }

    public double right() {
        return x2;
    }

    public double bottom() {
        return y2;
    }

    public double x() {
        return left();
    }

    public double y() {
        return top();
    }

    public double width() {
        return right() - left();
    }

    public double height() {
        return bottom() - top();
    }

    @Override
    public String toString() {
        return String.format("Rectangle[x1=%s,y1=%s,x2=%s,y2=%s,w=%s,h=%s]", left(), top(), right(), bottom(), width(), height());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle o) {
            return x1 == o.x1 && y1 == o.y1 && x2 == o.x2 && y2 == o.y2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(x1, y1, x2, y2);
    }
}