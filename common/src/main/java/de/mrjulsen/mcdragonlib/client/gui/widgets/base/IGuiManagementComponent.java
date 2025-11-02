package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.nio.file.Path;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public interface IGuiManagementComponent extends Comparable<IGuiManagementComponent> {

    public static enum Phase {
        PRE, POST;
    }

    int getPriority();

    default void onAttach(DLWindowManager manager) {}
    default void onDetach(DLWindowManager manager) {}
    
    default void init() {}
    default void render(DLGuiGraphics graphics, int mouseX, int mouseY, RenderLayer layer) {}
    default void tick() {}
    default void close() {}

    default boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseMoved(double mouseX, double mouseY) { return false; }
    default boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return false; }
    default boolean mouseDragged(Phase phase, double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }
    default boolean charTyped(char codePoint, int modifiers) { return false; }
    default boolean onFilesDrop(List<Path> paths) { return false; }

    @Override
    default int compareTo(IGuiManagementComponent o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

}
