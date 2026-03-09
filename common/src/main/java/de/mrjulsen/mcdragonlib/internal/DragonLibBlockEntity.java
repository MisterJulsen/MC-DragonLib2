package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import de.mrjulsen.mcdragonlib.block.DLWritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.client.gui.builtin.WritableSignScreen;
import de.mrjulsen.mcdragonlib.util.DLColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class DragonLibBlockEntity extends DLWritableSignBlockEntity {

    protected DragonLibBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public WritableSignScreen.WritableSignConfig getRenderConfig() {
        float y = 120;
        return new WritableSignScreen.WritableSignConfig(new WritableSignScreen.ConfiguredLineData[] {
                new WritableSignScreen.ConfiguredLineData(0, -1.0F / 16.0F * 4.25f, new Vec2(1, 1.5f), new Vec2(1.5f, 1.5f), 1.0F / 16.0F * 15, 1, 0)
        }, true, 1.0F / 16.0F * 6.5f, y, WritableSignScreen.WritableSignConfig.DEFAULT_SCALE, 90, 0.4f, 0.0f, 0.02f, (blockState) -> {
            return 90f;
        }, DLColor.pickBasedOnBrightness(DLColor.WHITE, DLColor.WHITE, DLColor.BLACK, 0.5f).getAsARGB());
    }

    public DragonLibBlockEntity(BlockPos pos, BlockState state) {
        super(DragonLib.DRAGONLIB_BLOCK_ENTITY.get(), pos, state);
    }
}
