package de.mrjulsen.mcdragonlib.registry.fabric;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DLClientRegistriesImpl {
    
    public static <T extends BlockEntity> void register(BlockEntityType<T> type, BlockEntityRendererProvider<? super T> provider) {
        throw new AssertionError();
    }
}
