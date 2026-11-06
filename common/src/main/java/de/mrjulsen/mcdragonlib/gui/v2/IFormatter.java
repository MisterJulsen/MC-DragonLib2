package de.mrjulsen.mcdragonlib.gui.v2;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface IFormatter<T extends GuiComponent, S> {
    S format(T component);
}
