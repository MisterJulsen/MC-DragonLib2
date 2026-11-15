package de.mrjulsen.mcdragonlib.util.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import net.minecraft.core.Direction;

import java.util.Optional;

/**
 * Ray-trace shape representing a capsule: the set of all points within {@code radius} of the
 * segment [pointA, pointB] (a cylinder with hemispherical end caps).
 *
 * <p>If both points are equal, the shape is a sphere.
 */
@SuppressWarnings("deprecation")
public class LineRayShape implements IRayTraceShape {
    private static final double EPSILON = 1.0E-9;

    private final Vector3dc pointA;
    private final Vector3dc pointB;
    private final double radius;

    /**
     * Create a capsule-shaped line between two points with given radius.
     *
     * @param a segment start position
     * @param b segment end position
     * @param radius capsule radius (>= 0)
     */
    public LineRayShape(Vector3dc a, Vector3dc b, double radius) {
        if (radius < 0 || !Double.isFinite(radius)) {
            throw new IllegalArgumentException("radius must be a finite value >= 0, but was " + radius);
        }
        this.pointA = a;
        this.pointB = b;
        this.radius = radius;
    }

    /**
     * Create a capsule-shaped line between two points with given radius.
     *
     * @param a segment start position (copied)
     * @param b segment end position (copied)
     * @param radius capsule radius (>= 0)
     */
    public LineRayShape(Vector3f a, Vector3f b, float radius) {
        this(new Vector3d(a), new Vector3d(b), radius);
    }

    public Vector3dc getPointA() {
        return pointA;
    }

    public Vector3dc getPointB() {
        return pointB;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Optional<RayIntersection> intersect(Vector3dc rayOrigin, Vector3dc rayDirection, double maxDistance) {
        Vector3d rd = new Vector3d(rayDirection).normalize();
        if (rd.lengthSquared() < EPSILON) {
            return Optional.empty();
        }

        double r2 = radius * radius;
        Vector3dc closestToOrigin = closestPointOnSegment(rayOrigin);
        if (rayOrigin.distanceSquared(closestToOrigin) <= r2) {
            return Optional.of(new RayIntersection(rayOrigin, 0, RaycastUtils.faceFromDirection(rd), true));
        }

        double t = Double.POSITIVE_INFINITY;
        Vector3dc ba = new Vector3d(pointB).sub(pointA);
        Vector3dc oa = new Vector3d(rayOrigin).sub(pointA);
        double baba = ba.dot(ba);

        if (baba > EPSILON) {
            double bard = ba.dot(rd);
            double baoa = ba.dot(oa);
            double rdoa = rd.dot(oa);
            double oaoa = oa.dot(oa);

            double a = baba - bard * bard;
            if (a > EPSILON * baba) {
                double b = baba * rdoa - baoa * bard;
                double c = baba * oaoa - baoa * baoa - r2 * baba;
                double h = b * b - a * c;
                if (h >= 0) {
                    double tc = (-b - Math.sqrt(h)) / a;
                    double y = baoa + tc * bard;
                    if (tc >= 0 && y >= 0 && y <= baba) {
                        t = tc;
                    }
                }
            }
            t = Math.min(t, intersectSphere(rayOrigin, rd, pointB, r2));
        }
        t = Math.min(t, intersectSphere(rayOrigin, rd, pointA, r2));

        if (t == Double.POSITIVE_INFINITY || t > maxDistance) {
            return Optional.empty();
        }

        Vector3dc hit = new Vector3d(rayOrigin).add(new Vector3d(rd).mul(t));
        Vector3dc normal = new Vector3d(hit).sub(closestPointOnSegment(hit));
        Direction face = normal.lengthSquared() > EPSILON ? Direction.getNearest(normal.x(), normal.y(), normal.z()) : RaycastUtils.faceFromDirection(rd);
        return Optional.of(new RayIntersection(hit, t, face, false));
    }

    /**
     * Compute the first intersection point between the provided ray and this capsule.
     *
     * @param rayOrigin ray origin
     * @param rayDirection normalized ray direction
     * @return hit position if ray intersects this capsule
     * @deprecated Use {@link #intersect(Vector3dc, Vector3dc, double)}.
     */
    @Override
    @Deprecated
    public Optional<Vector3f> intersects(Vector3f rayOrigin, Vector3f rayDirection) {
        return intersect(new Vector3d(rayOrigin), new Vector3d(rayDirection), Double.POSITIVE_INFINITY).map(hit -> new Vector3f((float)hit.location().x(), (float)hit.location().y(), (float)hit.location().z()));
    }

    private Vector3dc closestPointOnSegment(Vector3dc point) {
        Vector3d ba = new Vector3d(pointB).sub(pointA);
        double baba = ba.dot(ba);
        if (baba <= EPSILON) {
            return pointA;
        }
        double h = Math.max(0, Math.min(1, new Vector3d(point).sub(pointA).dot(ba) / baba));
        return new Vector3d(pointA).add(ba.mul(h));
    }

    private static double intersectSphere(Vector3dc origin, Vector3dc rd, Vector3dc center, double r2) {
        Vector3dc oc = new Vector3d(origin).sub(center);
        double b = oc.dot(rd);
        double c = oc.dot(oc) - r2;
        double h = b * b - c;
        if (h < 0) {
            return Double.POSITIVE_INFINITY;
        }
        double t = -b - Math.sqrt(h);
        return t >= 0 ? t : Double.POSITIVE_INFINITY;
    }
}
