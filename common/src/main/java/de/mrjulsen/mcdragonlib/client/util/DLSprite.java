package de.mrjulsen.mcdragonlib.client.util;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import net.minecraft.world.item.ItemStack;

/**
 * Lightweight representation of a drawable sprite that can either reference a texture
 * region or an ItemStack. Instances are immutable and carry all layout information
 * (size, texture UVs or item decoration flag).
 *
 * <p>This class is intended for UI rendering helpers where an image may be either a
 * packed texture region (DLTexture) or a single item render.
 */
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

    /**
	 * Returns an Optional wrapping the texture backing this sprite.
	 *
	 * @return optional texture; empty when this sprite is item-backed
	 */
	public Optional<DLTexture> getTexture() {
		return Optional.ofNullable(texture);
	}

	/**
	 * Returns an Optional wrapping the ItemStack backing this sprite.
	 *
	 * @return optional item; empty when this sprite is texture-backed
	 */
	public Optional<ItemStack> getItem() {
		return Optional.ofNullable(item);
	}

	/**
	 * Whether item decorations (stack count, overlays) should be rendered for item sprites.
	 *
	 * @return true when item decorations are enabled
	 */
	public boolean isItemDecorated() {
		return itemDecorations;
	}

	/**
	 * Sprite width in pixels.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sprite height in pixels.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Texture U coordinate (source X) for texture-backed sprites.
	 *
	 * @return source U
	 */
	public int getU() {
		return u;
	}

	/**
	 * Texture V coordinate (source Y) for texture-backed sprites.
	 *
	 * @return source V
	 */
	public int getV() {
		return v;
	}

	/**
	 * Texture source width (region width).
	 *
	 * @return source width
	 */
	public int getUWidth() {
		return uWidth;
	}

	/**
	 * Texture source height (region height).
	 *
	 * @return source height
	 */
	public int getVHeight() {
		return vHeight;
	}

	/**
	 * Returns true when the sprite has no valid texture nor item or has non-positive size.
	 *
	 * @return true if sprite is empty / nothing to render
	 */
	public boolean isEmpty() {
		return (texture == null && item == null) || (width <= 0 && height <= 0);
	}

	/**
	 * Render the sprite using the provided graphics context at the given screen coordinates.
	 *
	 * <p>If the sprite is texture-backed the texture is drawn; if item-backed the item is rendered.
	 * Empty sprites are ignored.
	 *
	 * @param graphics rendering helper/context
	 * @param x screen X coordinate
	 * @param y screen Y coordinate
	 */
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
