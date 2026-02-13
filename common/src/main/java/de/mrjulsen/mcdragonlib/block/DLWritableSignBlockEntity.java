package de.mrjulsen.mcdragonlib.block;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.client.gui.builtin.WritableSignScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DLWritableSignBlockEntity extends DLSyncedBlockEntity {
    private String[] lines = null;

    protected DLWritableSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
        this.notifyUpdate();
    }

    public void setTexts(String[] messages) {
        initTextArray();
        this.lines = messages;
        this.notifyUpdate();
    }

    public String getText(int line) {
        initTextArray();        
        return this.lines == null ? null : this.lines[line];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lines = new String[this.getRenderConfig().lineData().length];
        for (int i = 0; i < this.getRenderConfig().lineData().length; i++) {
            this.lines[i] = tag.getString("line" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.lines != null) {
            for (int i = 0; i < this.getRenderConfig().lineData().length; i++) {
                tag.putString("line" + i, this.lines[i]);
            }
        }
    }
}
