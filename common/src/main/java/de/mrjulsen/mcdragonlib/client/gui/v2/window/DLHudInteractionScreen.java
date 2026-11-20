/*
 * DragonLib 3 - 1.20.1
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

package de.mrjulsen.mcdragonlib.client.gui.v2.window;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.List;

public final class DLHudInteractionScreen extends Screen {
    public DLHudInteractionScreen() {
        super(TextUtils.EMPTY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        DLHudManager.getWindowManager().mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return (DLHudManager.isReady() && DLHudManager.getWindowManager().mousePressed(mouseX, mouseY, button)) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return (DLHudManager.isReady() && DLHudManager.getWindowManager().mouseReleased(mouseX, mouseY, button)) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return (DLHudManager.isReady() && DLHudManager.getWindowManager().mouseDragged(mouseX, mouseY, button, dragX, dragY)) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return (DLHudManager.isReady() && DLHudManager.getWindowManager().mouseScrolled(mouseX, mouseY, 0, delta)) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (DLHudManager.isReady() && DLHudManager.getWindowManager().keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (DragonLib.KEY_OVERLAY_SETTINGS.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return (DLHudManager.isReady() && DLHudManager.getWindowManager().keyReleased(keyCode, scanCode, modifiers)) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (DLHudManager.isReady() && DLHudManager.getWindowManager().charTyped(codePoint, modifiers)) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (!DLHudManager.isReady() || !DLHudManager.getWindowManager().filesDropped(paths)) {
            super.onFilesDrop(paths);
        }
    }

    @Override
    public void removed() {
        super.removed();
        DLWindowManager manager = DLHudManager.getWindowManager();
        manager.closeAllPopups();
        manager.focus(null);
        manager.router().clearState();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
