package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.model.ModelResourceLocationBuilder;
import de.mrjulsen.mcdragonlib.client.model.ModelUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BasicMesh extends Mesh {

    public BasicMesh() {
        super(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }


    public static BasicMesh fromBakedModel(BlockState state, BakedModel srcModel, RandomSource random) {
        BasicMesh mesh = new BasicMesh();
        Direction[] directions = new Direction[Direction.values().length + 1];
        System.arraycopy(Direction.values(), 0, directions, 0, Direction.values().length);
        for (Direction side : directions) {
            for (BakedQuad quad : srcModel.getQuads(state, side, random)) {
                mesh.addFace(new Face(quad, side));
            }
        }
        mesh.cleanUp(0.0001f, true, true, true);
        return mesh;
    }

    public static BasicMesh fromBlock(BlockState state, RandomSource random) {
        BakedModel srcModel = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        return fromBakedModel(state, srcModel, random);
    }

    public static BasicMesh fromLocation(ModelResourceLocation modelLocation, RandomSource random) {
        return fromBakedModel(null, ModelUtils.getModel(modelLocation), random);
    }

    public void combine(boolean merge, Mesh... meshes) {
        for (Mesh mesh : meshes) {
            if (mesh == this) {
                throw new IllegalArgumentException("Cannot combine Mesh with itself.");
            }
            for (Face face : mesh.getFaces()) {
                addFace(face);
            }
        }

        if (merge) {
            cleanUp(0.0001F, true, true, true);
        }
    }

    public void addFace(Face face) {
        faces.add(face);
        edges.addAll(face.getEdges());
        for (FaceVertex wrapper : face.getCorners()) {
            vertices.add(wrapper.getVertex());
        }
    }

    public void removeFace(Face face) {
        Map<Vertex, Vertex> replacements = new HashMap<>(4);
        faces.remove(face);
        for (FaceVertex wrapper : face.getCorners()) {
            Vertex current = wrapper.getVertex();
            Vertex copy = current.copy();
            wrapper.updateVertex(copy);
            replacements.put(current, copy);
        }        
        face.updateEdges(x -> x.copy(y -> replaceFunc(replacements, y)));
    }

    public void swapTextures(ResourceLocation current, ResourceLocation newLocation) {
        for (Face face : faces) {
            if (face.getTextureLocation() == null) {
                continue;
            }
            ResourceLocation loc = face.getTextureLocation();
            if (!loc.equals(current)) {
                continue;
            }

            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(newLocation);
            face.setTexture(sprite);
        }
    }

}
