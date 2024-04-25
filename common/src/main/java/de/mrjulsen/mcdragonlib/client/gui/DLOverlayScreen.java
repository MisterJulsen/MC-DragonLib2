package de.mrjulsen.mcdragonlib.client.gui;

import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public abstract class DLOverlayScreen implements ITickable {

    private final long id = System.nanoTime();
    protected final Font font;

    @SuppressWarnings("resource")
    public DLOverlayScreen() {
        this.font = Minecraft.getInstance().font;
    }
    
    public final long getId() {
        return id;
    }

    public abstract void render(Graphics graphics, float partialTicks, int screenWidth, int screenHeight);
    
    public void onClose() {}

    @Override
    public void tick() {}

    public boolean isStatic() {
        return false;
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return false;
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return false;
    }
}
