package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.util.UUID;

public class ModalId {
    private final UUID id;

    public ModalId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModalId o) {
            return id.equals(o.id);
        }
        if (obj instanceof UUID o) {
            return id.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
