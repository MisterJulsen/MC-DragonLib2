package de.mrjulsen.mcdragonlib.gui.v2.render;

/**
 * A render layer for deferred drawing. Layers are flushed in ascending order, so a higher order ends
 * up on top. See {@link LayerRegistry} for the built in ones.
 */
public record GuiLayer(String name, int order) implements Comparable<GuiLayer> {

    @Override
    public int compareTo(GuiLayer other) {
        int result = Integer.compare(order, other.order);
        return result != 0 ? result : name.compareTo(other.name);
    }
}
