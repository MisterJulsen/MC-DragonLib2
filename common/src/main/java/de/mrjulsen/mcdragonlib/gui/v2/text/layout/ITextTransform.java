package de.mrjulsen.mcdragonlib.gui.v2.text.layout;

@FunctionalInterface
public interface ITextTransform {
    String apply(int line, String text);
}
