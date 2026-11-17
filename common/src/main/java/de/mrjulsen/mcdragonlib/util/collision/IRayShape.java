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
import org.joml.Vector3dc;

/**
 * A geometric shape that can be ray-traced against.
 *
 * <p>Implementations compute the first point where a ray enters the shape. All calculations use world coordinates and double precision.
 */
@FunctionalInterface
public interface IRayShape {

    /**
     * Compute the first intersection of the ray with this shape.
     *
     * @param rayOrigin world-space origin of the ray
     * @param rayDirection world-space direction of the ray; does not need to be normalized, but must not be zero
     * @param maxDistance maximum distance from the origin at which hits are accepted; may be {@link Double#POSITIVE_INFINITY}
     * @return the intersection, or empty if the ray misses this shape within {@code maxDistance}
     */
    Optional<RayIntersection> intersect(Vector3dc rayOrigin, Vector3dc rayDirection, double maxDistance);
}
