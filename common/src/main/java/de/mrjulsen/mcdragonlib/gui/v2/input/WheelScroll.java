package de.mrjulsen.mcdragonlib.gui.v2.input;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.AbstractBehaviour;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;

/**
 * Makes a component scrollable with the mouse wheel. Attach it to anything whose content is bigger
 * than its content area.
 * <p>It only consumes the event if the scroll offset really moved, so a list that is already at its
 * end hands the wheel over to its parent.</p>
 */
public final class WheelScroll extends AbstractBehaviour {

    public static final int PRIORITY = GuiEvent.PRIORITY_LOW;

    private final int priority;

    /**
     * Whether the wheel scrolls this component. When off, the event is left to the parents.
     */
    public final BooleanProperty enabled = new BooleanProperty(true);
    /**
     * Whether the wheel scrolls vertically.
     */
    public final BooleanProperty vertical = new BooleanProperty(true);
    /**
     * Whether the wheel scrolls horizontally. With no horizontal wheel the vertical delta is used, so
     * this also makes a sideways list scrollable.
     */
    public final BooleanProperty horizontal = new BooleanProperty(false);
    /**
     * Pixels per wheel notch.
     */
    public final NumberProperty<Double> step = new NumberProperty<>(12.0D, 0.0D, 4096.0D);

    public WheelScroll() {
        this(PRIORITY);
    }

    /**
     * @param priority listener priority, lower than normal by default so components handle the wheel
     *                 themselves first
     */
    public WheelScroll(int priority) {
        this.priority = priority;
    }

    /**
     * Makes the component scrollable with the wheel.
     *
     * @return the behaviour, for further configuration
     */
    public static WheelScroll attach(GuiComponent target) {
        return target.addBehaviour(new WheelScroll());
    }

    @Override
    protected void onAttached(GuiComponent host) {
        listen(GuiEvents.ScrollEvent.class, this::onScroll, priority);
    }

    public GuiComponent target() {
        return host();
    }

    private void onScroll(GuiComponent src, GuiEvents.ScrollEvent event) {
        GuiComponent target = host();
        if (!enabled.get() || target == null) {
            return;
        }
        double amount = step.get();
        if (vertical.get() && event.deltaY() != 0 && target.maxScrollY() > 0) {
            double before = target.scrollY();
            target.setScrollY(before - event.deltaY() * amount);
            if (target.scrollY() != before) {
                event.consume();
                return;
            }
        }
        double delta = event.deltaX() != 0 ? event.deltaX() : event.deltaY();
        if (horizontal.get() && delta != 0 && target.maxScrollX() > 0) {
            double before = target.scrollX();
            target.setScrollX(before - delta * amount);
            if (target.scrollX() != before) {
                event.consume();
            }
        }
    }
}
