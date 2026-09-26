package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

@FunctionalInterface
public interface ITextTransform {
    String apply(int line, String text);
}
