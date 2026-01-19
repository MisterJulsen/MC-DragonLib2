package de.mrjulsen.mcdragonlib.client.model.mesh.fabric;

import de.mrjulsen.mcdragonlib.client.model.mesh.Face;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class FaceImpl {
    public static BakedQuad buildQuad(Face face, int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade) {
        return new BakedQuad(
                vertices,
                tintIndex,
                direction,
                sprite,
                !face.isEmissive() && shade
        );
    }
}