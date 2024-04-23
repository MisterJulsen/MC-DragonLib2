package de.mrjulsen.mcdragonlib.client.ber;

import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractBlockEntityRenderInstance<T extends BlockEntity> implements IBlockEntityRendererInstance<T> {

    public AbstractBlockEntityRenderInstance(T blockEntity) {
        preinit(blockEntity);
        update(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, EUpdateReason.INITIALIZED);
    }

    protected void preinit(T blockEntity) {}

}