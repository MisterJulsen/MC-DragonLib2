/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
    default void render(Phase phase, DLGuiGraphics graphics, int mouseX, int mouseY, RenderLayer layer) {}
    default void tick() {}
    default void close() {}

    default boolean mouseClicked(Phase phase, boolean consumed, double mouseX, double mouseY, int button) { return false; }
    default boolean mouseMoved(Phase phase, boolean consumed, double mouseX, double mouseY) { return false; }
    default boolean mouseReleased(Phase phase, boolean consumed, double mouseX, double mouseY, int button) { return false; }
    default boolean mouseScrolled(Phase phase, boolean consumed, double mouseX, double mouseY, double scrollX, double scrollY) { return false; }
    default boolean mouseDragged(Phase phase, boolean consumed, double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
    default boolean keyPressed(Phase phase, boolean consumed, int keyCode, int scanCode, int modifiers) { return false; }
    default boolean keyReleased(Phase phase, boolean consumed, int keyCode, int scanCode, int modifiers) { return false; }
    default boolean charTyped(Phase phase, boolean consumed, char codePoint, int modifiers) { return false; }
    default boolean onFilesDrop(Phase phase, boolean consumed, List<Path> paths) { return false; }

    @Override
    default int compareTo(IGuiManagementComponent o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

}
