package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.client.gui.properties.InheritableProperty;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

@SupportsEvents({
    DLPanel.BackgroundColorChangedEvent.class,
    DLPanel.TextColorChangedEvent.class
})
public class DLPanel extends DLGuiComponent {
    
    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}
    public record TextColorChangedEvent(DLColor color) implements IEvent {}

    @InheritableProperty(overrideLocal = false)
    public final ColorProperty textColor = new ColorProperty(DLColor.UNDEFINED, DLColor.TRANSPARENT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLPanel.TextColorChangedEvent(a), true));
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty backgroundTint = new ColorProperty(DLColor.UNDEFINED, DLColor.TRANSPARENT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLPanel.BackgroundColorChangedEvent(a), true));

    public DLPanel(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.inputConsumptionPolicy.set(c -> true);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.fill(graphics, 0, 0, width(), height(), backgroundTint.get());
    }
    
}
