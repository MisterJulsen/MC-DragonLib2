package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ITextFormatter<T extends DLGuiComponent> {
    Component combine(T src);
}
