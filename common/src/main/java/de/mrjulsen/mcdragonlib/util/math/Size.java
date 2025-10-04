package de.mrjulsen.mcdragonlib.util.math;

import java.util.Objects;

public class Size {

    public static final Size EMPTY = new Size(0, 0);
    public static final Size INFINITY = new Size(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    
    protected double w;
    protected double h;

    protected Size(double w, double h) {
        this.w = w;
        this.h = h;
    }

    public static Size of(double w, double h) {
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException(String.format("width and height must not be negative! width=%s, height=%s", w, h));
        }
        return new Size(w, h);
    }

    public double w() {
        return w;
    }

    public double h() {
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Size o) {
            return w == o.w && h == o.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(w, h);
    }

    @Override
    public String toString() {
        return String.format("Size[xw=%s,h=%s]", w, h);
    }
}
