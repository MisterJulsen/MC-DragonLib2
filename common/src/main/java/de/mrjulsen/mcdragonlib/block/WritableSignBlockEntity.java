package de.mrjulsen.mcdragonlib.block;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class WritableSignBlockEntity extends SyncedBlockEntity {
    private String[] lines = null;

    protected WritableSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract WritableSignScreen.WritableSignConfig getRenderConfig();

    private void initTextArray() {
        if (this.lines == null) {
            this.lines = new String[this.getRenderConfig().lineData().length];
            Arrays.fill(lines, "");
        }
    }

    public void setText(String text, int line) {
        if (line < 0 || line > this.getRenderConfig().lineData().length)
            return;

        initTextArray();

        this.lines[line] = text;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public void setTexts(String[] messages) {
        initTextArray();
        this.lines = messages;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public String getText(int line) {
        initTextArray();        
        return this.lines == null ? null : this.lines[line];
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.lines = new String[this.getRenderConfig().lineData().length];
        for (int i = 0; i < this.getRenderConfig().lineData().length; i++) {
            this.lines[i] = compound.getString("line" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.lines != null) {
            for (int i = 0; i < this.getRenderConfig().lineData().length; i++) {
                tag.putString("line" + i, this.lines[i]);
            }
        }
    }
}
