package de.mrjulsen.mcdragonlib.client.model.extension;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.List;

public abstract class DLBakedQuad extends BakedQuad {

    protected final boolean ambientOcclusion;
    protected final boolean emissive;
    protected final List<String> tags;

    protected DLBakedQuad(BakedQuad quad, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        this(quad.getVertices(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), ambientOcclusion, emissive, tags);
    }

    protected DLBakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        super(vertices, tintIndex, direction, sprite, shade);
        this.ambientOcclusion = ambientOcclusion;
        this.emissive = emissive;
        this.tags = tags;
    }

    public boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    public boolean isEmissive() {
        return emissive;
    }

    public List<String> getTags() {
        return tags;
    }

    public static DLBakedQuad create(BakedQuad quad, DLFaceData data) {
        return create(quad.getVertices(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), data.ambientOcclusion(), data.emissive(), data.tags());
    }

    @ExpectPlatform
    public static DLBakedQuad create(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        throw new AssertionError();
    }
}
