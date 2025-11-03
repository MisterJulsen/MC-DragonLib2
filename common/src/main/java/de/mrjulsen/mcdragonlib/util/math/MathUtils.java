package de.mrjulsen.mcdragonlib.util.math;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MathUtils {
    private MathUtils() {}

    public static double proportion(double val, double max) {
        return (1D / max) * val;
    }

    public static double bounds(double value, double add, double min, double max) {
        if ((add > 0 && value >= max - add) || (add < 0 && value <= min + add)) {
            return value;
        }
        return value + add;
    }
    
    public static double round(double value, int decimals) {
        if (decimals < 0)
            throw new IllegalArgumentException();

        long factor = (long) java.lang.Math.pow(10, decimals);
        value = value * factor;
        long tmp = java.lang.Math.round(value);
        return (double) tmp / factor;
    }

    public static String printVector3f(Vector3fc vec) {
        return String.format("(%s, %s, %s)", vec.x(), vec.y(), vec.z());
    }

    /**
    * Creates a {@link Vec3i} from the passed {@code BlockPos}.
    * @param pos
    * @return
    */
    public static Vec3i blockPosToVec3i(BlockPos pos) {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
    * Creates a {@link Vec3} from the passed {@code BlockPos}.
    * @param pos
    * @return
    */
    public static Vec3 blockPosToVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
    * Creates a {@link Vector3f} from the passed {@code BlockPos}.
    * @param pos
    * @return
    */
    public static Vector3f blockPosToVector3f(BlockPos pos) {
        return new Vector3f(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos vector3iToBlockPos(Vector3ic vec) {
        return new BlockPos((int)vec.x(), (int)vec.y(), (int)vec.z());
    }

    public static BlockPos vec3ToBlockPos(Vec3 vec) {
        return new BlockPos((int)vec.x(), (int)vec.y(), (int)vec.z());
    }

    public static BlockPos vec3iToBlockPos(Vec3i vec) {
        return new BlockPos(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
    * Creates a {@link Vector3i} from the passed {@code BlockPos}.
    * @param pos
    * @return
    */
    public static Vector3i blockPosToVector3i(BlockPos pos) {
        return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Calculates the slope from point A to point B.
     * @param a
     * @param b
     * @return
     */
    public static double slope(Vector3f a, Vector3f b) {
        double heightDiff = java.lang.Math.max(a.y, b.y) - java.lang.Math.min(a.y, b.y);
        float dx = a.x - b.x;
        float dz = a.z - b.z;
        float horizontalDistance = (float) Math.sqrt(dx * dx + dz * dz);
        return horizontalDistance / heightDiff;
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);    
    }

    public static byte clamp(byte value, byte min, byte max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static long clamp(long value, long min, long max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static double getVectorAngle(Vector3f vec) {
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
    public static final Vector3f CENTER_OF_ORIGIN2 = new Vector3f(0.5f, 0.5f, 0.5f);

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

    public static boolean rectanglesIntersecting(double x1, double y1, double w1, double h1, double x2, double y2, double w2, double h2) {
        return (x1 < x2 + w2 && y1 < y2 + h2) && (x1 + w1 > x2 && y1 + h1 > y2);
    }

    public boolean isSectionInChunk(SectionPos section, ChunkPos chunk) {
        return section.getX() == chunk.x && section.getZ() == chunk.z;
    }

    public ChunkPos getChunkOfSection(SectionPos section) {
        return new ChunkPos(section.getX(), section.getZ());
    }







    public static Vec2 rotateY(Vec2 vec, double deg) {
		if (deg == 0)
			return vec;
		if (vec == Vec2.ZERO)
			return vec;

		float angle = (float) (deg / 180f * Math.PI);
		double sin = Mth.sin(angle);
		double cos = Mth.cos(angle);
		double x = vec.x;
		double y = vec.y;
        return new Vec2((float)(x * cos + y * sin), (float)(y * cos - x * sin));
	}
    
	public static VoxelShape moveShape(VoxelShape shape, Vec3 vec) {
        AABB[] aabbs = shape.toAabbs().toArray(AABB[]::new);
        VoxelShape[] shapes = new VoxelShape[aabbs.length];
        for (int i = 0; i < aabbs.length; i++) {
            shapes[i] = Shapes.create(moveAABB(aabbs[i], vec));
        }
        return Shapes.or(Shapes.empty(), shapes);
    }

    public static AABB moveAABB(AABB aabb, Vec3 vec) {
        return new AABB(aabb.minX + vec.x, aabb.minY + vec.y, aabb.minZ + vec.z, aabb.maxX + vec.x, aabb.maxY + vec.y, aabb.maxZ + vec.z);
    }

	public static VoxelShape rotateShape(VoxelShape shape, Axis axis, int degrees) {
        AABB[] aabbs = shape.toAabbs().toArray(AABB[]::new);
        VoxelShape[] shapes = new VoxelShape[aabbs.length];
        for (int i = 0; i < aabbs.length; i++) {
            shapes[i] = Shapes.create(rotateAABB(aabbs[i], axis, degrees));
        }
        return Shapes.or(Shapes.empty(), shapes);
    }

    public static AABB rotateAABB(AABB aabb, Axis axis, int degrees) {
        int normalizedDegrees = ((degrees % 360) + 360) % 360;
        if (normalizedDegrees == 0) return aabb;

        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;
        
        switch (axis) {
            case X:
                switch (normalizedDegrees) {
                    case 90:
                        return new AABB(minX, -maxZ, minY, maxX, -minZ, maxY);
                    case 180:
                        return new AABB(minX, -maxY, -maxZ, maxX, -minY, -minZ);
                    case 270:
                        return new AABB(minX, minZ, -maxY, maxX, maxZ, -minY);
                }
                break;
            case Y:
                switch (normalizedDegrees) {
                    case 90:
                        return new AABB(1f-maxZ, minY, minX, 1f-minZ, maxY, maxX);
                    case 180:
                        return new AABB(1f-maxX, minY, 1f-maxZ, 1f-minX, maxY, 1f-minZ);
                    case 270:
                        return new AABB(minZ, minY, 1f-maxX, maxZ, maxY, 1f-minX);
                }
                break;
            case Z:
                switch (normalizedDegrees) {
                    case 90:
                        return new AABB(-maxY, minX, minZ, -minY, maxX, maxZ);
                    case 180:
                        return new AABB(-maxX, -maxY, minZ, -minX, -minY, maxZ);
                    case 270:
                        return new AABB(minY, -maxX, minZ, maxY, -minX, maxZ);
                }
                break;
            default:
                throw new IllegalArgumentException("Axis must be 'x', 'y', or 'z'");
        }
        throw new IllegalArgumentException("Degrees must be 0, 90, 180, or 270");
    }


	public static VoxelShape scaleShape(VoxelShape shape, Axis axis, double factor, double pivot) {
        AABB[] aabbs = shape.toAabbs().toArray(AABB[]::new);
        VoxelShape[] shapes = new VoxelShape[aabbs.length];
        for (int i = 0; i < aabbs.length; i++) {
            shapes[i] = Shapes.create(scaleAABB(aabbs[i], axis, factor, pivot));
        }
        return Shapes.or(Shapes.empty(), shapes);
    }

    /**
     * Scales the AABB along an axis by a factor with an optional pivot.
     *
     * @param axis The axis along which scaling occurs.
     * @param factor The scaling factor (1.0 = no change, >1.0 = increase in size, <1.0 = decrease in size).
     * @param pivot The pivot point (default = 0.5 = center).
     * @return The scaled AABB.
     */
    public static AABB scaleAABB(AABB aabb, Axis axis, double factor, double pivot) {
        double min, max;
        switch (axis) {
            case X:
                min = aabb.minX;
                max = aabb.maxX;
                break;            
            case Y:
                min = aabb.minY;
                max = aabb.maxY;
                break;
            case Z:
                min = aabb.minZ;
                max = aabb.maxZ;
                break;
            default:
                throw new IllegalArgumentException("Axis must be 'x', 'y', or 'z'");
        }

        double minDiff = 0.5d - min;
        double maxDiff = max - 0.5d;
        double newMin = 0.5d - (factor * minDiff);
        double newMax = 0.5d + (factor * maxDiff);

        return switch (axis) {
            case X -> new AABB(newMin, aabb.minY, aabb.minZ, newMax, aabb.maxY, aabb.maxZ);
            case Y -> new AABB(aabb.minX, newMin, aabb.minZ, aabb.maxX, newMax, aabb.maxZ);
            case Z -> new AABB(aabb.minX, aabb.minY, newMin, aabb.maxX, aabb.maxY, newMax);
            default -> aabb;
        };
    }


	public static VoxelShape scaleShapeOneSide(VoxelShape shape, Axis axis, double factor, AxisDirection direction) {
        AABB[] aabbs = shape.toAabbs().toArray(AABB[]::new);
        VoxelShape[] shapes = new VoxelShape[aabbs.length];
        for (int i = 0; i < aabbs.length; i++) {
            shapes[i] = Shapes.create(scaleAABBOneSide(aabbs[i], axis, factor, direction));
        }
        return Shapes.or(Shapes.empty(), shapes);
    }

     /**
     * Scales the AABB along an axis, keeping one side fixed.
     *
     * @param axis The axis to scale along.
     * @param factor The scaling factor (>1.0 = increase, <1.0 = decrease).
     * @param direction The direction that remains fixed.
     * @return The rescaled AABB.
     */
    public static AABB scaleAABBOneSide(AABB aabb, Axis axis, double factor, AxisDirection direction) {
        double min, max;
        switch (axis) {
            case X:
                min = aabb.minX;
                max = aabb.maxX;
                break;            
            case Y:
                min = aabb.minY;
                max = aabb.maxY;
                break;
            case Z:
                min = aabb.minZ;
                max = aabb.maxZ;
                break;
            default:
                throw new IllegalArgumentException("Axis must be 'x', 'y', or 'z'");
        }

        double minDiff = 0.5d - min;
        double maxDiff = max - 0.5d;
        double newMin = direction == AxisDirection.NEGATIVE ? 0.5d - (factor * minDiff) : min;
        double newMax = direction == AxisDirection.POSITIVE ? 0.5d + (factor * maxDiff) : max;

        return switch (axis) {
            case X -> new AABB(newMin, aabb.minY, aabb.minZ, newMax, aabb.maxY, aabb.maxZ);
            case Y -> new AABB(aabb.minX, newMin, aabb.minZ, aabb.maxX, newMax, aabb.maxZ);
            case Z -> new AABB(aabb.minX, aabb.minY, newMin, aabb.maxX, aabb.maxY, newMax);
            default -> aabb;
        };
    }

    public static int checkPointPosition(Vec2 pointA, Vec2 pointB, Vec2 pointP) {
        return (int)Math.signum((pointB.x - pointA.x) * (pointP.y - pointA.y) - (pointB.y - pointA.y) * (pointP.x - pointA.x));
    }

    public static Vector3f rotateToDirection(Vector3f v, Vector3f dir) {
        Vector3f direction = new Vector3f(dir).normalize();
        Vector3f xAxis = new Vector3f(1, 0, 0);
        if (xAxis.equals(direction, 1e-6f)) {
            return new Vector3f(v);
        }

        if (xAxis.equals(new Vector3f(direction).negate(), 1e-6f)) {
            Quaternionf q180 = new Quaternionf().fromAxisAngleRad(0, 0, 1, (float)Math.PI);
            return q180.transform(new Vector3f(v));
        }

        Vector3f axis = xAxis.cross(direction, new Vector3f()).normalize();
        float angle = (float)Math.acos(xAxis.dot(direction));
        Quaternionf rotation = new Quaternionf().fromAxisAngleRad(axis, angle);
        return rotation.transform(new Vector3f(v));
    }

    
    public static Vector3f rotate(Vector3f vec, Vector3f rotationVec) {
		return rotate(vec, rotationVec.x, rotationVec.y, rotationVec.z);
	}

	public static Vector3f rotate(Vector3f vec, double xRot, double yRot, double zRot) {
		return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
	}

	public static Vector3f rotateCentered(Vector3f vec, double deg, Axis axis) {
		Vector3f shift = getCenterOf(new Vector3i());
		return rotate(new Vector3f(vec).sub(shift), deg, axis).add(shift);
	}

	public static Vector3f rotate(Vector3f vec, double deg, Axis axis) {
		if (deg == 0)
			return vec;

		float angle = (float) (deg / 180f * Math.PI);
		float sin = Mth.sin(angle);
		float cos = Mth.cos(angle);
		float x = vec.x;
		float y = vec.y;
		float z = vec.z;

		if (axis == Axis.X)
			return new Vector3f(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new Vector3f(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new Vector3f(x * cos - y * sin, y * cos + x * sin, z);
		return vec;
	}

	public static Vector3f getCenterOf(Vector3i pos) {
		if (pos.equals(new Vector3i()))
			return CENTER_OF_ORIGIN2;
		return new Vector3f(pos).add(.5f, .5f, .5f);
	}


    public static Vector3f centerOf(Vector3f... points) {
        if (points == null || points.length == 0) {
            return null;
        }

        Vector3f sum = new Vector3f(0, 0, 0);

        for (Vector3f v : points) {
            sum.add(v);
        }

        sum.div(points.length);

        return sum;
    }

    public static double snap(double x, double a) {
        if (a <= 0) throw new IllegalArgumentException("a must be > 0");
        return Math.floor(x / a) * a;
    }

    public static double snapNearest(double x, double a) {
        if (a <= 0) throw new IllegalArgumentException("a must be > 0");
        return Math.round(x / a) * a;
    }
}


