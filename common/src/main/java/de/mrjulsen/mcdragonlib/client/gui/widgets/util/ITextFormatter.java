package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ITextFormatter<T extends DLGuiComponent> {
    Component combine(T src);
}
