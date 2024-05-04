package de.mrjulsen.mcdragonlib.client.util;

import java.util.BitSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer.AmbientOcclusionFace;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BERUtils {

    private static boolean aoRenderingErrorKnown = false;

    public static final ResourceLocation BLANK_TEXTURE_LOCATION;
    static {
        NativeImage img = new NativeImage(1, 1, false);
        img.setPixelRGBA(0, 0, 0xFFFFFFFF);
        BLANK_TEXTURE_LOCATION = Minecraft.getInstance().getTextureManager().register(DragonLib.MODID + "_blank_texture", new DynamicTexture(img));
    }

    public void initRenderEngine() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public void setTint(int r, int g, int b, int a) {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public void renderTexture(ResourceLocation texture, MultiBufferSource bufferSource, BlockEntity blockEntity, PoseStack poseStack, boolean ao, float x, float y, float z, float w, float h, float u0, float v0, float u1, float v1, Direction facing, int tint, int light) {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.text(texture));
        short[] color = ColorUtils.decodeARGB(tint);
        addQuadSide(blockEntity, blockEntity.getBlockState(), facing, vertexconsumer, poseStack, ao,
            x, y, z,
            x + w, y + h, z,
            u0, v0,
            u1, v1,
            (float)color[1] / 255.0F, (float)color[2] / 255.0F, (float)color[3] / 255.0F, (float)color[0] / 255.0F, 
            light
        );        
    }

    public static void addVert(VertexConsumer builder, PoseStack pPoseStack, float x, float y, float z, float u, float v, float r, float g, float b, float a, int lu, int lv) {
        builder.vertex(pPoseStack.last().pose(), x, y, z).color(r, g, b, a).uv(u, v).uv2(lu, lv).overlayCoords(OverlayTexture.NO_OVERLAY).normal(pPoseStack.last().normal(), 0, 0, 1).endVertex();
    }

    private static void renderWithoutAO(VertexConsumer builder, PoseStack pPoseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        addVert(builder, pPoseStack, x0, y0, z0, u0, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, pPoseStack, x0, y1, z0, u0, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, pPoseStack, x1, y1, z1, u1, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, pPoseStack, x1, y0, z1, u1, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
    }

    @SuppressWarnings("resource")
    private static void renderWithAO(BlockEntity be, BlockState state, Direction direction, VertexConsumer builder, PoseStack pPoseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace ao = new AmbientOcclusionFace();
        BlockAndTintGetter batg = Minecraft.getInstance().level;
        ao.calculate(batg, state, be.getBlockPos(), direction, afloat, bitset, true);
        
        addVert(builder, pPoseStack, x0, y0, z0, u0, v0, r * ao.brightness[0], g * ao.brightness[0], b * ao.brightness[0], a, ao.lightmap[0] & 0xFFFF, (ao.lightmap[0] >> 16) & 0xFFFF);
        addVert(builder, pPoseStack, x0, y1, z0, u0, v1, r * ao.brightness[1], g * ao.brightness[1], b * ao.brightness[1], a, ao.lightmap[1] & 0xFFFF, (ao.lightmap[1] >> 16) & 0xFFFF);
        addVert(builder, pPoseStack, x1, y1, z1, u1, v1, r * ao.brightness[2], g * ao.brightness[2], b * ao.brightness[2], a, ao.lightmap[2] & 0xFFFF, (ao.lightmap[2] >> 16) & 0xFFFF);
        addVert(builder, pPoseStack, x1, y0, z1, u1, v0, r * ao.brightness[3], g * ao.brightness[3], b * ao.brightness[3], a, ao.lightmap[3] & 0xFFFF, (ao.lightmap[3] >> 16) & 0xFFFF);
    }

    @SuppressWarnings("resources")
    public static void addQuadSide(BlockEntity be, BlockState state, Direction direction, VertexConsumer builder, PoseStack pPoseStack, boolean ao, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        if (!ao || !Minecraft.useAmbientOcclusion() || be.getLevel() == null || be.getBlockPos() == null) {
            try {
                renderWithoutAO(builder, pPoseStack, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
            } catch (Exception e2) {
                DragonLib.LOGGER.error("Error while rendering without AO.", e2);
            }
        } else {
            try {
                renderWithAO(be, state, direction, builder, pPoseStack, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
                aoRenderingErrorKnown = false;
            } catch (Exception e) {
                if (!aoRenderingErrorKnown) {
                    DragonLib.LOGGER.error("Error while rendering with AO.", e);
                }
                aoRenderingErrorKnown = true;

                try {
                    renderWithoutAO(builder, pPoseStack, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
                } catch (Exception e2) {
                    DragonLib.LOGGER.error("Error while rendering without AO.", e2);
                }
            }
        }
    }

    public void fillColor(MultiBufferSource pBufferSource, BlockEntity blockEntity, int color, PoseStack poseStack, float x, float y, float z, float w, float h, Direction facing, int light) {
        renderTexture(BLANK_TEXTURE_LOCATION, pBufferSource, blockEntity, poseStack, false, x, y, z, w, h, 0, 0, 1, 1, facing, color, light);
    }
}
