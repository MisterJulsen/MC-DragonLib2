package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input;

@FunctionalInterface
public interface IKeyStroke {
    boolean matches(KeyEvent event);
}
