package de.mrjulsen.mcdragonlib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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
}


