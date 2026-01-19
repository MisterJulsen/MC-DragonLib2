package de.mrjulsen.mcdragonlib.client.model.extension.forge;

import de.mrjulsen.mcdragonlib.client.model.extension.IBakedQuadExtension;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.QuadTransformers;

import java.util.ArrayList;
import java.util.List;

public class DLBakedModelWrapperImpl implements BakedModel {

    private final BakedModel original;

    public DLBakedModelWrapperImpl(BakedModel original) {
        this.original = original;
    }

    public static BakedModel wrap(BakedModel original) {
        return new DLBakedModelWrapperImpl(original);
    }

    private static List<BakedQuad> transformQuads(List<BakedQuad> quads) {
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            if (quad instanceof IBakedQuadExtension ext && ext.dragonlib$getFaceData() != null) {
                if (ext.dragonlib$getFaceData().emissive()) {
                    QuadTransformers.settingMaxEmissivity().processInPlace(quad);
                }
                QuadTransformers.applyingColor(ext.dragonlib$getFaceData().color()).processInPlace(quad);

                boolean updateQuad = ext.dragonlib$getFaceData().emissive() || ext.dragonlib$getFaceData().ambientOcclusion() != quad.hasAmbientOcclusion();
                BakedQuad newQuad = !updateQuad ? quad : new BakedQuad(
                        quad.getVertices(),
                        quad.getTintIndex(),
                        quad.getDirection(),
                        quad.getSprite(),
                        !ext.dragonlib$getFaceData().emissive() && quad.isShade(),
                        !ext.dragonlib$getFaceData().emissive() && ext.dragonlib$getFaceData().ambientOcclusion()
                );

                ((IBakedQuadExtension)newQuad).dragonlib$setFaceData(ext.dragonlib$getFaceData());
                result.add(newQuad);
            } else {
                result.add(quad);
            }
        }
        return result;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource random) {
        return transformQuads(original.getQuads(state, direction, random));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return original.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return original.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return original.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return original.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return original.getOverrides();
    }
}
