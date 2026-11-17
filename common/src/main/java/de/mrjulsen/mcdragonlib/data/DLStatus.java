/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.data;

import net.minecraft.nbt.CompoundTag;

public record DLStatus(byte flag, int code, String message) {

    private static final String NBT_FLAG = "Flag";
    private static final String NBT_CODE = "Code";
    private static final String NBT_MESSAGE = "Message";

    public static final String EMPTY_MSG = "";

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
        nbt.putString(NBT_MESSAGE, message == null ? "" : message);
        return nbt;
    }

    public static DLStatus fromNbt(CompoundTag nbt) {
        return new DLStatus(
            nbt.contains(NBT_FLAG) ? nbt.getByte(NBT_FLAG) : 0x0,
            nbt.contains(NBT_CODE) ? nbt.getInt(NBT_CODE) : CODE_UNKNOWN,
            nbt.contains(NBT_MESSAGE) ? nbt.getString(NBT_MESSAGE) : EMPTY_MSG);
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