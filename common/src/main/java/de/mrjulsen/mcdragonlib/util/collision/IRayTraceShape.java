package de.mrjulsen.mcdragonlib.util.collision;

import java.util.Optional;

import org.joml.Vector3f;

public interface IRayTraceShape {
    
    /**
    * Checks whether the ray hits this shape.
    * @param rayOrigin Starting point of the ray
    * @param rayDirection Normalized direction vector
    * @return Optionally with HitPosition (in world coordinates) if hit
    */
    Optional<Vector3f> intersects(Vector3f rayOrigin, Vector3f rayDirection);
}

