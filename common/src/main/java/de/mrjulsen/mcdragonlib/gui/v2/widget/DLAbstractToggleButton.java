package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;

import java.util.List;

public abstract class DLAbstractToggleButton extends DLAbstractButton {

    public record CheckedChangedEvent(boolean checked) implements IEvent {}

    public final BooleanProperty checked = new BooleanProperty(false);
    public final BooleanProperty radioButtonMode = new BooleanProperty(false);

    public final EventListenerId defaultToggleActionEventId;

    public DLAbstractToggleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        checked.addChangeListener((o, n) -> {
            invokeEvent(this, new CheckedChangedEvent(n));
        });

        defaultToggleActionEventId = addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
            if (!checked.get() || !radioButtonMode.get()) {
                checked.toggle();
            }
            if (checked.get() && radioButtonMode.get()) {
                getParent().ifPresent(p -> {
                    List<GuiComponent> components = p.getComponentsIf(c -> c != this && c instanceof DLAbstractToggleButton t && t.radioButtonMode.get());
                    for (GuiComponent c : components) {
                        ((DLAbstractToggleButton)c).checked.set(false);
                    }
                });
            }
        });
    }
}
