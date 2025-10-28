package de.mrjulsen.mcdragonlib.client.util;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import net.minecraft.world.item.ItemStack;

public class DLSprite {
    private final DLTexture texture;
    private final ItemStack item;
    private final boolean itemDecorations;
    private final int width;
    private final int height;
    private final int u;
    private final int v;
    private final int uWidth;
    private final int vHeight;

    public DLSprite(DLTexture texture, int width, int height) {
        this(texture, width, height, 0, 0, width, height);
    }
    
    public DLSprite(DLTexture texture, int width, int height, int u, int v) {
        this(texture, width, height, u, v, width, height);
    }
    
    public DLSprite(DLTexture texture, int width, int height, int u, int v, int uWidth, int vHeight) {
        this(texture, null, false, width, height, u, v, uWidth, vHeight);
    }

    private DLSprite(DLTexture texture, ItemStack stack, boolean itemDecorations, int width, int height, int u, int v, int uWidth, int vHeight) {
        this.item = stack;
        this.texture = texture;
        this.itemDecorations = itemDecorations;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
    }
    
    public DLSprite(ItemStack stack, int size, boolean decorated) {
        this(null, stack, decorated, size, size, 0, 0, size, size);
    }
    
    public static DLSprite empty() {
        return new DLSprite(null, null, false, 0, 0, 0, 0, 0, 0);
    }

    public Optional<DLTexture> getTexture() {
        return Optional.ofNullable(texture);
    }

    public Optional<ItemStack> getItem() {
        return Optional.ofNullable(item);
    }

    public boolean isItemDecorated() {
        return itemDecorations;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getUWidth() {
        return uWidth;
    }

    public int getVHeight() {
        return vHeight;
    }

    public boolean isEmpty() {
        return (texture == null && item == null) || (width <= 0 && height <= 0);
    }

    public void render(DLGuiGraphics graphics, int x, int y) {
        if (isEmpty()) {
            return;
        }
        if (getTexture().isPresent()) {
            GuiUtils.drawTexture(texture, graphics, x, y, width, height, u, v, uWidth, vHeight, TextureFillMode.STRETCH);
        } else if (getItem().isPresent()) {
            GuiUtils.renderItem(graphics, item, x, y, 1F / 16F * width, itemDecorations);
        }
    }
}
