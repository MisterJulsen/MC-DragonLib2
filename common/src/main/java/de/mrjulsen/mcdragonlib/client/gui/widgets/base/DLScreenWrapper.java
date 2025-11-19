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

public class DLScreenWrapper<M extends AbstractContainerMenu> extends Screen implements MenuAccess<M> {

    private final DLWindowManager root;
    private final M menu;
    
    public <T extends DLWindow> DLScreenWrapper(@Nullable M menu, WindowBuilder<T> window) {
        super(TextUtils.empty());
        final Screen previousScreen = Minecraft.getInstance().screen;
        this.menu = menu;
        this.root = new DLWindowManager(menu, window, width, height, (mgr) -> {
            Minecraft.getInstance().setScreen(mgr.shouldShowPreviousScreenOnClose() ? previousScreen : null);
        });
    }

    public DLWindowManager getWindowManager() {
        return root;
    }

    public boolean supportsMenus() {
        return menu != null;
    }



    @Override
    protected void init() {
        super.init();
        root.updateLayout(width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        DLGuiGraphics graphics = new DLGuiGraphics(guiGraphics, guiGraphics.pose(), Minecraft.getInstance().font, partialTick);
        root.render(graphics, mouseX, mouseY);
    }

    @Override
    public void tick() {
        root.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return root.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        root.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return root.mouseReleased(mouseX, mouseY, button);
    }

    public boolean onScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        return root.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return root.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return root.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return root.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return root.charTyped(codePoint, modifiers);
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        root.onFilesDrop(paths);
    }

    @Override
    public void onClose() {
        root.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return root.isPauseScreen();
    }

    @Override
    public M getMenu() {
        return menu;
    }
}
