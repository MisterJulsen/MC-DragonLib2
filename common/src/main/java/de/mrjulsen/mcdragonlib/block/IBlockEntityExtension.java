package de.mrjulsen.mcdragonlib.block;

public interface IBlockEntityExtension {
    default void dragonlib$onChunkUnloaded() {}
    default void dragonlib$onBlockEntityLoad() {}
}
