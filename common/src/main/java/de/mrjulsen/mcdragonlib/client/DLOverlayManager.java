package de.mrjulsen.mcdragonlib.client;

import de.mrjulsen.mcdragonlib.client.newgui.test.RedWindow;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;

public final class DLOverlayManager {
    private DLOverlayManager() {}

    private static final DLWindowManager root = new DLWindowManager(RedWindow::new, GuiUtils.getScreenWidth(), GuiUtils.getScreenHeight(), () -> {});

    public static void init() {
        ClientTickEvent.CLIENT_POST.register((mc) -> {
            root.tick();
        });

        dev.architectury.event.events.client.ClientGuiEvent.INIT_POST.register((guiGraphics, screen) -> {
            root.updateLayout((int)GuiUtils.getScreenWidth(), (int)GuiUtils.getScreenHeight());
        });
        

        dev.architectury.event.events.client.ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((mc) -> {            
            root.createWindow(RedWindow::new);
        });

        dev.architectury.event.events.client.ClientGuiEvent.RENDER_HUD.register((guiGraphics, partialTick) -> {
            Graphics graphics = new Graphics(guiGraphics, guiGraphics.pose(), Minecraft.getInstance().font, partialTick);
            root.render(graphics, (int)GuiUtils.mouseXOnScreen(), (int)GuiUtils.mouseYOnScreen());
        });

        dev.architectury.event.events.client.ClientScreenInputEvent.CHAR_TYPED_POST.register((mc, screen, codePoint, modifiers) -> {
            boolean result = root.charTyped(codePoint, modifiers);
            return result ? EventResult.interruptTrue() : EventResult.pass();
        });
        dev.architectury.event.events.client.ClientScreenInputEvent.KEY_PRESSED_POST.register((mc, screen, keyCode, scanCode, modifiers) -> {
            boolean result = root.keyPressed(keyCode, scanCode, modifiers);
            return result ? EventResult.interruptTrue() : EventResult.pass();
        });
        dev.architectury.event.events.client.ClientScreenInputEvent.KEY_RELEASED_POST.register((mc, screen, keyCode, scanCode, modifiers) -> {
            boolean result = root.keyReleased(keyCode, scanCode, modifiers);
            return result ? EventResult.interruptTrue() : EventResult.pass();
        });
        dev.architectury.event.events.client.ClientScreenInputEvent.MOUSE_CLICKED_POST.register((mc, screen, mouseX, mouseY, button) -> {
            boolean result = root.iterateCurrentModal((win, consumed) -> root.mouseClicked(win, consumed, mouseX, mouseY, button), () -> root.prepareMouseClick(mouseX, mouseY, button), (consumed) -> {
                if (!consumed) {
                    root.updateWindowFocus(true);
                }
            });            
            return result ? EventResult.interruptTrue() : EventResult.pass();
        });
        dev.architectury.event.events.client.ClientScreenInputEvent.MOUSE_DRAGGED_POST.register((mc, screen, mouseX, mouseY, button, dragX, dragY) -> {
            boolean result = root.iterateCurrentModal((win, consumed) -> root.mouseDragged(win, consumed, mouseX, mouseY, button, dragX, dragY), () -> root.prepareMouseDragged(mouseX, mouseY, button, dragX, dragY), (consumed) -> root.finishMouseDragged(mouseX, mouseY, button, dragX, dragY));
            return result ? EventResult.interruptTrue() : EventResult.pass();
        });
        dev.architectury.event.events.client.ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((mc, screen, mouseX, mouseY, button) -> {
            boolean result = root.iterateCurrentModal((win, consumed) -> root.mouseReleased(win, consumed, mouseX, mouseY, button), null, (consumed) -> root.finishMouseRelease(mouseX, mouseY));
            return result ? EventResult.interruptTrue() : EventResult.pass();
        });
        dev.architectury.event.events.client.ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((mc) -> {
            root.close();
        });
    }

    public static void mouseMoved(double mouseX, double mouseY) {
        root.iterateCurrentModal((win, consumed) -> root.mouseMoved(win, consumed, mouseX, mouseY), null, null);
    }

    public static boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return root.iterateCurrentModal((win, consumed) -> root.mouseScrolled(win, consumed, mouseX, mouseY, scrollX, scrollY), null, null);
    }

    public static void resizeDisplay() {
        root.updateLayout((int)GuiUtils.getScreenWidth(), (int)GuiUtils.getScreenHeight());
    }
}

