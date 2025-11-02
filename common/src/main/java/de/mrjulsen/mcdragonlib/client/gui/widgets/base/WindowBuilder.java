package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

@FunctionalInterface
public interface WindowBuilder<T extends DLWindow> {
    T build(DLWindowManager manager);
}
