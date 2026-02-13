package de.mrjulsen.mcdragonlib.client.model.extension.neoforge;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.QuadTransformers;

import java.util.List;

public class DLBakedQuadImpl extends DLBakedQuad {

    protected DLBakedQuadImpl(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        super(vertices, tintIndex, direction, sprite, shade, ambientOcclusion, emissive, tags);
    }

    @Override
    public boolean hasAmbientOcclusion() {
        return isAmbientOcclusion();
    }

    public static DLBakedQuad create(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion, boolean emissive, List<String> tags) {
        BakedQuad quad = new BakedQuad(vertices, tintIndex, direction, sprite, shade, ambientOcclusion);
        if (emissive) {
            QuadTransformers.settingMaxEmissivity().processInPlace(quad);
        }

        return new DLBakedQuadImpl(
                quad.getVertices(),
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                !emissive && quad.isShade(),
                !emissive && ambientOcclusion,
                emissive,
                tags
        );
    }
}
