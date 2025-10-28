package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.action;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface TooltipAction {
    Component getTooltip();
}
