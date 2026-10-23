package de.mrjulsen.mcdragonlib.gui.v2.theme;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;

@FunctionalInterface
/**
 * Draws the background of a widget, so the look can be swapped out through the theme instead of by
 * subclassing. Widgets keep their renderer in a theme key of their own, e.g. Button#RENDERER.
 * <p>Coordinates are the widget's local ones, so the rectangle usually starts at 0/0.</p>
 */
public interface WidgetRenderer<T extends GuiComponent> {

    /**
     * Draws nothing. For widgets that should have no background at all.
     */
    WidgetRenderer<?> NONE = (g, component, x, y, width, height) -> {};

    void render(DLGuiGraphics g, GuiComponent component, double x, double y, double width, double height);
}
