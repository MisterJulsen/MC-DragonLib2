package de.mrjulsen.mcdragonlib.client.model.extension.fabric;

import de.mrjulsen.mcdragonlib.client.model.extension.IBakedQuadExtension;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class EmissiveQuadTransform implements RenderContext.QuadTransform {

    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    private static final RenderMaterial EMISSIVE_MAT = RENDERER.materialFinder()
            .emissive(true)
            .ambientOcclusion(TriState.FALSE)
            .disableDiffuse(true)
            .find();

    @Override
    public boolean transform(MutableQuadView quad) {
        TextureAtlasSprite sprite = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)).find(quad);

        if (quad instanceof IBakedQuadExtension ext && ext.dragonlib$getFaceData() != null) {
            if (ext.dragonlib$getFaceData().emissive()) {
                quad.material(EMISSIVE_MAT);
            }
        }

        return true;
    }
}

