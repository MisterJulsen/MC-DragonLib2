package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.nio.file.Path;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager.WindowBuilder;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class DLScreenWrapper extends Screen {

    private final DLWindowManager root;
    
    public DLScreenWrapper(WindowBuilder<?> window) {
        super(TextUtils.empty());
        this.root = new DLWindowManager(window, width, height, () -> {
            Minecraft.getInstance().setScreen(null);
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
        return root.iterateCurrentModal((win, consumed) -> root.mouseClicked(win, consumed, mouseX, mouseY, button), () -> root.prepareMouseClick(mouseX, mouseY, button), (consumed) -> {
            if (!consumed) {
                root.updateWindowFocus(true);
            }
        });
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        root.iterateCurrentModal((win, consumed) -> root.mouseMoved(win, consumed, mouseX, mouseY), null, null);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return root.iterateCurrentModal((win, consumed) -> root.mouseReleased(win, consumed, mouseX, mouseY, button), null, (consumed) -> root.finishMouseRelease(mouseX, mouseY));
    }

    public boolean onScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        return root.iterateCurrentModal((win, consumed) -> root.mouseScrolled(win, consumed, mouseX, mouseY, scrollX, scrollY), null, null);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return root.iterateCurrentModal((win, consumed) -> root.mouseDragged(win, consumed, mouseX, mouseY, button, dragX, dragY), () -> root.prepareMouseDragged(mouseX, mouseY, button, dragX, dragY), (consumed) -> root.finishMouseDragged(mouseX, mouseY, button, dragX, dragY));
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
    public void onFilesDrop(List<Path> packs) {
        root.iterateCurrentModal((win, consumed) -> root.onFilesDrop(win, consumed, packs), null, null);
    }

    @Override
    public void onClose() {
        root.onClose();
    }
}
