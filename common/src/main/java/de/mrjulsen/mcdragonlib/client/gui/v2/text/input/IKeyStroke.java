package de.mrjulsen.mcdragonlib.client.gui.v2.text.input;

@FunctionalInterface
public interface IKeyStroke {
    boolean matches(KeyEvent event);
}
