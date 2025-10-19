package de.mrjulsen.mcdragonlib.client.newgui.widgets.components;

import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.client.newgui.properties.InheritableProperty;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.Color;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public class DLPanel extends DLGuiComponent {

    @InheritableProperty(overrideLocal = false)
    public final ColorProperty textColor = new ColorProperty(Color.UNDEFINED, Color.TRANSPARENT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLGuiCommonEvents.TextColorChangedEvent(a), true));
    @InheritableProperty(overrideLocal = false)
    public final ColorProperty backgroundTint = new ColorProperty(Color.UNDEFINED, Color.TRANSPARENT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLGuiCommonEvents.BackgroundColorChangedEvent(a), true));

    public DLPanel(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.inputConsumptionPolicy.set(c -> true);
    }

    @Override
    public void renderMainLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.fill(graphics, 0, 0, width(), height(), backgroundTint.get());
    }
    
}
