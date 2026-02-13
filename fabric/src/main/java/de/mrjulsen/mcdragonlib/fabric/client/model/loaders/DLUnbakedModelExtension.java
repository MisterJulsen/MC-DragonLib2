package de.mrjulsen.mcdragonlib.fabric.client.model.loaders;

import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceKey;
import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

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
    public BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, boolean isGui3d) {
        BakedModel vanilla = parent.bake(baker, parent, spriteGetter, modelState, isGui3d);
        return new DLBakedModelExtension(vanilla, faceData);
    }
}
