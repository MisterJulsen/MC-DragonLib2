package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.DLOverlayScreen;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public class TestOverlay extends DLOverlayScreen {

    @Override
    public void render(Graphics graphics, float partialTicks, int screenWidth, int screenHeight) {
        GuiUtils.drawString(graphics, getFont(), 10, 10, TextUtils.text("Test Text Overlay"), 0xFFFF0000, EAlignment.LEFT, true);

    }
    
}
