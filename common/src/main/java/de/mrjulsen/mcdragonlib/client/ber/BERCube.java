package de.mrjulsen.mcdragonlib.client.ber;

import java.util.function.Predicate;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.client.util.BERUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class BERCube {

    private float width;
    private float height;
    private float depth;
    private BERQuad[] quads = new BERQuad[6];

    public BERCube(float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public static BERCube fullCube(ResourceLocation texture, float width, float height, float depth) {
        return cube(texture, width, height, depth, dir -> true);
    }

    public static BERCube cube(ResourceLocation texture, float width, float height, float depth, Predicate<Direction> directionCheck) {
        BERCube cube = new BERCube(width, height, depth);
        for (Direction dir : Direction.values()) {
            if (directionCheck.test(dir)) {
                cube.setQuad(texture, dir, 0, 0, 1, 1);
            }
        }
        return cube;
    }

    public BERQuad setQuad(ResourceLocation texture, Direction facing, float u0, float v0, float u1, float v1) {
        float w = 0, h = 0, z = 0;
        Quaternion rot = Vector3f.YP.rotationDegrees(0);
        switch (facing) {
            case EAST:
                w = depth;
                h = height;
                z = width;
                rot = Vector3f.YP.rotationDegrees(90);
                break;
            case WEST:
                w = depth;
                h = height;
                z = width;
                rot = Vector3f.YP.rotationDegrees(270);
                break;
            case UP:
                w = width;
                h = depth;
                z = height;
                rot = Vector3f.XP.rotationDegrees(90);
                break;
            case DOWN:
                w = width;
                h = depth;
                z = height;
                rot = Vector3f.XN.rotationDegrees(90);
                break;
            case SOUTH:
                w = width;
                h = height;
                z = depth;
                rot = Vector3f.YP.rotationDegrees(180);
                break;
            default:
            case NORTH:
                w = width;
                h = height;
                z = depth;
                break;
        }
        BERQuad quad = new BERQuad(texture, w, h, u0, v0, u1, v1, facing);
        quad.setRotation(rot);
        quad.setTranslate(new Vector3f(-w / 2f, -h / 2f, -z / 2f));
        quads[facing.get3DDataValue()] = quad;
        return quad;
    }

    public void setAmbienOcclusion(boolean b) {
        for (BERQuad quad : quads) {
            if (quad == null) continue;
            quad.setAmbientOcclusion(b);
        }
    }

    public void setLight(int light) {
        for (BERQuad quad : quads) {
            if (quad == null) continue;
            quad.setLight(light);
        }
    }

    public void setTint(int tint) {
        for (BERQuad quad : quads) {
            if (quad == null) continue;
            quad.setTint(tint);
        }
    }

    public void addQuadPx(ResourceLocation texture, float width, float height, int u0, int v0, int u1, int v1, int textureWidth, int textureHeight, Direction facing) {
        quads[facing.get3DDataValue()] = new BERQuad(texture, BERUtils.bpx(width), BERUtils.bpx(width), BERUtils.px(u0, textureWidth), BERUtils.px(v0, textureHeight), BERUtils.px(u1, textureWidth), BERUtils.px(v1, textureHeight), facing);
    }

    public BERQuad getQuadFor(Direction facing) {
        return quads[facing.get3DDataValue()];
    }
    
    public void render(BERGraphics<?> graphics) {
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(width / 2f, height / 2f, depth / 2f);
        for (BERQuad quad : quads) {            
            if (quad == null) {
                continue;
            }
            quad.render(graphics);
        }
        graphics.poseStack().popPose();
    }
}
