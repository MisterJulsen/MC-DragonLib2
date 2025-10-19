package de.mrjulsen.mcdragonlib.client.atlas;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class GLGuiTextureData {
    public static final AbstractSprite EMPTY_SPRITE = new StretchedSprite(new int[] {0, 0}, new int[] {0, 0});
    public static final GLGuiTextureData EMPTY = new GLGuiTextureData(Map.of("", EMPTY_SPRITE), new int[] {0, 0});
    public static final GLGuiTextureDataSerializer SERIALIZER = new GLGuiTextureDataSerializer();

    static {
        EMPTY.setTextureLocation(TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    public static abstract class AbstractSprite {
        private GLGuiTextureData data = EMPTY;
        void setMetadata(GLGuiTextureData data) {
            Objects.requireNonNull(data);
            this.data = data;
        }
        public GLGuiTextureData metadata() {
            return data;
        }
        public abstract void render(Graphics graphics, int x, int y, int w, int h);
    }

    public static class StretchedSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;

        public StretchedSprite(int[] uv, int[] size) {
            this.uv = uv;
            this.size = size;
        }

        public int u() {
            return uv[0];
        }

        public int v() {
            return uv[1];
        }

        public int width() {
            return size[0];
        }

        public int height() {
            return size[1];
        }

        @Override
        public void render(Graphics graphics, int x, int y, int w, int h) {
            graphics.graphics().blit(
                metadata().location(),
                x, y, w, h,
                u(),
                v(),
                width(),
                height(),
                metadata().width(),
                metadata().height()
            );
        }
    }

    public static class TiledSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;

        public TiledSprite(int[] uv, int[] size) {
            this.uv = uv;
            this.size = size;
        }

        public int u() {
            return uv[0];
        }

        public int v() {
            return uv[1];
        }

        public int width() {
            return size[0];
        }

        public int height() {
            return size[1];
        }

        @Override
        public void render(Graphics graphics, int x, int y, int w, int h) {
            GuiUtils.drawTexture(
                metadata().location(),
                graphics,
                x, y, w, h,
                u(),
                v(),
                width(),
                height(),
                TextureFillMode.TILE,
                metadata().textureSize[0],
                metadata().textureSize[1]
            );
        }
    }

    public static class NineSlicedSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;
        private final int[] borderSize;
        private final boolean tiledContent;
        private final boolean tiledBorder;

        public NineSlicedSprite(int[] uv, int[] size, int[] borderSize, boolean tiledBorder, boolean tiledContent) {
            this.uv = uv;
            this.size = size;
            this.borderSize = borderSize;
            this.tiledContent = tiledContent;
            this.tiledBorder = tiledBorder;
        }

        public int u() {
            return uv[0];
        }

        public int v() {
            return uv[1];
        }

        public int width() {
            return size[0];
        }

        public int height() {
            return size[1];
        }

        public int leftBorder() {
            return borderSize[0];
        }

        public int topBorder() {
            return borderSize[1];
        }

        public int rightBorder() {
            return borderSize[2];
        }

        public int bottomBorder() {
            return borderSize[3];
        }

        public boolean tiledContent() {
            return tiledContent;
        }

        public boolean tiledBorder() {
            return tiledBorder;
        }

        @Override
        public void render(Graphics graphics, int x, int y, int w, int h) {

            GuiUtils.drawTexture(metadata().location(), graphics, x, y, leftBorder(), topBorder(), u(), v(), leftBorder(), topBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Top Left
            GuiUtils.drawTexture(metadata().location(), graphics, x + w - rightBorder(), y, rightBorder(), topBorder(), u() + width() - rightBorder(), v(), rightBorder(), topBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Top Right
            GuiUtils.drawTexture(metadata().location(), graphics, x, y + h - bottomBorder(), leftBorder(), bottomBorder(), u(), v() + height() - bottomBorder(), leftBorder(), bottomBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Bottom Left
            GuiUtils.drawTexture(metadata().location(), graphics, x + w - rightBorder(), y + h - bottomBorder(), rightBorder(), bottomBorder(), u() + width() - rightBorder(), v() + height() - bottomBorder(), rightBorder(), bottomBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Bottom Right

            GuiUtils.drawTexture(metadata().location(), graphics, x + leftBorder(), y, w - leftBorder() - rightBorder(), topBorder(), u() + leftBorder(), v(), width() - leftBorder() - rightBorder(), topBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Top
            GuiUtils.drawTexture(metadata().location(), graphics, x + leftBorder(), y + h - bottomBorder(), w - leftBorder() - rightBorder(), bottomBorder(), u() + leftBorder(), v() + height() - bottomBorder(), width() - leftBorder() - rightBorder(), bottomBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Bottom
            GuiUtils.drawTexture(metadata().location(), graphics, x, y + topBorder(), leftBorder(), h - topBorder() - bottomBorder(), u(), v() + topBorder(), leftBorder(), height() - topBorder() - bottomBorder(), tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Left
            GuiUtils.drawTexture(metadata().location(), graphics, x + w - rightBorder(), y + topBorder(), rightBorder(), h - topBorder() - bottomBorder(), u() + width() - rightBorder(), v() + topBorder(), rightBorder(), height() - topBorder() - bottomBorder(),  tiledBorder() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]); // Right
            
            GuiUtils.drawTexture(metadata().location(), graphics, x + rightBorder(), y + rightBorder(), w - rightBorder() - leftBorder(), h - topBorder() - bottomBorder(), u() + rightBorder(), v() + topBorder(), width() - rightBorder() - leftBorder(), height() - topBorder() - bottomBorder(), tiledContent() ? TextureFillMode.TILE : TextureFillMode.STRETCH, metadata().textureSize[0], metadata().textureSize[1]);

        }
    }

    private final Map<String, AbstractSprite> sprites;
    private final int[] textureSize;
    private ResourceLocation location;

    public GLGuiTextureData(Map<String, AbstractSprite> sprites, int[] textureSize) {
        this.sprites = sprites;
        this.textureSize = textureSize;
        sprites.values().forEach(x -> x.setMetadata(this));
    }

    public Set<String> availableSprites() {
        return ImmutableSet.copyOf(sprites.keySet());
    }
    
    public AbstractSprite getSprite(String variant) {
        if (sprites.containsKey(variant)) {
            return sprites.get(variant);
        }
        if (sprites.containsKey("")) { // Fallback
            return sprites.get("");
        }
        return EMPTY_SPRITE;
    }

    public int width() {
        return textureSize[0];
    }

    public int height() {
        return textureSize[1];
    }

    public void setTextureLocation(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation location() {
        return location;
    }
}
