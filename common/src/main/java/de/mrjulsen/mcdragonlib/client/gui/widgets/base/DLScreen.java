package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DLScreen<M extends AbstractContainerMenu> extends Screen {

    private final DLWindowManager root;

    @Deprecated(forRemoval = true)
    public <T extends DLWindow> DLScreen(@Nullable M menu, WindowBuilder<T> window) {
        super(TextUtils.empty());
        final Screen previousScreen = Minecraft.getInstance().screen;
        this.root = new DLWindowManager(null, window, width, height, (mgr) -> {
            Minecraft.getInstance().setScreen(mgr.shouldShowPreviousScreenOnClose() ? previousScreen : null);
        });
    }

    public <T extends DLWindow> DLScreen(WindowBuilder<T> window) {
        super(TextUtils.empty());
        final Screen previousScreen = Minecraft.getInstance().screen;
        this.root = new DLWindowManager(null, window, width, height, (mgr) -> {
            Minecraft.getInstance().setScreen(mgr.shouldShowPreviousScreenOnClose() ? previousScreen : null);
        });
    }

    public DLWindowManager getWindowManager() {
        return root;
    }

    @Deprecated(forRemoval = true)
    public boolean supportsMenus() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        root.updateLayout(width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        DLGuiGraphics graphics = new DLGuiGraphics(guiGraphics, guiGraphics.pose(), Minecraft.getInstance().font, partialTick);
        root.render(graphics, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        root.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return root.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        boolean b = root.mouseMoved(mouseX, mouseY);
        if (!b) {
            super.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return root.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return root.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return root.mouseDragged(mouseX, mouseY, button, dragX, dragY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return root.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return root.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return root.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        boolean b = root.onFilesDrop(paths);
        if (!b) {
            super.onFilesDrop(paths);
        }
    }

    @Override
    public void onClose() {
        root.onClose();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return root.isPauseScreen();
    }

    @Deprecated(forRemoval = true)
    public M getMenu() {
        return null;
    }
}
