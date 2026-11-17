/*
 * DragonLib 3 - 1.20.1
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
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;

/**
 * Tests a ray against the collidable content of a single block position.
 *
 * <p>Used by {@link RaycastUtils#rayTrace(Vector3dc, Vector3dc, Level, double, IRayCollisionProvider)}.
 * The hit should lie within the block's cell, extended by the trace radius. Hits outside that area
 * can be found late or not at all.
 */
@FunctionalInterface
public interface IRayCollisionProvider {

    /**
     * Test the ray against the content at {@code pos}.
     *
     * @param level the level in which to perform the test
     * @param pos block position to test
     * @param rayOrigin world-space origin of the ray
     * @param rayDirection normalized world-space direction of the ray
     * @param maxDistance maximum accepted distance from the origin
     * @return the hit, or empty if nothing was hit within {@code maxDistance}
     */
    Optional<RaycastHitResult> tryHit(Level level, BlockPos pos, Vector3dc rayOrigin, Vector3dc rayDirection, double maxDistance);

    /**
     * Create a provider from a function that returns the shape at a block position.
     *
     * @param shapeGetter returns the shape for the position, or {@code null} if there is nothing to hit
     */
    static IRayCollisionProvider ofShapes(BiFunction<Level, BlockPos, IRayShape> shapeGetter) {
        return (level, pos, origin, direction, maxDistance) -> {
            IRayShape shape = shapeGetter.apply(level, pos);
            if (shape == null) {
                return Optional.empty();
            }
            return shape.intersect(origin, direction, maxDistance).map(hit -> new RaycastHitResult(hit, pos, shape));
        };
    }
}
