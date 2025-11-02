package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ModalWindowStack extends ConcurrentLinkedDeque<DLWindow> {
    private final ModalId id;

    public ModalWindowStack(UUID id) {
        Objects.requireNonNull(id);
        this.id = new ModalId(id);
    }

    @Override
    public boolean add(DLWindow win) {
        win.assignToModal(id);
        boolean b = super.add(win);
        return b;
    }

    @Override
    public DLWindow remove() {
        DLWindow win = super.remove();
        win.assignToModal(null);
        return win;
    }

    @Override
    public void clear() {
        for (DLWindow win : this) {
            win.assignToModal(null);
        }
        super.clear();
    }

    public ModalId id() {
        return id;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModalWindowStack o) {
            return id.equals(o.id);
        }
        if (obj instanceof ModalId o) {
            return id.equals(o);
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
}
