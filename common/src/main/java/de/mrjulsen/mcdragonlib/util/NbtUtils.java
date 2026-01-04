package de.mrjulsen.mcdragonlib.util;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * Utility methods for writing and reading common geometric types to/from CompoundTag.
 *
 * <p>Keys used: "X", "Y", "Z" for component storage.
 */
public final class NbtUtils {
    private NbtUtils() {}

    public static final String NBT_X = "X";
    public static final String NBT_Y = "Y";
    public static final String NBT_Z = "Z";

    private static void putIntPos(CompoundTag compound, String name, int x, int y, int z) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_X, x);
        nbt.putInt(NBT_Y, y);
        nbt.putInt(NBT_Z, z);
        compound.put(name, nbt);
    }

    private static void putDoublePos(CompoundTag compound, String name, double x, double y, double z) {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble(NBT_X, x);
        nbt.putDouble(NBT_Y, y);
        nbt.putDouble(NBT_Z, z);
        compound.put(name, nbt);
    }
    
    /**
     * Store a Vec3i-style position into a compound under {@code name}.
     */
    public static void putNbtPos(CompoundTag compound, String name, Vec3i pos) {
        putIntPos(compound, name, pos.getX(), pos.getY(), pos.getZ());
    }
    
    /**
     * Store a JOML Vector3ic (integer) into NBT.
     */
    public static void putNbtVector3i(CompoundTag compound, String name, Vector3ic pos) {
        putIntPos(compound, name, pos.x(), pos.y(), pos.z());
    }
    
    /**
     * Store a Vec3 (double) into NBT.
     */
    public static void putNbtVec(CompoundTag compound, String name, Vec3 pos) {
        putDoublePos(compound, name, pos.x(), pos.y(), pos.z());
    }
    
    /**
     * Store a JOML Vector3fc (float) into NBT (stored as doubles).
     */
    public static void putNbtVector3f(CompoundTag compound, String name, Vector3fc pos) {
        putDoublePos(compound, name, pos.x(), pos.y(), pos.z());
    }
    
    /**
     * Store a JOML Vector3dc (double) into NBT.
     */
    public static void putNbtVector3d(CompoundTag compound, String name, Vector3dc pos) {
        putDoublePos(compound, name, pos.x(), pos.y(), pos.z());
    }

    /**
     * Read a BlockPos previously stored under {@code name}.
     */
    public static BlockPos getNbtBlockPos(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new BlockPos(nbt.getInt(NBT_X), nbt.getInt(NBT_Y), nbt.getInt(NBT_Z));
    }

    /**
     * Read a SectionPos previously stored under {@code name}.
     */
    public static SectionPos getNbtSectionPos(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return SectionPos.of(nbt.getInt(NBT_X), nbt.getInt(NBT_Y), nbt.getInt(NBT_Z));
    }

    /**
     * Read a Vec3i from NBT.
     */
    public static Vec3i getNbtVec3i(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new Vec3i(nbt.getInt(NBT_X), nbt.getInt(NBT_Y), nbt.getInt(NBT_Z));
    }

    /**
     * Read a JOML Vector3i from NBT.
     */
    public static Vector3i getNbtVector3i(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new Vector3i(nbt.getInt(NBT_X), nbt.getInt(NBT_Y), nbt.getInt(NBT_Z));
    }

    /**
     * Read a Vec3 (double) from NBT.
     */
    public static Vec3 getNbtVec3(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new Vec3(nbt.getDouble(NBT_X), nbt.getDouble(NBT_Y), nbt.getDouble(NBT_Z));
    }

    /**
     * Read a JOML Vector3f from NBT.
     */
    public static Vector3f getNbtVector3f(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new Vector3f((float)nbt.getDouble(NBT_X), (float)nbt.getDouble(NBT_Y), (float)nbt.getDouble(NBT_Z));
    }

    /**
     * Read a JOML Vector3d from NBT.
     */
    public static Vector3d getNbtVector3d(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new Vector3d(nbt.getDouble(NBT_X), nbt.getDouble(NBT_Y), nbt.getDouble(NBT_Z));
    }

    /**
     * Read a ChunkPos stored under {@code name} (stores x and z).
     */
    public static ChunkPos getNbtChunkPos(CompoundTag compound, String name) {
        CompoundTag nbt = compound.getCompound(name);
        return new ChunkPos(nbt.getInt(NBT_X), nbt.getInt(NBT_Z));
    }
    
    /**
     * Store a ChunkPos under {@code name} (stores x and z).
     */
    public static void putNbtChunkPos(CompoundTag compound, String name, ChunkPos pos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_X, pos.x);
        nbt.putInt(NBT_Z, pos.z);
        compound.put(name, nbt);
    }
  }
