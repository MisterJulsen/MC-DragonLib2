package de.mrjulsen.mcdragonlib.data;

import net.minecraft.nbt.CompoundTag;

public record DLStatus(byte flag, int code, String message) {

    private static final String NBT_FLAG = "Flag";
    private static final String NBT_CODE = "Code";
    private static final String NBT_MESSAGE = "Message";

    public static final byte CODE_UNKNOWN = -0x1;

    public static final byte FLAG_OK = 0x0;
    public static final byte FLAG_DONE = 0x1;
    public static final byte FLAG_CANCEL = 0x2;
    public static final byte FLAG_ERROR = Byte.MIN_VALUE;

    public static final DLStatus OK = new DLStatus(FLAG_OK, CODE_UNKNOWN, "");
    public static final DLStatus DONE = new DLStatus(FLAG_DONE, CODE_UNKNOWN, "");
    public static final DLStatus CANCEL = new DLStatus(FLAG_CANCEL, CODE_UNKNOWN, "");    
    public static final DLStatus EMPTY = new DLStatus(FLAG_ERROR, CODE_UNKNOWN, "Not initialized!");
    
    public static final DLStatus error(Throwable ex) {
        return error(ex, CODE_UNKNOWN);
    }

    public static final DLStatus error(Throwable ex, int code) {
        return new DLStatus(FLAG_ERROR, code, ex.getMessage());
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(NBT_FLAG, flag);
        nbt.putInt(NBT_CODE, code);
        nbt.putString(NBT_MESSAGE, message);
        return nbt;
    }

    public static DLStatus fromNbt(CompoundTag nbt) {
        return new DLStatus(nbt.getByte(NBT_FLAG), nbt.getInt(NBT_CODE), nbt.getString(NBT_MESSAGE));
    }

    public boolean isDone() {
        return flag == FLAG_DONE;
    }

    public boolean isCancel() {
        return flag == FLAG_CANCEL;
    }

    public boolean isError() {
        return flag == FLAG_ERROR;
    }
    
    public boolean isOK() {
        return flag == FLAG_OK;
    }

    public boolean noIssues() {
        return isOK() || isDone();
    }
}