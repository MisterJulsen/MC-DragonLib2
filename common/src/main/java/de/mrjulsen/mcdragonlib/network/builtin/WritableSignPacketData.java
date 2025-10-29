package de.mrjulsen.mcdragonlib.network.builtin;

import de.mrjulsen.mcdragonlib.block.DLWritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public class WritableSignPacketData extends NetworkPacketData {

    private static final String NBT_POS = "Pos";
    private static final String NBT_MESSAGES = "Messages";

    private String[] messages;
    private BlockPos pos;

    public WritableSignPacketData(DLStatus status) {
        super(status);
    }

    public WritableSignPacketData(BlockPos pos, String[] messages) {
        super(DLStatus.OK);
        this.pos = pos;
        this.messages = messages;
    }
    

    @Override
    protected void write(CompoundTag nbt) {
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
        ListTag msgs = new ListTag();
        for (String msg : messages) {
            msgs.add(StringTag.valueOf(msg));
        }
        nbt.put(NBT_MESSAGES, msgs);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
        this.messages = nbt.getList(NBT_MESSAGES, Tag.TAG_STRING).stream().map(x -> ((StringTag)x).getAsString()).toArray(String[]::new);
    }

    public static void handler(WritableSignPacketData packet, NetworkPacketContext context) {
        context.queue(() -> {
            ServerPlayer sender = (ServerPlayer)context.getPlayer();
            if (sender.level().getBlockEntity(packet.pos) instanceof DLWritableSignBlockEntity blockEntity) {
                blockEntity.setTexts(packet.messages);
            }
        });
    }
}

