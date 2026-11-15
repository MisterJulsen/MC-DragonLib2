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
