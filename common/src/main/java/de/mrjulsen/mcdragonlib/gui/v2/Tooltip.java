package de.mrjulsen.mcdragonlib.gui.v2;

import java.util.List;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.gui.v2.behaviour.AbstractBehaviour;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.render.LayerRegistry;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

/**
 * An immutable tooltip: the lines to show and the width at which they wrap.
 * <p>A tooltip is not rendered by the component that owns it. Attach it with
 * {@link #attach(GuiComponent, Tooltip)} and the {@link Source} behaviour takes care of the rest.</p>
 */
public record Tooltip(List<? extends FormattedText> lines, int maxWidth) {

    public static final int DEFAULT_MAX_WIDTH = 200;
    public static final Tooltip EMPTY = new Tooltip(List.of(), DEFAULT_MAX_WIDTH);

    public static Tooltip of(String text) {
        return new Tooltip(List.of(TextUtils.text(text)), DEFAULT_MAX_WIDTH);
    }

    public static Tooltip of(Component... lines) {
        return new Tooltip(List.of(lines), DEFAULT_MAX_WIDTH);
    }

    public static Tooltip of(List<? extends FormattedText> lines) {
        return new Tooltip(List.copyOf(lines), DEFAULT_MAX_WIDTH);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * Queues this tooltip on the tooltip layer, so it ends up on top of everything else.
     *
     * @param screenX screen position to render at, usually the mouse position
     * @param screenY screen position to render at, usually the mouse position
     */
    public void render(DLGuiGraphics g, double screenX, double screenY) {
        if (isEmpty()) {
            return;
        }
        g.defer(LayerRegistry.TOOLTIP, () -> GuiUtils.tooltip(g, screenX, screenY, lines, maxWidth));
    }

    /**
     * The tooltip currently attached to the given component, or {@link #EMPTY} if it has none.
     */
    public static Tooltip of(GuiComponent component) {
        Source source = component.addBehaviour(Source.class);
        return source == null ? EMPTY : source.resolve();
    }

    /**
     * Attaches a static tooltip to the component, replacing whatever it had before.
     *
     * @return the behaviour holding the tooltip, so it can be changed later
     */
    public static Source attach(GuiComponent component, Tooltip tooltip) {
        Source source = component.addBehaviour(Source.class);
        if (source == null) {
            source = component.addBehaviour(new Source(tooltip));
        } else {
            source.tooltip.set(tooltip);
            source.provider.set(null);
        }
        return source;
    }

    public static Source attach(GuiComponent component, String text) {
        return attach(component, of(text));
    }

    public static Source attach(GuiComponent component, Component... lines) {
        return attach(component, of(lines));
    }

    /**
     * Attaches a tooltip that is rebuilt on every hover. Use this for text that changes at runtime.
     *
     * @return the behaviour holding the provider, so it can be changed later
     */
    public static Source attach(GuiComponent component, Supplier<Tooltip> provider) {
        Source source = component.addBehaviour(Source.class);
        if (source == null) {
            source = component.addBehaviour(new Source(EMPTY));
        }
        source.provider.set(provider);
        return source;
    }

    /**
     * Behaviour that marks a component as having a tooltip and resolves it when needed.
     */
    public static final class Source extends AbstractBehaviour {

        /**
         * The static tooltip. Ignored while a {@link #provider} is set.
         */
        public final Property<Tooltip> tooltip;
        /**
         * Dynamic tooltip, queried on every resolve. Takes precedence over {@link #tooltip} when set.
         */
        public final Property<Supplier<Tooltip>> provider = new Property<>(null);

        public Source(Tooltip tooltip) {
            this.tooltip = new Property<>(tooltip == null ? EMPTY : tooltip);
        }

        /**
         * The tooltip to show right now, never null.
         */
        public Tooltip resolve() {
            Supplier<Tooltip> dynamic = provider.get();
            if (dynamic != null) {
                Tooltip value = dynamic.get();
                return value == null ? EMPTY : value;
            }
            Tooltip value = tooltip.get();
            return value == null ? EMPTY : value;
        }
    }
}
