package de.mrjulsen.mcdragonlib.block;

public interface IBlockEntityExtension {
    default void onChunkUnloaded() {}
    default void onBlockEntityLoad() {}
}
