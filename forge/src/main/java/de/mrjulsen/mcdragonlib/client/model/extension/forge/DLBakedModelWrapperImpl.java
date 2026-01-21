package de.mrjulsen.mcdragonlib.client.model.extension.forge;

import de.mrjulsen.mcdragonlib.client.model.extension.IBakedQuadExtension;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DLBakedModelWrapperImpl extends BakedModelWrapper<BakedModel> {

    public DLBakedModelWrapperImpl(BakedModel original) {
        super(original);
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

                if (updateQuad) {
                    BakedQuad newQuad = new BakedQuad(
                            quad.getVertices(),
                            quad.getTintIndex(),
                            quad.getDirection(),
                            quad.getSprite(),
                            !ext.dragonlib$getFaceData().emissive() && quad.isShade(),
                            !ext.dragonlib$getFaceData().emissive() && ext.dragonlib$getFaceData().ambientOcclusion()
                    );

                    ((IBakedQuadExtension) newQuad).dragonlib$setFaceData(ext.dragonlib$getFaceData());
                    result.add(newQuad);
                } else {
                    result.add(quad);
                }

            } else {
                result.add(quad);
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return transformQuads(super.getQuads(state, side, rand, data, renderType));
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return transformQuads(super.getQuads(state, side, rand));
    }
}
