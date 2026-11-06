package de.mrjulsen.mcdragonlib.gui.v2.text.layout;

@FunctionalInterface
public interface IBlockLayout {

    BlockLayoutResult layout(BlockLayoutContext context);
}
