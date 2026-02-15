package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.nio.file.Path;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DLContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {

    private final DLWindowManager root;
    
    public <T extends DLWindow> DLContainerScreen(M menu, Inventory playerInventory, Component title, WindowBuilder<T> window) {
        super(menu, playerInventory, title);
        final Screen previousScreen = Minecraft.getInstance().screen;
        this.root = new DLWindowManager(menu, window, width, height, (mgr) -> {
            Minecraft.getInstance().setScreen(mgr.shouldShowPreviousScreenOnClose() ? previousScreen : null);
        });
    }

    public DLWindowManager getWindowManager() {
        return root;
    }

    @Override
    protected void init() {
        super.init();
        root.updateLayout(width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        DLGuiGraphics graphics = new DLGuiGraphics(guiGraphics, guiGraphics.pose(), Minecraft.getInstance().font, partialTick);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        root.render(graphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
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

    @Override
    protected final void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        DLGuiGraphics graphics = new DLGuiGraphics(guiGraphics, guiGraphics.pose(), Minecraft.getInstance().font, partialTick);
        renderBg(graphics, mouseX, mouseY);
    }

    protected void renderBg(DLGuiGraphics guiGraphics, int mouseX, int mouseY) {
    }
}
