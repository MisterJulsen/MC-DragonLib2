package de.mrjulsen.mcdragonlib.client.util;

import java.util.BitSet;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer.AmbientOcclusionFace;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;

public final class RenderUtils {

    private static boolean aoRenderingErrorKnown = false;

    public static final ResourceLocation BLANK_TEXTURE_LOCATION;
    static {
        NativeImage img = new NativeImage(1, 1, false);
        img.setPixelRGBA(0, 0, 0xFFFFFFFF);
        BLANK_TEXTURE_LOCATION = Minecraft.getInstance().getTextureManager().register(DragonLib.MODID + "_blank_texture", new DynamicTexture(img));
    }

    public static void initRenderEngine() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void setTint(DLColor color) {
        GuiUtils.setTint(color);
    }

    public static void resetTint() {
        GuiUtils.resetTint();
    }

    public static void addVert(VertexConsumer builder, DLGraphics graphics, float x, float y, float z, float u, float v, float r, float g, float b, float a, int lu, int lv) {
        builder.addVertex(graphics.poseStack().last().pose(), x, y, z).setColor(r, g, b, a).setUv(u, v).setUv2(lu, lv).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(graphics.poseStack().last(), 0, 0, 1);
    }

    private static void renderWithoutAO(VertexConsumer builder, DLGraphics graphics, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        addVert(builder, graphics, x0, y0, z0, u0, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, graphics, x0, y1, z0, u0, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, graphics, x1, y1, z1, u1, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        addVert(builder, graphics, x1, y0, z1, u1, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
    }

    private static void renderWithAO(Direction direction, VertexConsumer builder, BERGraphics<?> graphics, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {
        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace ao = new AmbientOcclusionFace();
        BlockAndTintGetter batg = Minecraft.getInstance().level;
        ao.calculate(batg, graphics.blockEntity().getBlockState(), graphics.blockEntity().getBlockPos(), direction, afloat, bitset, true);
        
        addVert(builder, graphics, x0, y0, z0, u0, v0, r * ao.brightness[0], g * ao.brightness[0], b * ao.brightness[0], a, ao.lightmap[0] & 0xFFFF, (ao.lightmap[0] >> 16) & 0xFFFF);
        addVert(builder, graphics, x0, y1, z0, u0, v1, r * ao.brightness[1], g * ao.brightness[1], b * ao.brightness[1], a, ao.lightmap[1] & 0xFFFF, (ao.lightmap[1] >> 16) & 0xFFFF);
        addVert(builder, graphics, x1, y1, z1, u1, v1, r * ao.brightness[2], g * ao.brightness[2], b * ao.brightness[2], a, ao.lightmap[2] & 0xFFFF, (ao.lightmap[2] >> 16) & 0xFFFF);
        addVert(builder, graphics, x1, y0, z1, u1, v0, r * ao.brightness[3], g * ao.brightness[3], b * ao.brightness[3], a, ao.lightmap[3] & 0xFFFF, (ao.lightmap[3] >> 16) & 0xFFFF);
    }
    
    @SuppressWarnings("resources")
    public static void addQuadSide(Direction direction, VertexConsumer builder, DLGraphics graphics, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight, boolean ambientOcclusion) {
        if (!ambientOcclusion || !Minecraft.useAmbientOcclusion() || !(graphics instanceof BERGraphics<?> berGraphics) || berGraphics.blockEntity().getLevel() == null || berGraphics.blockEntity().getBlockPos() == null) {
            try {
                renderWithoutAO(builder, graphics, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
            } catch (Exception e2) {
                DragonLib.LOGGER.error("Error while rendering without AO.", e2);
            }
        } else {
            try {
                renderWithAO(direction, builder, berGraphics, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, packedLight);
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
    
    public static void renderTexture(ResourceLocation texture, DLGraphics graphics, Vector3f pos, float w, float h, float u, float v, float uW, float vH, Direction facing, DLColor tint, int light, boolean ambiebtOcclusion) {
        graphics.multiBufferSource().getBuffer(RenderType.text(texture));
        VertexConsumer vertexconsumer = graphics.vertexConsumer(texture);
        addQuadSide(facing, vertexconsumer, graphics,
            pos.x(), pos.y(), pos.z(),
            pos.x() + w, pos.y() + h, pos.z(),
            u, v,
            u + uW, v + vH,
            tint.getRedF(), tint.getGreenF(), tint.getBlueF(), tint.getAlphaF(), 
            light,
            ambiebtOcclusion
        );
    }

    public static void renderTexture(ResourceLocation texture, DLGraphics graphics, Vector3f pos, float w, float h, float u, float v, float uW, float vH, Direction facing, DLColor tint, boolean ambientOcclusion) {
        renderTexture(texture, graphics, pos, w, h, u, v, uW, vH, facing, tint, graphics.packedLight(), ambientOcclusion);        
    }

    public static void renderTexture(ResourceLocation texture, DLGraphics graphics, Vector3f pos, float w, float h, Direction facing, boolean ambientOcclusion) {
        renderTexture(texture, graphics, pos, w, h, 0, 0, 1, 1, facing, DLColor.WHITE, graphics.packedLight(), ambientOcclusion);        
    }    

    // RenderStateShard$TextureStateShard for textureId
    public static void renderTexture(DLTexture texture, DLGraphics graphics, Vector3f pos, float w, float h, float u, float v, float uW, float vH, Direction facing, DLColor tint, int light, boolean ambientOcclusion) {
        if (texture.usesTextureId() || texture.getTexture().isEmpty()) {
            throw new IllegalArgumentException("TextureIds are not supported in BlockEntityRenderers.");
        }
        renderTexture(
            texture.getTexture().get(),
            graphics,
            pos, w, h,
            (float)MathUtils.proportion(u, texture.width()),
            (float)MathUtils.proportion(v, texture.height()),
            (float)MathUtils.proportion(uW, texture.width()),
            (float)MathUtils.proportion(vH, texture.height()),
            facing,
            tint,
            light,
            ambientOcclusion
        );        
    }

    public static void renderTexture(DLTexture texture, DLGraphics graphics, Vector3f pos, float w, float h, float u, float v, float uW, float vH, Direction facing, DLColor tint, boolean ambientOcclusion) {
        renderTexture(texture, graphics, pos, w, h, u, v, uW, vH, facing, tint, graphics.packedLight(), ambientOcclusion);
    }

    public static void renderTexture(DLTexture texture, DLGraphics graphics, Vector3f pos, float w, float h, Direction facing, boolean ambientOcclusion) {
        renderTexture(texture, graphics, pos, w, h, 0, 0, texture.width(), texture.height(), facing, DLColor.WHITE, ambientOcclusion);
    }

    public static void fillColor(DLGraphics graphics, Vector3f pos, float w, float h, DLColor color, Direction facing, int light, boolean ambientOcclusion) {
        renderTexture(BLANK_TEXTURE_LOCATION, graphics, pos, w, h, 0, 0, 1, 1, facing, color, light, ambientOcclusion);
    }
    
    public static void fillColor(DLGraphics graphics, Vector3f pos, float w, float h, DLColor color, Direction facing) {
        fillColor(graphics, pos, w, h, color, facing, graphics.packedLight(), false);
    }

    public static void drawString(DLGraphics graphics, Font font, float x, float y, Component text, DLColor color, ETextAlignment alignment, boolean dropShadow, boolean transparent, DLColor backgroundColor, int packedLight) {
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
        font.drawInBatch(text, dx, y, color.getAsARGB(), dropShadow, graphics.poseStack().last().pose(), graphics.multiBufferSource(), transparent ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL, backgroundColor.getAsARGB(), packedLight);
    }

    public static void drawString(DLGraphics graphics, Font font, float x, float y, Component text, DLColor color, ETextAlignment alignment, boolean dropShadow, int packedLight) {        
        drawString(graphics, font, x, y, text, color, alignment, dropShadow, false, DLColor.TRANSPARENT, packedLight);
    }

    public static void drawString(DLGraphics graphics, Font font, float x, float y, Component text, DLColor color, ETextAlignment alignment, boolean dropShadow) {
        drawString(graphics, font, x, y, text, color, alignment, dropShadow, graphics.packedLight());
    }

    public static void drawString(DLGraphics graphics, Font font, float x, float y, String text, DLColor color, ETextAlignment alignment, boolean dropShadow, boolean transparent, DLColor backgroundColor, int packedLight) {        
        drawString(graphics, font, x, y, TextUtils.text(text), color, alignment, dropShadow, transparent, backgroundColor, packedLight);
    }

    public static void drawString(DLGraphics graphics, Font font, float x, float y, String text, DLColor color, ETextAlignment alignment, boolean dropShadow, int packedLight) {        
        drawString(graphics, font, x, y, text, color, alignment, dropShadow, false, DLColor.TRANSPARENT, packedLight);
    }

    public static void drawString(DLGraphics graphics, Font font, float x, float y, String text, DLColor color, ETextAlignment alignment, boolean dropShadow) {
        drawString(graphics, font, x, y, text, color, alignment, dropShadow, graphics.packedLight());
    }
    
    

    public static void drawDebugLineGradient(PoseStack poseStack, VertexConsumer consumer, Vector3f from, Vector3f to, DLColor colorA, DLColor colorB) {
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        float dx = to.x() - from.x();
        float dy = to.y() - from.y();
        float dz = to.z() - from.z();
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        consumer.addVertex(matrix4f, (float) from.x(), (float) from.y(), (float) from.z()).setColor(colorA.getRedF(), colorA.getGreenF(), colorA.getBlueF(), colorA.getAlphaF()).setNormal(lastPose, dx, dy, dz);
        consumer.addVertex(matrix4f, (float) to.x(), (float) to.y(), (float) to.z()).setColor(colorB.getRedF(), colorB.getGreenF(), colorB.getBlueF(), colorB.getAlphaF()).setNormal(lastPose, dx, dy, dz);
    }

    public static void drawDebugLine(PoseStack poseStack, VertexConsumer consumer, Vector3f from, Vector3f to, DLColor color) {
        drawDebugLineGradient(poseStack, consumer, from, to, color, color);
    }
    
    public static void drawDebugLineGradient(DLGraphics graphics, Vector3f from, Vector3f to, DLColor colorA, DLColor colorB) {
        drawDebugLine(graphics.poseStack(), graphics.multiBufferSource().getBuffer(RenderType.lines()), from, to, colorB);
    }

    public static void drawDebugLine(DLGraphics graphics, Vector3f from, Vector3f to, DLColor color) {
        drawDebugLineGradient(graphics, from, to, color, color);
    }

    protected static void renderNameTag(PoseStack poseStack, MultiBufferSource buffer, Vector3f pos, float yOffset, List<Component> text, int packedLight) {
        if (Minecraft.getInstance().getCameraEntity().position().toVector3f().distance(pos) > 64) {
            return;
        }
        final float lineHeight = Minecraft.getInstance().font.lineHeight * 1.5f;
        poseStack.pushPose();
        poseStack.translate(pos.x(), pos.y() + yOffset, pos.z());
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.0125F, -0.0125F, 0.0125F);
        Matrix4f matrix4f = poseStack.last().pose();

        for (int k = 0; k < text.size(); k++) {
            float y = lineHeight * k;
            Component txt = text.get(k);

            float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int backgroundColor = (int)(opacity * 255.0F) << 24;
            Font font = Minecraft.getInstance().font;
            float x = (float)(-font.width(txt) / 2);

            font.drawInBatch(txt, x, -(lineHeight * text.size()) + y, 553648127, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, backgroundColor, packedLight);
            font.drawInBatch(txt, x, -(lineHeight * text.size()) + y, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        }

        poseStack.popPose();
    }

    protected static void renderNameTag(DLGraphics graphics, Vector3f pos, float yOffset, List<Component> text, int packedLight) {
        renderNameTag(graphics.poseStack(), graphics.multiBufferSource(), pos, yOffset, text, packedLight);
    }
}
