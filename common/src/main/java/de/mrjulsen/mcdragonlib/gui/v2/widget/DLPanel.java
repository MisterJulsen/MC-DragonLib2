package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;

public class DLPanel extends GuiComponent {

    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}

    public final ColorProperty backgroundColor = new ColorProperty(DLColor.UNDEFINED, DLColor.TRANSPARENT);

    public DLPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.backgroundColor.addChangeListener((o, n) -> invokeEvent(this, new DLPanel.BackgroundColorChangedEvent(n), true));
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.fill(g, 0, 0, width(), height(), backgroundColor.get());
    }
}
