package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import net.minecraft.network.chat.FormattedText;

public record DLTooltip(List<FormattedText> lines, int maxWidth) {

    public static final DLTooltip EMPTY = new DLTooltip(List.of(), 100);

    public void render(DLGuiGraphics graphics, int x, int y) {
        GuiUtils.drawTooltip(graphics, graphics.defaultFont(), x, y, lines(), maxWidth());
    }    
}
