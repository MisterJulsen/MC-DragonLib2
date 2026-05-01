package de.mrjulsen.mcdragonlib.client.atlas;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
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
            graphics.graphics().blitRepeating(
                metadata().location(),
                x, y, w, h,
                u(),
                v(),
                width(),
                height()
            );
        }
    }

    public static class NineSlicedSprite extends AbstractSprite {

        private final int[] uv;
        private final int[] size;
        private final int[] borderSize;

        public NineSlicedSprite(int[] uv, int[] size, int[] borderSize) {
            this.uv = uv;
            this.size = size;
            this.borderSize = borderSize;
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

        @Override
        public void render(Graphics graphics, int x, int y, int w, int h) {
            graphics.graphics().blitNineSliced(
                metadata().location(),
                x, y, w, h,
                leftBorder(),
                topBorder(),
                rightBorder(),
                bottomBorder(),
                width(),
                height(),
                u(),
                v()
            );
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
