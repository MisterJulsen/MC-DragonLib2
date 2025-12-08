package de.mrjulsen.mcdragonlib.data;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WorldLocation {
    private static final String NBT_X = "x";
    private static final String NBT_Y = "y";
    private static final String NBT_Z = "z";
    private static final String NBT_DIM = "dimension";

    public final double x;
    public final double y;
    public final double z;
    public final ResourceLocation dimension;
    

    public WorldLocation(double x, double y, double z, ResourceLocation dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public WorldLocation(int x, int y, int z, Level level) {
        this((double)x, (double)y, (double)z, level);
    }

    public WorldLocation(double x, double y, double z, Level level) {
        this(x, y, z, level.dimension().location());
    }

    public WorldLocation(Vec3i vec, Level level) {
        this(vec.getX(), vec.getY(), vec.getZ(), level);
    }

    public WorldLocation(Vec3 vec, Level level) {
        this(vec.x, vec.y, vec.z, level);
    }

    public WorldLocation(Vector3fc vec, Level level) {
        this(vec.x(), vec.y(), vec.z(), level);
    }

    public WorldLocation(Vector3dc vec, Level level) {
        this(vec.x(), vec.y(), vec.z(), level);
    }
    

    public BlockPos getLocationBlockPos() {
        return new BlockPos(getLocationVec3i());
    }

    public Vec3i getLocationVec3i() {
        return new Vec3i((int)x, (int)y, (int)z);
    }

    public Vec3 getLocationVec3() {
        return new Vec3(x, y, z);
    }

    public Vector3f getLocationVector3f() {
        return new Vector3f((float)x, (float)y, (float)z);
    }

    public Vector3i getLocationVector3i() {
        return new Vector3i((int)x, (int)y, (int)z);
    }

    public Vector3d getLocationVector3d() {
        return new Vector3d(x, y, z);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(NBT_X, x);
        tag.putDouble(NBT_Y, y);
        tag.putDouble(NBT_Z, z);
        tag.putString(NBT_DIM, dimension.toString());

        return tag;
    }

    public static WorldLocation loadFromNbt(CompoundTag tag) {
        return new WorldLocation(
            tag.getDouble(NBT_X),
            tag.getDouble(NBT_Y),
            tag.getDouble(NBT_Z),
            DLUtils.resourceLocation(tag.getString(NBT_DIM))
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorldLocation other) {
            return x == other.x && y == other.y && z == other.z && dimension.equals(other.dimension);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("x=%s, y=%s, z=%s, dim=%s", x, y, z, dimension);
    }
}
