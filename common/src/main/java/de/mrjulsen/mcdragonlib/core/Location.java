package de.mrjulsen.mcdragonlib.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Location {
    private static final String NBT_X = "x";
    private static final String NBT_Y = "y";
    private static final String NBT_Z = "z";
    private static final String NBT_DIM = "dim";

    public double x;
    public double y;
    public double z;
    public String dimension;
    private BlockPos blockPos;

    private Location() {}
    
    public Location(int x, int y, int z, String dimension) {
        this((double)x, (double)y, (double)z, dimension);
    }

    public Location(double x, double y, double z, String dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.generateBlockPos();
    }

    public Location(BlockPos pos, Level level) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.dimension = level.dimension().location().toString();
        this.blockPos = pos;
    }

    public Location(Vec3 vec, Level level) {
        this(vec.x, vec.y, vec.z, level.dimension().location().toString());
    }
    

    public BlockPos getLocationBlockPos() {
        return blockPos;
    }

    public Vec3 getLocationVec3() {
        return new Vec3(x, y, z);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(NBT_X, x);
        tag.putDouble(NBT_Y, y);
        tag.putDouble(NBT_Z, z);
        tag.putString(NBT_DIM, dimension);

        return tag;
    }

    private void generateBlockPos() {
        this.blockPos = new BlockPos(this.x, this.y, this.z);
    }

    public static Location fromNbt(CompoundTag tag) {
        return fromNbtInternal(tag, false);
    }

    /**
     * This method will read all values as int.
     * @deprecated Use {@code fromNbt} instead.
     */
    @Deprecated
    public static Location fromNbtAsInt(CompoundTag tag) {
        return fromNbtInternal(tag, true);
    }

    private static Location fromNbtInternal(CompoundTag tag, boolean legacy) {
        if (!tag.contains(NBT_X) || !tag.contains(NBT_Y) || !tag.contains(NBT_Z) || !tag.contains(NBT_DIM)) {
            return null;
        }

        Location loc = new Location();
        loc.loadFromNbt(tag, legacy);
        return loc;
    }

    public void loadFromNbt(CompoundTag tag, boolean legacy) {
        if (legacy) {
            this.x = tag.getInt(NBT_X);
            this.y = tag.getInt(NBT_Y);
            this.z = tag.getInt(NBT_Z);
        } else {
            this.x = tag.getDouble(NBT_X);
            this.y = tag.getDouble(NBT_Y);
            this.z = tag.getDouble(NBT_Z);
        }
        this.dimension = tag.getString(NBT_DIM);
        this.generateBlockPos();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Location other) {
            return x == other.x && y == other.y && z == other.z && dimension.equals(other.dimension);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("x=%s, y=%s, z=%s, dim=%s", x, y, z, dimension);
    }
}
