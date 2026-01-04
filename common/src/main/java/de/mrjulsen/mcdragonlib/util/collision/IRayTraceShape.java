package de.mrjulsen.mcdragonlib.util.collision;

import java.util.Optional;

import org.joml.Vector3f;

/**
 * Represents a geometric shape able to be ray-traced against.
 *
 * <p>The shape implementation is responsible for computing the first intersection
 * point (if any) in world coordinates relative to a provided ray.
 */
public interface IRayTraceShape {
    
    /**
     * Checks whether the ray hits this shape and returns the hit location in world coordinates.
     *
     * @param rayOrigin starting point of the ray in world coordinates
     * @param rayDirection normalized direction vector of the ray
     * @return Optional containing the hit location (Vector3f) if the ray intersects; otherwise empty
     */
    Optional<Vector3f> intersects(Vector3f rayOrigin, Vector3f rayDirection);
}

