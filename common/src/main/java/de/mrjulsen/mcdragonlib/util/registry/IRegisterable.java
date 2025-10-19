package de.mrjulsen.mcdragonlib.util.registry;

import de.mrjulsen.mcdragonlib.data.INBTSerializable;

public interface IRegisterable<T extends INBTSerializable> extends INBTSerializable {
    DLRegistryObject<T> getRegistryType();
}
