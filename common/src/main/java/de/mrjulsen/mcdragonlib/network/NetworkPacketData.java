package de.mrjulsen.mcdragonlib.network;

import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Function;

public abstract class NetworkPacketData implements INBTSerializable {

    static final Function<DLStatus, NetworkPacketData> DEFAULT_INSTANCE = (status) -> new NetworkPacketData(status) {
        @Override
        protected void write(CompoundTag nbt) {}

        @Override
        protected void read(CompoundTag nbt) {}
    };
    
    private static final String NBT_STATUS = "Status";
    private static final String NBT_DATA = "Data";

    private DLStatus status = DLStatus.EMPTY;

    public NetworkPacketData(DLStatus status) {
        this.status = status;
    }

    public final DLStatus getStatus() {
        return status;
    }
    
    final void setStatus(DLStatus status) {
        this.status = status;
    }

    @Override
    public final CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.put(NBT_STATUS, status.toNbt());
        CompoundTag data = new CompoundTag();
        write(data);
        nbt.put(NBT_DATA, data);
        return nbt;
    }

    @Override
    public final void deserializeNbt(CompoundTag nbt) {
        this.status = DLStatus.fromNbt(nbt.getCompound(NBT_STATUS));
        read(nbt.getCompound(NBT_DATA));
    }


    protected abstract void write(CompoundTag nbt);
    protected abstract void read(CompoundTag nbt);



    static final class Empty extends NetworkPacketData {
        public Empty(DLStatus result) {
            super(DLStatus.OK);
        }

        @Override
        protected void write(CompoundTag nbt) {
        }

        @Override
        protected void read(CompoundTag nbt) {
        }        
    }
}
