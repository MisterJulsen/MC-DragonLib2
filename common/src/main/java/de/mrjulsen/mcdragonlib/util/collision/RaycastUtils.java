package de.mrjulsen.mcdragonlib.util.collision;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility helpers for ray tracing across blocks using an {@link IRayCollisionProvider}.
 */
public class RaycastUtils {

    /**
     * Trace a ray from {@code start} to {@code end} and return the closest hit.
     *
     * <p>The blocks along the ray are visited exactly in the order the ray passes through them
     * (voxel traversal after Amanatides &amp; Woo). For each visited cell, all blocks within
     * {@code ceil(radius)} cells are tested via the provider (each block only once).
     * The trace stops as soon as no untested block can produce a closer hit, provided that hits lie
     * within their block's cell extended by {@code radius}. Hits beyond {@code end} are ignored.
     * Blocks in unloaded chunks are skipped.
     *
     * @param start ray origin (world coordinates)
     * @param end ray end (world coordinates)
     * @param level level context passed to the provider
     * @param radius additional search radius around the ray (e.g. for thick shapes), {@code >= 0}
     * @param collisionProvider provider used to determine per-block hits
     * @return the closest hit, if any
     */
    public static Optional<RaycastHitResult> rayTrace(Vector3dc start, Vector3dc end, Level level, double radius, IRayCollisionProvider collisionProvider) {
        Objects.requireNonNull(collisionProvider);
        if (radius < 0 || !Double.isFinite(radius)) {
            throw new IllegalArgumentException("Radius must be a finite value >= 0, but was " + radius);
        }

        Vector3dc delta = new Vector3d(end).sub(start);
        double length = delta.length();
        if (!Double.isFinite(length) || length < 1.0E-7) {
            return Optional.empty();
        }
        Vector3dc dir = new Vector3d(delta).mul(1D / length);
        int padding = Mth.ceil(radius);

        int x = Mth.floor(start.x());
        int y = Mth.floor(start.y());
        int z = Mth.floor(start.z());

        int stepX = (int) Math.signum(dir.x());
        int stepY = (int) Math.signum(dir.y());
        int stepZ = (int) Math.signum(dir.z());

        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.x());
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.y());
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.z());

        double tMaxX = initialTMax(start.x(), x, stepX, tDeltaX);
        double tMaxY = initialTMax(start.y(), y, stepY, tDeltaY);
        double tMaxZ = initialTMax(start.z(), z, stepZ, tDeltaZ);

        LongSet checked = new LongOpenHashSet();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        RaycastHitResult closest = null;
        double tEnter = 0;

        while (tEnter <= length) {
            if (closest != null && tEnter > closest.getPreciseDistance()) {
                break;
            }

            for (int dx = -padding; dx <= padding; dx++) {
                for (int dy = -padding; dy <= padding; dy++) {
                    for (int dz = -padding; dz <= padding; dz++) {
                        mutable.set(x + dx, y + dy, z + dz);
                        if (!checked.add(mutable.asLong()) || !level.isLoaded(mutable)) {
                            continue;
                        }
                        BlockPos pos = mutable.immutable();
                        Optional<RaycastHitResult> hit = collisionProvider.tryHit(level, pos, start, dir, length);
                        if (hit.isPresent()) {
                            RaycastHitResult h = hit.get();
                            double d = h.getPreciseDistance();
                            if (d >= 0 && d <= length && (closest == null || d < closest.getPreciseDistance())) {
                                closest = h;
                            }
                        }
                    }
                }
            }

            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                x += stepX;
                tEnter = tMaxX;
                tMaxX += tDeltaX;
            } else if (tMaxY <= tMaxZ) {
                y += stepY;
                tEnter = tMaxY;
                tMaxY += tDeltaY;
            } else {
                z += stepZ;
                tEnter = tMaxZ;
                tMaxZ += tDeltaZ;
            }
        }

        return Optional.ofNullable(closest);
    }

    /**
     * Trace a thin ray (radius 0) from {@code start} to {@code end}.
     *
     * @see #rayTrace(Vector3dc, Vector3dc, Level, double, IRayCollisionProvider)
     */
    public static Optional<RaycastHitResult> rayTrace(Vector3dc start, Vector3dc end, Level level, IRayCollisionProvider collisionProvider) {
        return rayTrace(start, end, level, 0, collisionProvider);
    }

    /**
     * Perform a ray trace from start to end, testing candidate blocks via the provided collisionProvider.
     *
     * @param start ray origin (world coordinates)
     * @param end ray end (world coordinates)
     * @param level Minecraft level context used for block/world queries
     * @param radius search radius around the ray
     * @param stepSize ignored. The trace now uses exact voxel traversal.
     * @param collisionProvider provider used to determine per-block hits
     * @return Optional containing the closest RaycastHitResult if any hit was found
     * @deprecated Use {@link #rayTrace(Vector3dc, Vector3dc, Level, double, IRayCollisionProvider)}.
     */
    @Deprecated
    public static Optional<RaycastHitResult> rayTrace(Vector3f start, Vector3f end, Level level, float radius, float stepSize, @SuppressWarnings("deprecation") ICollisionProvider collisionProvider) {
        return rayTrace(new Vector3d(start), new Vector3d(end), level, radius, collisionProvider);
    }

    /**
     * Returns the face a ray with the given direction hits first (the side facing against the direction).
     */
    public static Direction faceFromDirection(Vector3dc direction) {
        return Direction.getNearest(direction.x(), direction.y(), direction.z()).getOpposite();
    }

    private static double initialTMax(double origin, int cell, int step, double tDelta) {
        if (step > 0) {
            return (cell + 1 - origin) * tDelta;
        } else if (step < 0) {
            return (origin - cell) * tDelta;
        }
        return Double.POSITIVE_INFINITY;
    }
}
