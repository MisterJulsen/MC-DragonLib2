package de.mrjulsen.mcdragonlib.client.gui.v2;

@FunctionalInterface
public interface IFormatter<T extends GuiComponent, S> {
    S format(T component);
}
