package de.mrjulsen.mcdragonlib.gui.v2.window;

import java.util.UUID;

/**
 * Handle of a modal layer, handed out by WindowManager#openModal and needed to close it again or to
 * open further windows on the same layer.
 */
public record ModalId(UUID value) {

    public static ModalId random() {
        return new ModalId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
