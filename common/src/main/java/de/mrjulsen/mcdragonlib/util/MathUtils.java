package de.mrjulsen.mcdragonlib.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public final class MathUtils {
    
    public static double round(double value, int decimals) {
        if (decimals < 0)
            throw new IllegalArgumentException();

        long factor = (long) java.lang.Math.pow(10, decimals);
        value = value * factor;
        long tmp = java.lang.Math.round(value);
        return (double) tmp / factor;
    }

    /**
    * Creates a {@code Vec3i} from the passed {@code BlockPos}.
    * @param pos
    * @return
    */
    public static Vec3i blockPosToVec3i(BlockPos pos) {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
    * Creates a {@code Vec3} from the passed {@code BlockPos}.
    * @param pos
    * @return
    */
    public static Vec3 blockPosToVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Calculates the slope from point A to point B.
     * @param a
     * @param b
     * @return
     */
    public static double slope(Vec3 a, Vec3 b) {
        double heightDiff = java.lang.Math.max(a.y, b.y) - java.lang.Math.min(a.y, b.y);
        Vec3 vec = b.subtract(a);
        double distance = vec.horizontalDistance();
        return distance / heightDiff;
    }

    public static double lerp(double pDelta, double pStart, double pEnd) {
        return pStart + pDelta * (pEnd - pStart);    
    }

    public static byte clamp(byte pValue, byte pMin, byte pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static int clamp(int pValue, int pMin, int pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static long clamp(long pValue, long pMin, long pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static double clamp(double pValue, double pMin, double pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static double getVectorAngle(Vec3 vec) {
        return Math.round(Math.atan2(vec.x(), -vec.z()) * (180.0 / Math.PI));
    }

    private static double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
        double scale = Math.min(maxWidth / fontWidth, 1.0D);
        return Math.max(maxScale * scale, minScale);
    }

    public static double getScale(float fontWidth, float lineWidth, float min, float max) {
        return calcScale(min, max, lineWidth / max, fontWidth);
    }  

    public static double calculateMedian(Queue<Double> database, double smoothingThreshold, Predicate<Double> filter) {
        if (database.isEmpty()) {
            return 0;
        }

        List<Double> values = new LinkedList<>();
        for (double i : database) {
            if (!filter.test(i)) 
                continue;

            values.add(i);
        }

        Collections.sort(values);
        double median = 0;
        if (values.size() % 2 == 0) {
            median = ((double)values.get(values.size() / 2) + (double)values.get(values.size() / 2 + 1)) / 2D;
        }
        median = values.get(values.size() / 2);

        final double med = median;
        return database.stream().mapToDouble(x -> x).filter(x -> Math.abs(med - x) <= smoothingThreshold).average().orElse(0);
    }

    public static final Vec3 CENTER_OF_ORIGIN = new Vec3(0.5f, 0.5f, 0.5f);

    public static Vec3 rotate(Vec3 vec, Vec3 rotationVec) {
        return rotate(vec, rotationVec.x, rotationVec.y, rotationVec.z);
    }

    public static Vec3 rotate(Vec3 vec, double xRot, double yRot, double zRot) {
        return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
    }

    public static Vec3 rotateCentered(Vec3 vec, double deg, Axis axis) {
        Vec3 shift = getCenterOf(BlockPos.ZERO);
        return rotate(vec.subtract(shift), deg, axis).add(shift);
    }

    public static Vec3 rotate(Vec3 vec, double deg, Axis axis) {
        if (deg == 0)
            return vec;
        if (vec == Vec3.ZERO)
            return vec;

        float angle = (float) (deg / 180f * Math.PI);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;

        if (axis == Axis.X)
            return new Vec3(x, y * cos - z * sin, z * cos + y * sin);
        if (axis == Axis.Y)
            return new Vec3(x * cos + z * sin, y, z * cos - x * sin);
        if (axis == Axis.Z)
            return new Vec3(x * cos - y * sin, y * cos + x * sin, z);
        return vec;
    }

    public static Vec3 getCenterOf(Vec3i pos) {
        if (pos.equals(Vec3i.ZERO))
            return CENTER_OF_ORIGIN;
        return Vec3.atLowerCornerOf(pos).add(0.5f, 0.5f, 0.5f);
    }
}


