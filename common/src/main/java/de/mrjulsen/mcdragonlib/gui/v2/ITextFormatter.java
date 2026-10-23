package de.mrjulsen.mcdragonlib.gui.v2;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ITextFormatter<T extends GuiComponent> {
    Component format(T component);
}
