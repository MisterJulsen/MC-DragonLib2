package de.mrjulsen.mcdragonlib.client.ber;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.client.util.BERUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class BERQuad {

    private ResourceLocation texture;
    private float width;
    private float height;
    private float u0;
    private float v0;
    private float u1;
    private float v1;
    private Direction facing;

    private boolean ambientOcclusion;
    private boolean useCustomLight;
    private int light;
    private int tint = 0xFFFFFFFF;

    private Vector3f translate = null;
    private Quaternion rotation = null;
    

    public BERQuad(ResourceLocation texture, float width, float height, float u0, float v0, float u1, float v1, Direction facing) {
        this.width = width;
        this.height = height;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.facing = facing;
        this.texture = texture;
    }

    public static BERQuad createPx(ResourceLocation texture, int width, int height, int u0, int v0, int u1, int v1, int textureWidth, int textureHeight, Direction facing) {
        return new BERQuad(texture, BERUtils.bpx(width), BERUtils.bpx(width), BERUtils.px(u0, textureWidth), BERUtils.px(v0, textureHeight), BERUtils.px(u1, textureWidth), BERUtils.px(v1, textureHeight), facing);
    }

    public void setAmbientOcclusion(boolean b) {
        this.ambientOcclusion = b;
    }

    public boolean isUsingAmbientOcclusion() {
        return ambientOcclusion;
    }

    public void setLight(int light) {
        this.light = light;
        this.useCustomLight = true;
    }

    public void defaultLight() {
        useCustomLight = false;
    }

    public boolean isUsingCustomLight() {
        return useCustomLight;
    }

    public void setTint(int tint) {
        this.tint = tint;
    }
    
    public Vector3f getTranslate() {
        return translate;
    }

    public void setTranslate(Vector3f translate) {
        this.translate = translate;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public void render(BERGraphics<?> graphics) {
        render(graphics, 0, 0, 0);
    }
    
    public void renderPx(BERGraphics<?> graphics) {
        renderPx(graphics, 0, 0, 0);
    }
    
    public void render(BERGraphics<?> graphics, float x, float y, float z) {
        graphics.poseStack().pushPose();
        if (rotation != null) {
            graphics.poseStack().mulPose(rotation);
        }
        graphics.poseStack().pushPose();
        if (translate != null) {
            graphics.poseStack().translate(translate.x(), translate.y(), translate.z());
        }
        BERUtils.renderTexture(texture, graphics, ambientOcclusion, x, y, z, width, height, u0, v0, u1, v1, facing, tint, useCustomLight ? light : graphics.packedLight());
        graphics.poseStack().popPose();
        graphics.poseStack().popPose();
    }
    
    public void renderPx(BERGraphics<?> graphics, float x, float y, float z) {
        render(graphics, BERUtils.bpx(x), BERUtils.bpx(y), BERUtils.bpx(z));
    }
}
