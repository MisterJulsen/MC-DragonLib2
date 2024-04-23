package de.mrjulsen.mcdragonlib.client.gui.widgets;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public class DLInvisiblePanel extends DLButton {

    public DLInvisiblePanel(int pX, int pY, int pWidth, int pHeight) {
        super(pX, pY, pWidth, pHeight, TextUtils.empty(), (b) -> {});
    }    

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return true;
    }
}
