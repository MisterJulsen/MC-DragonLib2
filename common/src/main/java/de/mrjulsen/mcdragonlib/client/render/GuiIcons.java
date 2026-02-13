package de.mrjulsen.mcdragonlib.client.render;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.resources.ResourceLocation;

public enum GuiIcons {
    EMPTY("empty", 0, 0),
    CHECK("check", 1, 0),
    CROSS("cross", 2, 0),
    WARN("warn", 3, 0),
    CHECKMARK("checkmark", 4, 0),
    BULLET("bullet", 5, 0),
    ARROW_DOWN("arrow_down", 6, 0),
    ARROW_UP("arrow_up", 7, 0),
    ARROW_LEFT("arrow_left", 8, 0),
    ARROW_RIGHT("arrow_right", 9, 0),
    X("x", 10, 0);
    
    private String id;
    private int u;
    private int v;

    public static final int ICON_SIZE = 16;
    public static final DLTexture ICON_TEXTURE = new DLTexture(DLUtils.resourceLocation(DragonLib.MODID, "textures/gui/icons.png"), 256, 256);

    GuiIcons(String id, int u, int v) {
        this.id = id;
        this.u = u;
        this.v = v;
    }

    public String getId() {
        return id;
    }

    public int getUMultiplier() {
        return u;
    }

    public int getVMultiplier() {
        return v;
    }

    public int getU() {
        return u * ICON_SIZE;
    }

    public int getV() {
        return v * ICON_SIZE;
    }

    public static GuiIcons getByStringId(String id) {
        return Arrays.stream(values()).filter(x -> x.getId().equals(id)).findFirst().orElse(GuiIcons.EMPTY);
    }

    public void render(DLGuiGraphics graphics, int x, int y) {
        GuiUtils.drawTexture(GuiIcons.ICON_TEXTURE, graphics, x, y, ICON_SIZE, ICON_SIZE, getU(), getV());
    }

    public DLSprite getAsSprite(int renderWidth, int renderHeight) {
        return new DLSprite(ICON_TEXTURE, ICON_SIZE, ICON_SIZE, getU(), getV(), renderWidth, renderHeight);
    }
}
