package de.mrjulsen.mcdragonlib.util.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

/**
 * Adapter that uses a Minecraft VoxelShape (relative to {@code pos}) to perform precise ray-voxel intersection tests.
 */
@SuppressWarnings("deprecation")
public class VoxelShapeRayShape implements IRayTraceShape {
    private final VoxelShape shape;
    private final BlockPos pos;

    public VoxelShapeRayShape(VoxelShape shape, BlockPos pos) {
        this.shape = shape;
        this.pos = pos.immutable();
    }

    public VoxelShape getShape() {
        return shape;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public Optional<RayIntersection> intersect(Vector3dc rayOrigin, Vector3dc rayDirection, double maxDistance) {
        if (shape.isEmpty()) {
            return Optional.empty();
        }
        Vector3dc dir = new Vector3d(rayDirection).normalize();
        if (dir.lengthSquared() < 1.0E-9) {
            return Optional.empty();
        }

        AABB bounds = shape.bounds().move(pos);
        double dx = Math.max(Math.abs(rayOrigin.x() - bounds.minX), Math.abs(rayOrigin.x() - bounds.maxX));
        double dy = Math.max(Math.abs(rayOrigin.y() - bounds.minY), Math.abs(rayOrigin.y() - bounds.maxY));
        double dz = Math.max(Math.abs(rayOrigin.z() - bounds.minZ), Math.abs(rayOrigin.z() - bounds.maxZ));
        double reach = Math.min(maxDistance, Math.sqrt(dx * dx + dy * dy + dz * dz) + 1);

        Vector3dc endVec = new Vector3d(rayOrigin).add(new Vector3d(dir).mul(reach));
        BlockHitResult result = shape.clip(new Vec3(rayOrigin.x(), rayOrigin.y(), rayOrigin.z()), new Vec3(endVec.x(), endVec.y(), endVec.z()), pos);
        if (result == null) {
            return Optional.empty();
        }
        Vector3dc resultVec = new Vector3d(result.getLocation().x(), result.getLocation().y(), result.getLocation().z());
        double distance = resultVec.distance(rayOrigin);
        if (distance > maxDistance) {
            return Optional.empty();
        }
        return Optional.of(new RayIntersection(resultVec, distance, result.getDirection(), result.isInside()));
    }

    /**
     * @deprecated Use {@link #intersect(Vector3dc, Vector3dc, double)}. Unlike before, this method is no longer limited to 100 blocks.
     */
    @Override
    @Deprecated
    public Optional<Vector3f> intersects(Vector3f rayOrigin, Vector3f rayDirection) {
        return intersect(new Vector3d(rayOrigin), new Vector3d(rayDirection), Double.POSITIVE_INFINITY).map(hit -> new Vector3f((float)hit.location().x(), (float)hit.location().y(), (float)hit.location().z()));
    }
}
