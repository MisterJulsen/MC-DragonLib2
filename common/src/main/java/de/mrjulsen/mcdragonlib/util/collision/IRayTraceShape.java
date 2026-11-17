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

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import net.minecraft.world.phys.Vec3;

/**
 * Represents a geometric shape able to be ray-traced against.
 *
 * <p>The shape implementation is responsible for computing the first intersection
 * point (if any) in world coordinates relative to a provided ray.
 *
 * @deprecated Use {@link IRayShape} instead, which works with double precision, supports a maximum
 *             distance and returns more information about the hit. Existing implementations keep working:
 *             the new method is bridged to {@link #intersects(Vector3f, Vector3f)} by default.
 */
@Deprecated
@FunctionalInterface
public interface IRayTraceShape extends IRayShape {

    /**
     * Checks whether the ray hits this shape and returns the hit location in world coordinates.
     *
     * @param rayOrigin starting point of the ray in world coordinates
     * @param rayDirection normalized direction vector of the ray
     * @return Optional containing the hit location (Vector3f) if the ray intersects; otherwise empty
     * @deprecated Use {@link #intersect(Vector3dc, Vector3dc, double)}.
     */
    @Deprecated
    Optional<Vector3f> intersects(Vector3f rayOrigin, Vector3f rayDirection);

    /**
     * Default bridge for legacy implementations. Calls {@link #intersects(Vector3f, Vector3f)} and
     * filters the result by {@code maxDistance}. The face is estimated from the ray direction.
     */
    @Override
    default Optional<RayIntersection> intersect(Vector3dc rayOrigin, Vector3dc rayDirection, double maxDistance) {
        Vector3f originVec = new Vector3f((float)rayOrigin.x(), (float)rayOrigin.y(), (float)rayOrigin.z());
        Vector3f directionVec = new Vector3f((float)rayDirection.x(), (float)rayDirection.y(), (float)rayDirection.z()).normalize();

        return intersects(originVec, directionVec).flatMap(hit -> {
            double distance = hit.distance(originVec);
            if (distance > maxDistance) {
                return Optional.empty();
            }
            return Optional.of(new RayIntersection(new Vector3d(hit.x(), hit.y(), hit.z()), distance, RaycastUtils.faceFromDirection(new Vector3d(directionVec.x(), directionVec.y(), directionVec.z())), false));
        });
    }
}
