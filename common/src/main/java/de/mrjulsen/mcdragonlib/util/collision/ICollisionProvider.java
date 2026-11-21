/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.util.collision;

import java.util.Optional;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Abstraction that can test collision/raycast against a block position in a level.
 *
 * <p>Implementations should return an Optional Hit result if the supplied ray (origin + direction)
 * intersects collidable content at the provided block position; otherwise Optional.empty().
 *
 * @deprecated Use {@link IRayCollisionProvider} instead, which uses double precision and a maximum distance.
 *             Existing implementations keep working: they are bridged to the new interface automatically.
 */
@Deprecated
@FunctionalInterface
public interface ICollisionProvider extends IRayCollisionProvider {
    /**
     * Attempt to raycast/hit the block at {@code pos} using the given ray.
     *
     * @param level the level in which to perform the test
     * @param pos block position to test
     * @param rayOrigin world-space origin of the ray
     * @param rayDirection normalized world-space direction of the ray
     * @return Optional containing a RaycastHitResult when a hit occurred, otherwise empty
     * @deprecated Use {@link #tryHit(Level, BlockPos, Vector3dc, Vector3dc, double)}.
     */
    @Deprecated
    Optional<RaycastHitResult> tryHit(Level level, BlockPos pos, Vector3f rayOrigin, Vector3f rayDirection);

    /**
     * Default bridge for legacy implementations. Calls {@link #tryHit(Level, BlockPos, Vector3f, Vector3f)}
     * and filters the result by {@code maxDistance}.
     */
    @Override
    default Optional<RaycastHitResult> tryHit(Level level, BlockPos pos, Vector3dc rayOrigin, Vector3dc rayDirection, double maxDistance) {
        Vector3f originVec = new Vector3f((float)rayOrigin.x(), (float)rayOrigin.y(), (float)rayOrigin.z());
        Vector3f directionVec = new Vector3f((float)rayDirection.x(), (float)rayDirection.y(), (float)rayDirection.z()).normalize();
        return tryHit(level, pos, originVec, directionVec)
            .filter(hit -> hit.getPreciseDistance() >= 0 && hit.getPreciseDistance() <= maxDistance);
    }
}
