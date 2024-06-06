package de.mrjulsen.mcdragonlib.client.render;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;

public class DynamicGuiRenderer {

    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;
    protected static final int UI_SECTION_SIZE = 5;

    public static final int DRAGONLIB_UI_COLOR = 0xFF404040;
    public static final int DRAGONLIB_UI_COLOR_HIGHLIGHT = 0xFF707070;
    public static final int DRAGONLIB_UI_COLOR_DISABLED = 0xFF202020;

    public static void renderArea(Graphics graphics, GuiAreaDefinition area, AreaStyle style, ButtonState state) {
        renderArea(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight(), style, state);
    }

    public static void renderArea(Graphics graphics, int x, int y, int w, int h, AreaStyle style, ButtonState state) {

        if (style == AreaStyle.DRAGONLIB) {
            switch (state) {
                case DOWN:
                    renderColorableButton(graphics, x, y, w, h, DRAGONLIB_UI_COLOR, true);
                    break;
                case SELECTED:
                    renderColorableButton(graphics, x, y, w, h, DRAGONLIB_UI_COLOR_HIGHLIGHT, false);
                    GuiUtils.drawBox(graphics, new GuiAreaDefinition(x, y, w, h), 0x00FFFFFF, 0xFFFFFFFF);
                    break;
                case DISABLED:
                    renderColorableButton(graphics, x, y, w, h, DRAGONLIB_UI_COLOR_DISABLED, true);
                    break;
                default:
                    renderColorableButton(graphics, x, y, w, h, DRAGONLIB_UI_COLOR, false);
                    break;
            }
            return;
        }

        if (style == AreaStyle.NATIVE) {
            int i = 0;
            switch (state) {
                case SELECTED:
                    i = 2;
                    break;
                case DOWN:
                case DISABLED:
                    i = 0;
                    break;
                default:
                    i = 1;
                    break;
            }

            int bottomH = h - (h / 2);
            GuiUtils.drawTexture(DragonLib.NATIVE_WIDGETS, graphics, x, y, 0, 46 + i * 20, w / 2, h / 2);
            GuiUtils.drawTexture(DragonLib.NATIVE_WIDGETS, graphics, x + w / 2, y, 200 - w / 2, 46 + i * 20, w / 2, h / 2);       
            GuiUtils.drawTexture(DragonLib.NATIVE_WIDGETS, graphics, x, y + h / 2, 0, 46 + (i + 1) * 20 - bottomH, w / 2, bottomH);
            GuiUtils.drawTexture(DragonLib.NATIVE_WIDGETS, graphics, x + w / 2, y + h / 2, 200 - w / 2, 46 + (i + 1) * 20 - bottomH, w / 2, bottomH);
        }

        int startU = 0, startV = style.getIndex() * 5;
        startU += state.getIndex() * UI_SECTION_SIZE;
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static void renderContainerBackground(Graphics graphics, GuiAreaDefinition area) {
        renderContainerBackground(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight());
    }

    public static void renderContainerBackground(Graphics graphics, int x, int y, int w, int h) {
        int startU = 20, startV = 0;
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static void renderColorableButton(Graphics graphics, GuiAreaDefinition area, int color, boolean sunken) {
        renderContainerBackground(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight());
    }

    public static void renderColorableButton(Graphics graphics, int x, int y, int w, int h, int color, boolean sunken) {
        int startU = sunken ? 25 : 20, startV = 10;
        GuiUtils.setTint(color);
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        GuiUtils.resetTint();
    }

    public static void renderWindow(Graphics graphics, GuiAreaDefinition area) {
        renderWindow(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight());
    }

    public static void renderWindow(Graphics graphics, GuiAreaDefinition area, boolean rounded) {
        renderWindow(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight(), rounded);
    }

    public static void renderWindow(Graphics graphics, int x, int y, int w, int h) {
        renderWindow(graphics, x, y, w, h, true);
    }

    public static void renderWindow(Graphics graphics, int x, int y, int w, int h, boolean rounded) {
        int startU = rounded ? 0 : 10, startV = 15;

        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y, 4, 4, startU, startV, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + h - 4, 4, 4, startU, startV + 6, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 4, y, 4, 4, startU + 6, startV, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 4, y + h - 4, 4, 4, startU + 6, startV + 6, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 4, y, w - 8, 4, startU + 4, startV, 2, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 4, y + h - 4, w - 8, 4, startU + 4, startV + 6, 2, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.drawTexture(DragonLib.UI, graphics, x, y + 4, 4, h - 8, startU, startV + 4, 4, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + w - 4, y + 4, 4, h - 8, startU + 6, startV + 4, 4, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.drawTexture(DragonLib.UI, graphics, x + 4, y + 4, w - 8, h - 8, startU + 4, startV + 4, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static enum ButtonState {
        BUTTON(0),
        SELECTED(1),
        DOWN(2),
        DISABLED(3);

        private int index;

        private ButtonState(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static enum AreaStyle {
        NATIVE(-1),
        BROWN(0),
        GRAY(1),
        RED(2),
        DRAGONLIB(100);

        private int index;

        private AreaStyle(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}

