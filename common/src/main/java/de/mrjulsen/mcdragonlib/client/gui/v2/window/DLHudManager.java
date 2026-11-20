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
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.client.HudEvents;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class DLHudManager {
    private DLHudManager() {}

    private static final DLWindowManager manager = new DLWindowManager();
    public static DLWindowManager getWindowManager() {
        return manager;
    }

    public static boolean isReady() {
        return true;
    }

    public static void init() {
        ClientTickEvent.CLIENT_POST.register((mc) -> {
            if (DragonLib.KEY_OVERLAY_SETTINGS.consumeClick() && manager.hasWindows()) {
                Screen previous = Minecraft.getInstance().screen;
                DLHudInteractionScreen editor = new DLHudInteractionScreen();
                Minecraft.getInstance().setScreen(editor);
                manager.setCloseHandler(mgr -> {
                    if (Minecraft.getInstance().screen == editor) {
                        Minecraft.getInstance().setScreen(previous);
                    }
                });
            }
            manager.tick();
        });
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((mc) -> {
            manager.onClosed();
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((mc) -> {
            manager.onClosed();
        });
        ClientGuiEvent.RENDER_HUD.register((guiGraphics, partialTick) -> {
            int width = (int)GuiUtils.getScreenWidth();
            int height = (int)GuiUtils.getScreenHeight();
            if (manager.screenWidth() != width || manager.screenHeight() != height) {
                manager.resize(width, height);
            }

            boolean interacting = Minecraft.getInstance().screen instanceof DLHudInteractionScreen;
            double mouseX = interacting ? GuiUtils.mouseXOnScreen() : -1;
            double mouseY = interacting ? GuiUtils.mouseYOnScreen() : -1;

            manager.render(guiGraphics, Minecraft.getInstance().font, partialTick, mouseX, mouseY, g -> HudEvents.RENDER.invoker().render(g, partialTick, width, height));
        });
    }

    public static void resizeDisplay() {
        manager.resize((int)GuiUtils.getScreenWidth(), (int)GuiUtils.getScreenHeight());
    }
}
