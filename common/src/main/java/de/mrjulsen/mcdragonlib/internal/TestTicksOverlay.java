package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.DLOverlayScreen;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public class TestTicksOverlay extends DLOverlayScreen {

    private int ticks;

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void render(Graphics graphics, float partialTicks, int screenWidth, int screenHeight) {
        GuiUtils.drawString(graphics, font, 10, 20, TextUtils.text(String.valueOf(ticks)), 0xFFFF0000, EAlignment.LEFT, true);
    }
    
}
