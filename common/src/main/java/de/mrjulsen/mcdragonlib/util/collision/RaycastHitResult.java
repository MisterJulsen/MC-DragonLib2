package de.mrjulsen.mcdragonlib.util.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Concrete hit-result used by the raycast utilities.
 *
 * <p>Contains the hit location (Vec3), the BlockPos that was tested, the distance from the ray origin
 * and optional hit-specific data to be interpreted by the caller.
 */
public class RaycastHitResult extends HitResult implements Comparable<RaycastHitResult> {
    private final BlockPos blockPos;
    private final float distance;
    private final Object hitData;

    public RaycastHitResult(Vec3 location, BlockPos blockPos, float distance, Object hitData) {
        super(location);
        this.blockPos = blockPos;
        this.distance = distance;
        this.hitData = hitData;
    }

    /**
     * Return the BlockPos where the hit was reported.
     *
     * @return block position
     */
    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * Distance from the ray origin to the hit point.
     *
     * @return distance as float
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Arbitrary hit-specific data (may be null). The concrete type is defined by the provider.
     *
     * @return hit data object
     */
    public Object getHitData() {
        return hitData;
    }

    /**
     * Compare by distance to allow sorting of multiple hits.
     */
    @Override
    public int compareTo(RaycastHitResult other) {
        return Float.compare(getDistance(), other.getDistance());
    }

    /**
     * Report the broad HitResult type. This implementation always returns Type.BLOCK.
     */
    @Override
    public Type getType() {
        return Type.BLOCK;
    }
}
