package de.mrjulsen.mcdragonlib.util.collision;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Hit result used by the raycast utilities.
 *
 * <p>Extends {@link BlockHitResult}, so it can safely be passed to code that casts block-type hit results.
 * In addition to the vanilla data it contains the distance from the ray origin and optional
 * hit-specific data defined by the collision provider.
 *
 * <p>Note: {@link #compareTo(RaycastHitResult)} compares by distance only and is therefore not consistent with {@code equals}.
 */
public class RaycastHitResult extends BlockHitResult implements Comparable<RaycastHitResult> {
    private final double distance;
    private final Object hitData;

    /**
     * @param location world-space hit location
     * @param face the face that was hit
     * @param blockPos the block position the hit belongs to
     * @param inside whether the ray started inside the hit shape
     * @param distance distance from the ray origin to {@code location}
     * @param hitData optional provider-specific data
     */
    public RaycastHitResult(Vec3 location, Direction face, BlockPos blockPos, boolean inside, double distance, @Nullable Object hitData) {
        super(location, face, blockPos.immutable(), inside);
        this.distance = distance;
        this.hitData = hitData;
    }

    /**
     * Create a hit result from a shape intersection.
     */
    public RaycastHitResult(RayIntersection intersection, BlockPos blockPos, @Nullable Object hitData) {
        this(new Vec3(intersection.location().x(), intersection.location().y(), intersection.location().z()), intersection.face(), blockPos, intersection.inside(), intersection.distance(), hitData);
    }

    /**
     * @deprecated Use {@link #RaycastHitResult(Vec3, Direction, BlockPos, boolean, double, Object)}.
     *             This constructor has no face information, so it is estimated from the location relative to the block center.
     */
    @Deprecated
    public RaycastHitResult(Vec3 location, BlockPos blockPos, float distance, Object hitData) {
        this(location, estimateFace(location, blockPos), blockPos, false, distance, hitData);
    }

    private static Direction estimateFace(Vec3 location, BlockPos pos) {
        Vec3 rel = location.subtract(Vec3.atCenterOf(pos));
        return Direction.getNearest(rel.x, rel.y, rel.z);
    }

    /**
     * Distance from the ray origin to the hit point.
     *
     * @return distance as float
     * @see #getPreciseDistance()
     */
    public float getDistance() {
        return (float) distance;
    }

    /**
     * Distance from the ray origin to the hit point with double precision.
     */
    public double getPreciseDistance() {
        return distance;
    }

    /**
     * Arbitrary hit-specific data (may be null). The concrete type is defined by the provider.
     *
     * @return hit data object
     */
    @Nullable
    public Object getHitData() {
        return hitData;
    }

    /**
     * Returns the hit data if it is an instance of the given type.
     */
    public <T> Optional<T> getHitData(Class<T> type) {
        return type.isInstance(hitData) ? Optional.of(type.cast(hitData)) : Optional.empty();
    }

    @Override
    public RaycastHitResult withDirection(Direction direction) {
        return new RaycastHitResult(getLocation(), direction, getBlockPos(), isInside(), distance, hitData);
    }

    @Override
    public RaycastHitResult withPosition(BlockPos pos) {
        return new RaycastHitResult(getLocation(), getDirection(), pos, isInside(), distance, hitData);
    }

    /**
     * Compare by distance to allow sorting of multiple hits.
     */
    @Override
    public int compareTo(RaycastHitResult other) {
        return Double.compare(distance, other.distance);
    }

    /**
     * Report the broad HitResult type. This implementation always returns Type.BLOCK.
     */
    @Override
    public Type getType() {
        return Type.BLOCK;
    }
}
