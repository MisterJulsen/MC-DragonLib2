package de.mrjulsen.mcdragonlib.gui.v2.layout;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

/**
 * Base class for layouts with properties. It keeps track of the components using it and the property
 * factories below invalidate all of them on change, so changing a gap is enough to trigger a new
 * layout pass.
 */
public abstract class AbstractLayout implements Layout {

    private final List<GuiComponent> hosts = new ArrayList<>(1);

    @Override
    public void attachHost(GuiComponent host) {
        if (!hosts.contains(host)) {
            hosts.add(host);
        }
    }

    @Override
    public void detachHost(GuiComponent host) {
        hosts.remove(host);
    }

    /**
     * Schedules a new layout pass for every component using this layout.
     */
    protected void invalidateHosts() {
        for (int i = 0; i < hosts.size(); i++) {
            hosts.get(i).invalidateLayout();
        }
    }

    /**
     * Creates a property that invalidates the hosts when it changes.
     */
    protected <T> Property<T> option(T defaultValue) {
        Property<T> property = new Property<>(defaultValue);
        property.addChangeListener((o, n) -> invalidateHosts());
        return property;
    }

    /**
     * Creates a boolean property that invalidates the hosts when it changes.
     */
    protected BooleanProperty flag(boolean defaultValue) {
        BooleanProperty property = new BooleanProperty(defaultValue);
        property.addChangeListener((o, n) -> invalidateHosts());
        return property;
    }

    /**
     * Creates a non-negative length property that invalidates the hosts when it changes.
     */
    protected NumberProperty<Double> length(double defaultValue) {
        NumberProperty<Double> property = new NumberProperty<>(defaultValue, 0.0D, Double.MAX_VALUE);
        property.addChangeListener((o, n) -> invalidateHosts());
        return property;
    }

    /**
     * Creates a bounded int property that invalidates the hosts when it changes.
     */
    protected NumberProperty<Integer> count(int defaultValue, int min, int max) {
        NumberProperty<Integer> property = new NumberProperty<>(defaultValue, min, max);
        property.addChangeListener((o, n) -> invalidateHosts());
        return property;
    }
}
