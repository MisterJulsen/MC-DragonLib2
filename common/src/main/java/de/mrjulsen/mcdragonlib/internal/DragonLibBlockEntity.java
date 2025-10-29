package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DragonLibBlockEntity extends DLSyncedBlockEntity {

    protected DragonLibBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public DragonLibBlockEntity(BlockPos pos, BlockState state) {
        super(DragonLib.DRAGONLIB_BLOCK_ENTITY.get(), pos, state);
    }
}
