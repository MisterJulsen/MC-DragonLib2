package de.mrjulsen.mcdragonlib.util.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

/**
 * Utility helpers for coarse-grained ray tracing across blocks using an ICollisionProvider.
 *
 * <p>The provided rayTrace method steps along a ray and queries the collision provider
 * for blocks within the step neighbourhood. It returns the closest hit encountered.
 */
public class RaycastUtils {

    /**
     * Perform a stepped ray trace from start to end, testing candidate blocks via the provided collisionProvider.
     *
     * <p>The method moves in fixed increments (stepSize) along the ray and tests all blocks overlapping
     * the radius at the current sample point. The first (closest) hit is returned.
     *
     * @param start ray origin (world coordinates)
     * @param end ray end (world coordinates)
     * @param level Minecraft level context used for block/world queries
     * @param radius search radius around each sample point
     * @param stepSize sampling step distance along the ray
     * @param collisionProvider provider used to determine per-block hits
     * @return Optional containing the closest RaycastHitResult if any hit was found
     */
    public static Optional<RaycastHitResult> rayTrace(Vector3f start, Vector3f end, Level level, float radius, float stepSize, ICollisionProvider collisionProvider) {
        Vector3f direction = new Vector3f(end).sub(start);
        Vector3f normal = new Vector3f(direction).normalize();
        Vector3f step = new Vector3f(normal).mul(stepSize);
        float length = direction.length();

        float distance = 0f;
        Vector3f current = new Vector3f(start);
        HashSet<BlockPos> checkedBlocks = new HashSet<>();

        RaycastHitResult closestHit = null;

        while (distance < length) {
            current.add(step);

            Iterator<BlockPos> positions = BlockPos.betweenClosed(
                (int)Math.floor(current.x() - radius), (int)Math.floor(current.y() - radius), (int)Math.floor(current.z() - radius),
                (int)Math.floor(current.x() + radius), (int)Math.floor(current.y() + radius), (int)Math.floor(current.z() + radius)
            ).iterator();

            while (positions.hasNext()) {
                BlockPos pos = new BlockPos(positions.next());
                if (checkedBlocks.add(pos)) {
                    Optional<RaycastHitResult> hit = collisionProvider.tryHit(level, pos, new Vector3f(start), new Vector3f(normal));
                    if (hit.isPresent()) {
                        if (closestHit == null || hit.get().getDistance() < closestHit.getDistance()) {
                            closestHit = hit.get();
                        }
                    }
                }
            }
            if (closestHit != null) {
                return Optional.of(closestHit);
            }

            distance += stepSize;
        }

        return Optional.ofNullable(closestHit);
    }

}


