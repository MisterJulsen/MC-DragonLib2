package de.mrjulsen.mcdragonlib.forge.client.model.loaders;

import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Map;
import java.util.function.Function;

public class DLUnbakedModelExtension implements IUnbakedGeometry<DLUnbakedModelExtension> {

    private final BlockModel parent;
    private final Map<DLFaceKey, DLFaceData> faceData;

    public DLUnbakedModelExtension(BlockModel parent, Map<DLFaceKey, DLFaceData> data) {
        this.parent = parent;
        this.faceData = data;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel vanilla = parent.bake(baker, parent, spriteGetter, modelState, modelLocation, false);
        return new DLBakedModelExtension(vanilla, faceData);
    }
}
