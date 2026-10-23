package de.mrjulsen.mcdragonlib.gui.v2.window;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * Bridge between Minecraft's Screen and the v2 GUI: forwards input, rendering and ticks to a
 * {@link WindowManager} and remembers the screen it was opened from.
 * <p>Usually there is no need to subclass this, see {@link #open(Function)}.</p>
 */
public class DLScreen extends Screen {

    private final WindowManager manager = new WindowManager();
    private final Function<WindowManager, Window> initialWindow;
    private final Screen previousScreen;
    private boolean initialized;

    /**
     * @param initialWindow builds the window to open as soon as the screen is initialized; may be null
     *                      for a screen whose windows are opened later
     */
    public DLScreen(Function<WindowManager, Window> initialWindow) {
        super(TextUtils.empty());
        this.initialWindow = initialWindow;
        this.previousScreen = Minecraft.getInstance().screen;
        this.manager.setCloseHandler(m -> Minecraft.getInstance().setScreen(m.showPreviousScreenOnClose.get() ? previousScreen : null));
    }

    /**
     * Opens a new screen with a single window on it. The usual entry point into a v2 GUI.
     */
    public static void open(Function<WindowManager, Window> window) {
        Minecraft.getInstance().setScreen(new DLScreen(window));
    }

    public WindowManager getWindowManager() {
        return manager;
    }

    @Override
    protected void init() {
        super.init();
        manager.resize(width, height);
        if (!initialized) {
            initialized = true;
            if (initialWindow != null) {
                manager.openWindow(initialWindow.apply(manager));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        manager.render(guiGraphics, Minecraft.getInstance().font, partialTick, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        manager.tick();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        manager.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return manager.mousePressed(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return manager.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return manager.mouseDragged(mouseX, mouseY, button, dragX, dragY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return manager.mouseScrolled(mouseX, mouseY, 0, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (manager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && manager.escapePressed()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return manager.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return manager.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (!manager.filesDropped(paths)) {
            super.onFilesDrop(paths);
        }
    }

    @Override
    public void onClose() {
        manager.onClosed();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return manager.isPauseScreen();
    }
}
