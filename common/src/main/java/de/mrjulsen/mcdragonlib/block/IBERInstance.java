package de.mrjulsen.mcdragonlib.block;

import de.mrjulsen.mcdragonlib.client.ber.IBlockEntityRendererInstance;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IBERInstance<T extends BlockEntity> {
    IBlockEntityRendererInstance<T> getRenderer();
}
