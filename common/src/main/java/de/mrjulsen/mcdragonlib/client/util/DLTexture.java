package de.mrjulsen.mcdragonlib.client.util;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public class DLTexture {
    private final ResourceLocation texture;
    private final int textureId;
    private final boolean useTextureId;
    private final int textureWidth;
    private final int textureHeight;

    public DLTexture(ResourceLocation texture, int textureId, boolean useTextureId, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.textureId = textureId;
        this.useTextureId = useTextureId;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public DLTexture(ResourceLocation texture, int width, int height) {
        this(texture, 0, false, width, height);
    }

    public DLTexture(int textureId, int width, int height) {
        this(null, textureId, true, width, height);
    }

    public Optional<ResourceLocation> getTexture() {
        return Optional.ofNullable(texture);
    }

    public int getTextureId() {
        return textureId;
    }

    public boolean usesTextureId() {
        return useTextureId;
    }

    public int width() {
        return textureWidth;
    }

    public int height() {
        return textureHeight;
    }
}
