package de.mrjulsen.mcdragonlib.client.util;

import java.util.BitSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.ColorUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class BERUtils {

    private static boolean aoRenderingErrorKnown = false;

    public static final ResourceLocation BLANK_TEXTURE_LOCATION;
    static {
        NativeImage img = new NativeImage(1, 1, false);
        img.setPixelRGBA(0, 0, 0xFFFFFFFF);
        BLANK_TEXTURE_LOCATION = Minecraft.getInstance().getTextureManager().register(DragonLib.MODID + "_blank_texture", new DynamicTexture(img));
    }

    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> renderProvider) {
        BlockEntityRendererRegistry.register(type, renderProvider);
    }

    public static void initRenderEngine() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Converts the pixel input to a float value between 0 and 1.
     * @param pixelValue The pixel value
     * @param maxPixels Max pixel size
     */
    public static float px(float pixelValue, int maxPixels) {
        return 1.0F / maxPixels * pixelValue;
    }

    /**
     * Converts the input to a float value between 0 and 1 per 16 block pixels.
     * @param pixelValue The block pixel value
     */
    public static float bpx(float pixelValue) {
        return px(pixelValue, 16);
    }

    public static void setTint(int r, int g, int b, int a) {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void renderTexture(ResourceLocation texture, BlockEntity blockEntity, BERGraphics<?> graphics, boolean ao, float x, float y, float z, float w, float h, float u0, float v0, float u1, float v1, Direction facing, int tint, int light) {
        VertexConsumer vertexconsumer = graphics.getVertexConsumer(texture);
        short[] color = ColorUtils.decodeARGB(tint);
        addQuadSide(blockEntity, blockEntity.getBlockState(), facing, vertexconsumer, graphics, ao,
            x, y, z,
            x + w, y + h, z,
            u0, v0,
            u1, v1,
            (float)color[1] / 255.0F, (float)color[2] / 255.0F, (float)color[3] / 255.0F, (float)color[0] / 255.0F, 
            light
        );        
    }

    public static void renderTexture(ResourceLocation texture, BlockEntity blockEntity, BERGraphics<?> graphics, boolean ao, float x, float y, float z, float w, float h, float u0, float v0, float u1, float v1, Direction facing, int tint) {
        renderTexture(texture, blockEntity, graphics, ao, x, y, z, w, h, u0, v0, u1, v1, facing, tint, graphics.packedLight());        
    }

    public static void addVert(VertexConsumer builder, BERGraphics<?> graphics, float x, float y, float z, float u, float v, float r, float g, float b, float a, int lu, int lv) {
        builder.vertex(graphics.poseStack().last().pose(), x, y, z).color(r, g, b, a).uv(u, v).uv2(lu, lv).overlayCoords(OverlayTexture.NO_OVERLAY).normal(graphics.poseStack().last().normal(), 0, 0, 1).endVertex();
    }

    private static void renderWithoutAO(VertexConsumer builder, BERGraphics<?> graphics, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        addVert(builder, graphics, x1, y1, z0, u0, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, graphics, x1, y0, z0, u0, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, graphics, x0, y0, z1, u1, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, graphics, x0, y1, z1, u1, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
    }

    @SuppressWarnings("resource")
    private static void renderWithAO(BlockEntity be, BlockState state, Direction direction, VertexConsumer builder, BERGraphics<?> graphics, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace ao = Minecraft.getInstance().getBlockRenderer().getModelRenderer().new AmbientOcclusionFace();
        BlockAndTintGetter batg = Minecraft.getInstance().level;
        ao.calculate(batg, state, be.getBlockPos(), direction, afloat, bitset, true);
        
        addVert(builder, graphics, x1, y1, z0, u0, v0, r * ao.brightness[0], g * ao.brightness[0], b * ao.brightness[0], a, ao.lightmap[0] & 0xFFFF, (ao.lightmap[0] >> 16) & 0xFFFF);
        addVert(builder, graphics, x1, y0, z0, u0, v1, r * ao.brightness[1], g * ao.brightness[1], b * ao.brightness[1], a, ao.lightmap[1] & 0xFFFF, (ao.lightmap[1] >> 16) & 0xFFFF);
        addVert(builder, graphics, x0, y0, z1, u1, v1, r * ao.brightness[2], g * ao.brightness[2], b * ao.brightness[2], a, ao.lightmap[2] & 0xFFFF, (ao.lightmap[2] >> 16) & 0xFFFF);
        addVert(builder, graphics, x0, y1, z1, u1, v0, r * ao.brightness[3], g * ao.brightness[3], b * ao.brightness[3], a, ao.lightmap[3] & 0xFFFF, (ao.lightmap[3] >> 16) & 0xFFFF);
    }

    @SuppressWarnings("resources")
    public static void addQuadSide(BlockEntity be, BlockState state, Direction direction, VertexConsumer builder, BERGraphics<?> graphics, boolean ao, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        if (!ao || !Minecraft.useAmbientOcclusion() || be.getLevel() == null || be.getBlockPos() == null) {
            try {
                renderWithoutAO(builder, graphics, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
            } catch (Exception e2) {
                DragonLib.LOGGER.error("Error while rendering without AO.", e2);
            }
        } else {
            try {
                renderWithAO(be, state, direction, builder, graphics, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
                aoRenderingErrorKnown = false;
            } catch (Exception e) {
                if (!aoRenderingErrorKnown) {
                    DragonLib.LOGGER.error("Error while rendering with AO.", e);
                }
                aoRenderingErrorKnown = true;

                try {
                    renderWithoutAO(builder, graphics, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
                } catch (Exception e2) {
                    DragonLib.LOGGER.error("Error while rendering without AO.", e2);
                }
            }
        }
    }

    public static void fillColor(BlockEntity blockEntity, BERGraphics<?> graphics, float x, float y, float z, float w, float h, int color, Direction facing, int light) {
        renderTexture(BLANK_TEXTURE_LOCATION, blockEntity, graphics, false, x, y, z, w, h, 0, 0, 1, 1, facing, color, light);
    }

    public static void fillColor(BlockEntity blockEntity, BERGraphics<?> graphics, float x, float y, float z, float w, float h, int color, Direction facing) {
        fillColor(blockEntity, graphics, x, y, z, w, h, color, facing, graphics.packedLight());
    }

    public static void drawString(BERGraphics<?> graphics, Font font, float x, float y, Component text, int color, EAlignment alignment, boolean drawShadow, boolean transparent, int backgroundColor, int packedLight) {
        float dx = x;
        switch (alignment) {
            case RIGHT:
                dx = x - font.width(text);
                break;
            case CENTER:
                dx = x - font.width(text) / 2;
                break;
            default:
                break;
        }        
        font.drawInBatch(text, dx, y, color, drawShadow, graphics.poseStack().last().pose(), graphics.multiBufferSource(), transparent, backgroundColor, packedLight);
    }

    public static void drawString(BERGraphics<?> graphics, Font font, float x, float y, Component text, int color, EAlignment alignment, boolean drawShadow, int packedLight) {        
        drawString(graphics, font, x, y, text, color, alignment, drawShadow, false, 0, packedLight);
    }

    public static void drawString(BERGraphics<?> graphics, Font font, float x, float y, Component text, int color, EAlignment alignment, boolean drawShadow) {
        drawString(graphics, font, x, y, text, color, alignment, drawShadow, graphics.packedLight());
    }

    public static void drawString(BERGraphics<?> graphics, Font font, float x, float y, String text, int color, EAlignment alignment, boolean drawShadow, boolean transparent, int backgroundColor, int packedLight) {        
        drawString(graphics, font, x, y, TextUtils.text(text), color, alignment, drawShadow, transparent, backgroundColor, packedLight);
    }

    public static void drawString(BERGraphics<?> graphics, Font font, float x, float y, String text, int color, EAlignment alignment, boolean drawShadow, int packedLight) {        
        drawString(graphics, font, x, y, text, color, alignment, drawShadow, false, 0, packedLight);
    }

    public static void drawString(BERGraphics<?> graphics, Font font, float x, float y, String text, int color, EAlignment alignment, boolean drawShadow) {
        drawString(graphics, font, x, y, text, color, alignment, drawShadow, graphics.packedLight());
    }
}
