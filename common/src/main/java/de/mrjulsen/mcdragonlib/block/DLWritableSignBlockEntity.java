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

package de.mrjulsen.mcdragonlib.block;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.client.gui.builtin.WritableSignScreen;
import net.minecraft.core.BlockPos;
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
