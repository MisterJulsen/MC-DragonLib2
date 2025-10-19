package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

public enum CursorType {
    ARROW(GLFW.GLFW_ARROW_CURSOR),
    IBEAM(GLFW.GLFW_IBEAM_CURSOR),
    CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
    HAND(GLFW.GLFW_HAND_CURSOR),
    HRESIZE(GLFW.GLFW_HRESIZE_CURSOR),
    VRESIZE(GLFW.GLFW_VRESIZE_CURSOR),
    TRBLRESIZE(GLFW.GLFW_RESIZE_NESW_CURSOR),
    TLBRRESIZE(GLFW.GLFW_RESIZE_NWSE_CURSOR),
    ALLRESIZE(GLFW.GLFW_RESIZE_ALL_CURSOR),
    NOT_ALLOWED(GLFW.GLFW_NOT_ALLOWED_CURSOR);

    private final int shape;
    private long cursor = 0L;

    private static long currentCursor = 0L;

    CursorType(int c) {
        shape = c;
    }

    public static void set(CursorType type) {
        var window = Minecraft.getInstance().getWindow().getWindow();

        if (type == null) {
            GLFW.glfwSetCursor(window, MemoryUtil.NULL);
            currentCursor = 0L;
            return;
        }

        if (type.cursor == 0L) {
            type.cursor = GLFW.glfwCreateStandardCursor(type.shape);
        }

        if (currentCursor != type.cursor) {
            GLFW.glfwSetCursor(window, type.cursor);
            currentCursor = type.cursor;
        }
    }
}