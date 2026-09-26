package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

@FunctionalInterface
public interface IBlockLayout {

    BlockLayoutResult layout(BlockLayoutContext context);
}
