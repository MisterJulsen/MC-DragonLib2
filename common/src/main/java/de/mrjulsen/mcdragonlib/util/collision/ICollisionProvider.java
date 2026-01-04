package de.mrjulsen.mcdragonlib.util.collision;

import java.util.Optional;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Abstraction that can test collision/raycast against a block position in a level.
 *
 * <p>Implementations should return an Optional Hit result if the supplied ray (origin + direction)
 * intersects collidable content at the provided block position; otherwise Optional.empty().
 */
public interface ICollisionProvider {
    /**
     * Attempt to raycast/hit the block at {@code pos} using the given ray.
     *
     * @param level the level in which to perform the test
     * @param pos block position to test
     * @param rayOrigin world-space origin of the ray
     * @param rayDirection normalized world-space direction of the ray
     * @return Optional containing a RaycastHitResult when a hit occurred, otherwise empty
     */
    Optional<RaycastHitResult> tryHit(Level level, BlockPos pos, Vector3f rayOrigin, Vector3f rayDirection);
}

