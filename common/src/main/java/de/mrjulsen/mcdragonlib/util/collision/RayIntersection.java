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

import net.minecraft.core.Direction;
import org.joml.Vector3dc;

/**
 * Result of a ray/shape intersection test.
 *
 * @param location world-space point where the ray enters the shape
 * @param distance distance from the ray origin to {@code location}
 * @param face the face (or nearest axis direction of the surface normal) that was hit
 * @param inside {@code true} if the ray origin was already inside the shape. In that case {@code location} is the origin and {@code distance} is 0.
 */
public record RayIntersection(Vector3dc location, double distance, Direction face, boolean inside) {
}
