package de.mrjulsen.mcdragonlib.util.math;

import java.util.Objects;

public class Point {
    
    protected double x;
    protected double y;

    protected Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public static double distance(Point a, Point b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        double dX = Math.abs(a.x() - b.x());
        double dY = Math.abs(a.y() - b.y());
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }

    public static Point center(Point... points) {
        Objects.requireNonNull(points);
        double sumX = 0;
        double sumY = 0;
        for (Point p : points) {
            sumX += p.x;
            sumY += p.y;
        }
        double mX = sumX / points.length;
        double mY = sumY / points.length;
        return new Point(mX, mY);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point o) {
            return x == o.x && y == o.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("Point[x=%s,y=%s]", x, y);
    }
}
