package de.mrjulsen.mcdragonlib.gui.v2.render;

import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.gui.v2.Transform;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Drawing helpers for component rendering: rectangles, gradients, borders, shadows, text, sprites
 * and items.
 * <p>Unless stated otherwise, coordinates are the local coordinates of the component being drawn and
 * are snapped to whole pixels, and a null or fully transparent color means nothing is drawn.</p>
 */
public final class GuiUtils {
    private GuiUtils() {}

    private static final double ITEM_Z_COMPENSATION = 150.0D;
    private static final double ITEM_Z_BAND = 12.0D;
    private static final double DECORATION_Z_COMPENSATION = 200.0D;
    private static final double MODEL_Z_BAND = 16.0D;
    private static final int SHADOW_CORNER_SEGMENTS = 8;

    private static final Quaternionf ENTITY_UPRIGHT = new Quaternionf().rotateZ((float)Math.PI);

    private static final DLModel BLOCK_STATE_MODEL = new DLModel() {
        @Override
        protected Mesh getMesh(ModelType type, BakedModel originalModel, BlockState state, RandomSource random, ModelContext context) {
            Mesh mesh = BasicMesh.fromBlock(state, random);
            mesh.rotate(Axis.ZP.rotationDegrees(180), new Vector3f(0.5f));
            return mesh;
        }
    };

    /**
     * Binds a texture to shader texture unit 0.
     * Only needed for custom drawing, the {@code texture(...)} methods bind on their own.
     */
    public static void setTexture(ResourceLocation location) {
        RenderSystem.setShaderTexture(0, location);
    }

    /**
     * Binds a raw OpenGL texture id to shader texture unit 0.
     */
    public static void setTexture(int textureId) {
        RenderSystem.setShaderTexture(0, textureId);
    }

    /**
     * Multiplies everything drawn afterwards with this color.
     * Stays active until {@link #resetTint()} is called and affects all GUI rendering, not just this component.
     */
    public static void setTint(DLColor color) {
        RenderSystem.setShaderColor(color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF());
    }

    /**
     * Resets the tint to opaque white.
     */
    public static void resetTint() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Runs the task with the given tint applied and resets the tint afterwards.
     */
    public static void withTint(DLColor color, Runnable task) {
        setTint(color);
        try {
            task.run();
        } catch (Exception e) {
            DragonLib.LOGGER.error(e.getMessage(), e);
        }
        resetTint();
    }

    /**
     * Fills a rectangle with a solid color. Coordinates are snapped to whole pixels.
     * Does nothing for an empty size or a {@code null} or fully transparent color.
     */
    public static void fill(DLGuiGraphics g, double x, double y, double width, double height, DLColor color) {
        if (width <= 0 || height <= 0 || color == null || color.getAlpha() == 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.graphics().fill(
            (int) Transform.snap(x),
            (int) Transform.snap(y),
            (int) Transform.snap(x + width),
            (int) Transform.snap(y + height),
            color.getAsARGB()
        );
        RenderSystem.disableBlend();
    }

    /**
     * Fills the given rectangle with a solid color.
     */
    public static void fill(DLGuiGraphics g, Rectangle area, DLColor color) {
        fill(g, area.x(), area.y(), area.width(), area.height(), color);
    }

    /**
     * Fills a rectangle with one color per corner, interpolated in between.
     * Does nothing for an empty size or if any color is {@code null}.
     */
    public static void fillGradient(DLGuiGraphics g, double x, double y, double width, double height, DLColor topLeft, DLColor topRight, DLColor bottomRight, DLColor bottomLeft) {
        if (width <= 0 || height <= 0 || topLeft == null || topRight == null || bottomRight == null || bottomLeft == null) {
            return;
        }
        float x1 = (float) Transform.snap(x);
        float y1 = (float) Transform.snap(y);
        float x2 = (float) Transform.snap(x + width);
        float y2 = (float) Transform.snap(y + height);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f pose = g.pose().last().pose();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(pose, x2, y1, 0).color(topRight.getAsARGB()).endVertex();
        buffer.vertex(pose, x1, y1, 0).color(topLeft.getAsARGB()).endVertex();
        buffer.vertex(pose, x1, y2, 0).color(bottomLeft.getAsARGB()).endVertex();
        buffer.vertex(pose, x2, y2, 0).color(bottomRight.getAsARGB()).endVertex();
        t.end();

        RenderSystem.disableBlend();
    }

    /**
     * Fills a rectangle with a two-color gradient running along the given direction.
     */
    public static void fillGradient(DLGuiGraphics g, double x, double y, double width, double height, DLColor from, DLColor to, GradientDirection direction) {
        if (from == null || to == null || direction == null) {
            return;
        }
        fillGradient(g, x, y, width, height,
            DLColor.blend(from, to, direction.topLeft()),
            DLColor.blend(from, to, direction.topRight()),
            DLColor.blend(from, to, direction.bottomRight()),
            DLColor.blend(from, to, direction.bottomLeft())
        );
    }

    /**
     * Fills the given rectangle with a two-color gradient.
     */
    public static void fillGradient(DLGuiGraphics g, Rectangle area, DLColor from, DLColor to, GradientDirection direction) {
        fillGradient(g, area.x(), area.y(), area.width(), area.height(), from, to, direction);
    }

    /**
     * Draws a soft shadow around the rectangle, fading out over {@code radius} pixels.
     */
    public static void shadow(DLGuiGraphics g, double x, double y, double width, double height, double radius, DLColor color) {
        shadow(g, x, y, width, height, radius, 0.0D, 0.0D, color, true);
    }

    /**
     * Draws a soft shadow around the given rectangle.
     */
    public static void shadow(DLGuiGraphics g, Rectangle area, double radius, DLColor color) {
        shadow(g, area.x(), area.y(), area.width(), area.height(), radius, 0.0D, 0.0D, color, true);
    }

    /**
     * Draws a soft shadow around the given rectangle, displaced by the offset.
     */
    public static void shadow(DLGuiGraphics g, Rectangle area, double radius, double offsetX, double offsetY, DLColor color) {
        shadow(g, area.x(), area.y(), area.width(), area.height(), radius, offsetX, offsetY, color, true);
    }

    /**
     * Draws a soft shadow fading out over {@code radius} pixels, displaced by the offset.
     * Does nothing for an empty size, a radius of 0 or a {@code null} / fully transparent color.
     *
     * @param fillCenter whether the rectangle itself is filled with the shadow color. Pass {@code false}
     *                   if translucent content is drawn on top, otherwise it darkens twice.
     */
    public static void shadow(DLGuiGraphics g, double x, double y, double width, double height, double radius, double offsetX, double offsetY, DLColor color, boolean fillCenter) {
        if (width <= 0 || height <= 0 || radius <= 0 || color == null || color.getAlpha() == 0) {
            return;
        }
        DLColor edge = color.withAlpha(0);
        double left = x + offsetX;
        double top = y + offsetY;
        double right = left + width;
        double bottom = top + height;

        if (fillCenter) {
            fill(g, left, top, width, height, color);
        }
        fillGradient(g, left, top - radius, width, radius, color, edge, GradientDirection.BOTTOM_TO_TOP);
        fillGradient(g, left, bottom, width, radius, color, edge, GradientDirection.TOP_TO_BOTTOM);
        fillGradient(g, left - radius, top, radius, height, color, edge, GradientDirection.RIGHT_TO_LEFT);
        fillGradient(g, right, top, radius, height, color, edge, GradientDirection.LEFT_TO_RIGHT);
        shadowCorners(g, left, top, right, bottom, radius, color, edge);
    }

    private static void shadowCorners(DLGuiGraphics g, double left, double top, double right, double bottom, double radius, DLColor inner, DLColor outer) {
        double[] centers = {
            Transform.snap(left), Transform.snap(top),
            Transform.snap(right), Transform.snap(top),
            Transform.snap(right), Transform.snap(bottom),
            Transform.snap(left), Transform.snap(bottom)
        };
        int innerArgb = inner.getAsARGB();
        int outerArgb = outer.getAsARGB();
        double step = Math.PI / 2.0D / SHADOW_CORNER_SEGMENTS;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f pose = g.pose().last().pose();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int corner = 0; corner < 4; corner++) {
            float cx = (float) centers[corner * 2];
            float cy = (float) centers[corner * 2 + 1];
            double base = Math.PI + corner * (Math.PI / 2.0D);
            for (int segment = 0; segment < SHADOW_CORNER_SEGMENTS; segment++) {
                double a0 = base + segment * step;
                double a1 = a0 + step;
                buf.vertex(pose, cx, cy, 0).color(innerArgb).endVertex();
                buf.vertex(pose, cx + (float)(Math.cos(a1) * radius), cy + (float)(Math.sin(a1) * radius), 0).color(outerArgb).endVertex();
                buf.vertex(pose, cx + (float)(Math.cos(a0) * radius), cy + (float)(Math.sin(a0) * radius), 0).color(outerArgb).endVertex();
            }
        }
        t.end();

        RenderSystem.disableBlend();
    }

    /**
     * Draws a rectangular outline inside the given bounds, growing inwards.
     * Does nothing for an empty size or a fully transparent color.
     *
     * @param color must not be {@code null}
     */
    public static void border(DLGuiGraphics g, double x, double y, double width, double height, double thickness, DLColor color) {
        if (width <= 0 || height <= 0 || thickness <= 0 || color.getAlpha() == 0) {
            return;
        }
        fill(g, x, y, width, thickness, color);
        fill(g, x, y + height - thickness, width, thickness, color);
        fill(g, x, y + thickness, thickness, height - thickness * 2, color);
        fill(g, x + width - thickness, y + thickness, thickness, height - thickness * 2, color);
    }

    /**
     * Fills a rectangle and draws a 1 pixel border on top of it.
     */
    public static void box(DLGuiGraphics g, double x, double y, double width, double height, DLColor fill, DLColor border) {
        fill(g, x, y, width, height, fill);
        border(g, x, y, width, height, 1, border);
    }


    /**
     * Draws white text with a shadow at the given position.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, String value, double x, double y) {
        return text(g, value, x, y, DLColor.WHITE, true);
    }

    /**
     * Draws white text with a shadow at the given position.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, Component value, double x, double y) {
        return text(g, value, x, y, DLColor.WHITE, true);
    }

    /**
     * Draws white text with a shadow at the given position.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, FormattedText value, double x, double y) {
        return text(g, value, x, y, DLColor.WHITE, true);
    }

    /**
     * Draws white text with a shadow at the given position.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, FormattedCharSequence value, double x, double y) {
        return text(g, value, x, y, DLColor.WHITE, true);
    }

    /**
     * Draws a single line of text. Coordinates are snapped to whole pixels, {@code y} is the top of the line.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, String value, double x, double y, DLColor tint, boolean shadow) {
        return g.graphics().drawString(g.font(), value, (int)Transform.snap(x), (int)Transform.snap(y), tint.getAsARGB(), shadow);
    }

    /**
     * Draws a single line of text. Coordinates are snapped to whole pixels, {@code y} is the top of the line.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, Component value, double x, double y, DLColor tint, boolean shadow) {
        return g.graphics().drawString(g.font(), value.getVisualOrderText(), (int)Transform.snap(x), (int)Transform.snap(y), tint.getAsARGB(), shadow);
    }

    /**
     * Draws a single line of text. Coordinates are snapped to whole pixels, {@code y} is the top of the line.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, FormattedText value, double x, double y, DLColor tint, boolean shadow) {
        return g.graphics().drawString(g.font(), TextUtils.toComponent(value), (int)Transform.snap(x), (int)Transform.snap(y), tint.getAsARGB(), shadow);
    }

    /**
     * Draws a single line of text. Coordinates are snapped to whole pixels, {@code y} is the top of the line.
     *
     * @return the x coordinate right after the drawn text
     */
    public static int text(DLGuiGraphics g, FormattedCharSequence value, double x, double y, DLColor tint, boolean shadow) {
        return g.graphics().drawString(g.font(), value, (int)Transform.snap(x), (int)Transform.snap(y), tint.getAsARGB(), shadow);
    }

    /**
     * Draws a single line of text aligned within {@code maxWidth}, starting at {@code x}.
     * @apiNote The text is neither wrapped nor trimmed and can overflow the given width.
     */
    public static void text(DLGuiGraphics g, Component value, double x, double y, double maxWidth, ETextAlignment alignment, DLColor color, boolean shadow) {
        double textWidth = TextUtils.textWidth(g.font(), value);
        text(g, value, x + alignmentOffset(alignment, maxWidth, textWidth), y, color, shadow);
    }

    /**
     * Draws a single line of text aligned within {@code maxWidth}, starting at {@code x}.
     * @apiNote The text is neither wrapped nor trimmed and can overflow the given width.
     */
    public static void text(DLGuiGraphics g, FormattedText value, double x, double y, double maxWidth, ETextAlignment alignment, DLColor color, boolean shadow) {
        double textWidth = TextUtils.textWidth(g.font(), value);
        text(g, value, x + alignmentOffset(alignment, maxWidth, textWidth), y, color, shadow);
    }

    /**
     * Draws a single line of text aligned within {@code maxWidth}, starting at {@code x}.
     * @apiNote The text is neither wrapped nor trimmed and can overflow the given width.
     */
    public static void text(DLGuiGraphics g, String value, double x, double y, double maxWidth, ETextAlignment alignment, DLColor color, boolean shadow) {
        double textWidth = TextUtils.textWidth(g.font(), value);
        text(g, value, x + alignmentOffset(alignment, maxWidth, textWidth), y, color, shadow);
    }

    /**
     * Calculates the offset that aligns a content length inside an available length.
     *
     * @return the offset, negative if the content is longer than the available space
     */
    public static double alignmentOffset(ETextAlignment alignment, double available, double content) {
        return switch (alignment) {
            case CENTER -> (available - content) / 2.0D;
            case RIGHT -> available - content;
            default -> 0.0D;
        };
    }



    /**
     * Draws a sprite of a texture sheet, stretched to the given size.
     * Does nothing if the sprite is {@code null}.
     */
    public static void sprite(DLGuiGraphics g, AbstractSprite sprite, double x, double y, double width, double height) {
        if (sprite == null) {
            return;
        }
        sprite.render(g.legacy(), (int)Transform.snap(x), (int)Transform.snap(y), (int)Transform.snap(width), (int)Transform.snap(height));
    }

    /**
     * Draws a texture whose file size equals the destination size, 1:1.
     */
    public static void texture(DLGuiGraphics g, ResourceLocation texture, double x, double y, double width, double height) {
        texture(g, texture, x, y, width, height, 0, 0, width, height, (int)Transform.snap(width), (int)Transform.snap(height), TextureFillMode.STRETCH);
    }

    /**
     * Draws a texture whose file size equals the destination size.
     * With {@link TextureFillMode#TILE} the whole texture is used as the tile.
     */
    public static void texture(DLGuiGraphics g, ResourceLocation texture, double x, double y, double width, double height, TextureFillMode mode) {
        texture(g, texture, x, y, width, height, 0, 0, width, height, (int)Transform.snap(width), (int)Transform.snap(height), mode);
    }

    /**
     * Draws the top left region of a texture file, using a source region the size of the destination area.
     */
    public static void texture(DLGuiGraphics g, ResourceLocation texture, double x, double y, double width, double height, int textureWidth, int textureHeight, TextureFillMode mode) {
        texture(g, texture, x, y, width, height, 0, 0, width, height, textureWidth, textureHeight, mode);
    }

    /**
     * Draws a region of a texture file, starting at {@code u}/{@code v} and the size of the destination area.
     */
    public static void texture(DLGuiGraphics g, ResourceLocation texture, double x, double y, double width, double height, float u, float v, int textureWidth, int textureHeight, TextureFillMode mode) {
        texture(g, texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight, mode);
    }

    /**
     * Draws a region of a texture file into the destination area.
     * Does nothing if the destination area, the source region or the texture size is empty.
     *
     * @param u  left edge of the source region, in texture pixels
     * @param v  top edge of the source region, in texture pixels
     * @param uW width of the source region, and the tile width for {@link TextureFillMode#TILE}
     * @param vH height of the source region, and the tile height for {@link TextureFillMode#TILE}
     * @param textureWidth  full width of the texture file, used for the UV calculation
     * @param textureHeight full height of the texture file, used for the UV calculation
     */
    public static void texture(DLGuiGraphics g, ResourceLocation texture, double x, double y, double width, double height, float u, float v, double uW, double vH, int textureWidth, int textureHeight, TextureFillMode mode) {
        drawTexture(g, texture, 0, x, y, width, height, u, v, uW, vH, textureWidth, textureHeight, mode);
    }

    /**
     * Draws a complete {@link DLTexture} into the destination area, stretched.
     */
    public static void texture(DLGuiGraphics g, DLTexture texture, double x, double y, double width, double height) {
        texture(g, texture, x, y, width, height, TextureFillMode.STRETCH);
    }

    /**
     * Draws a complete {@link DLTexture} into the destination area.
     * With {@link TextureFillMode#TILE} the whole texture is used as the tile.
     */
    public static void texture(DLGuiGraphics g, DLTexture texture, double x, double y, double width, double height, TextureFillMode mode) {
        if (texture == null) {
            return;
        }
        texture(g, texture, x, y, width, height, 0, 0, texture.width(), texture.height(), mode);
    }

    /**
     * Draws a region of a {@link DLTexture} into the destination area, stretched.
     */
    public static void texture(DLGuiGraphics g, DLTexture texture, double x, double y, double width, double height, float u, float v, double uW, double vH) {
        texture(g, texture, x, y, width, height, u, v, uW, vH, TextureFillMode.STRETCH);
    }

    /**
     * Draws a region of a {@link DLTexture} into the destination area. The texture carries its own size,
     * and may be backed by a {@link ResourceLocation} or by a raw OpenGL texture id.
     * Does nothing if the texture is {@code null}.
     *
     * @param uW width of the source region, and the tile width for {@link TextureFillMode#TILE}
     * @param vH height of the source region, and the tile height for {@link TextureFillMode#TILE}
     */
    public static void texture(DLGuiGraphics g, DLTexture texture, double x, double y, double width, double height, float u, float v, double uW, double vH, TextureFillMode mode) {
        if (texture == null) {
            return;
        }
        drawTexture(
            g,
            texture.usesTextureId() ? null : texture.getTexture().orElse(null),
            texture.getTextureId(),
            x, y, width, height,
            u, v, uW, vH,
            texture.width(), texture.height(),
            mode
        );
    }

    private static void drawTexture(DLGuiGraphics g, @Nullable ResourceLocation location, int textureId, double x, double y, double width, double height, float u, float v, double uW, double vH, int textureWidth, int textureHeight, TextureFillMode mode) {
        if (width <= 0 || height <= 0 || uW <= 0 || vH <= 0 || textureWidth <= 0 || textureHeight <= 0) {
            return;
        }
        int px = (int)Transform.snap(x);
        int py = (int)Transform.snap(y);
        int pw = (int)Transform.snap(width);
        int ph = (int)Transform.snap(height);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (mode == TextureFillMode.TILE) {
            tiles(g, location, textureId, px, py, pw, ph, u, v, (float)uW, (float)vH, textureWidth, textureHeight);
        } else if (location != null) {
            g.graphics().blit(location, px, py, pw, ph, u, v, (int)uW, (int)vH, textureWidth, textureHeight);
        } else {
            quad(g, location, textureId, px, py, pw, ph, u, v, (float)uW, (float)vH, textureWidth, textureHeight);
        }
        RenderSystem.disableBlend();
    }

    private static void tiles(DLGuiGraphics g, @Nullable ResourceLocation location, int textureId, int x, int y, int width, int height, float u, float v, float uW, float vH, int textureWidth, int textureHeight) {
        BufferBuilder buffer = beginTextured(g, location, textureId);
        Matrix4f pose = g.pose().last().pose();
        float minU = u / textureWidth;
        float minV = v / textureHeight;
        float spanU = uW / textureWidth;
        float spanV = vH / textureHeight;

        for (float offsetY = 0; offsetY < height; offsetY += vH) {
            float tileHeight = Math.min(vH, height - offsetY);
            float y0 = y + offsetY;
            float y1 = y0 + tileHeight;
            float maxV = minV + spanV * (tileHeight / vH);
            for (float offsetX = 0; offsetX < width; offsetX += uW) {
                float tileWidth = Math.min(uW, width - offsetX);
                float x0 = x + offsetX;
                float x1 = x0 + tileWidth;
                float maxU = minU + spanU * (tileWidth / uW);
                buffer.vertex(pose, x0, y1, 0).uv(minU, maxV).endVertex();
                buffer.vertex(pose, x1, y1, 0).uv(maxU, maxV).endVertex();
                buffer.vertex(pose, x1, y0, 0).uv(maxU, minV).endVertex();
                buffer.vertex(pose, x0, y0, 0).uv(minU, minV).endVertex();
            }
        }
        Tesselator.getInstance().end();
    }

    private static void quad(DLGuiGraphics g, @Nullable ResourceLocation location, int textureId, int x, int y, int width, int height, float u, float v, float uW, float vH, int textureWidth, int textureHeight) {
        BufferBuilder buffer = beginTextured(g, location, textureId);
        Matrix4f pose = g.pose().last().pose();
        float minU = u / textureWidth;
        float minV = v / textureHeight;
        float maxU = (u + uW) / textureWidth;
        float maxV = (v + vH) / textureHeight;
        buffer.vertex(pose, x, y + height, 0).uv(minU, maxV).endVertex();
        buffer.vertex(pose, x + width, y + height, 0).uv(maxU, maxV).endVertex();
        buffer.vertex(pose, x + width, y, 0).uv(maxU, minV).endVertex();
        buffer.vertex(pose, x, y, 0).uv(minU, minV).endVertex();
        Tesselator.getInstance().end();
    }

    private static BufferBuilder beginTextured(DLGuiGraphics g, @Nullable ResourceLocation location, int textureId) {
        if (location != null) {
            setTexture(location);
        } else {
            setTexture(textureId);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        return buffer;
    }

    /**
     * Draws an item like in an inventory slot, 16x16 pixels, including its decorations.
     */
    public static void item(DLGuiGraphics g, ItemStack stack, double x, double y) {
        item(g, stack, x, y, 1, true);
    }

    /**
     * Draws an item like in an inventory slot, including its decorations.
     *
     * @param scale 1 draws it at 16x16 pixels
     */
    public static void item(DLGuiGraphics g, ItemStack stack, double x, double y, float scale) {
        item(g, stack, x, y, scale, true);
    }

    /**
     * Draws an item like in an inventory slot. Does nothing for a {@code null} or empty stack.
     * Advances the Z cursor, so following content is drawn in front of the item.
     *
     * @param scale       1 draws it at 16x16 pixels
     * @param decorations whether stack size and durability bar are drawn
     */
    public static void item(DLGuiGraphics g, ItemStack stack, double x, double y, float scale, boolean decorations) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(0, 0, -ITEM_Z_COMPENSATION + ITEM_Z_BAND / 2.0D);
        pose.scale(scale, scale, 1);
        g.graphics().renderItem(stack, (int) Transform.snap(x), (int) Transform.snap(y));
        pose.popPose();

        if (decorations) {
            itemDecoration(g, stack, x, y, scale);
        }
        g.advanceZ(ITEM_Z_BAND);
    }

    /**
     * Draws only the overlay of an item, meaning stack size and durability bar.
     */
    public static void itemDecoration(DLGuiGraphics g, ItemStack stack, double x, double y, float scale) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(0, 0, -DECORATION_Z_COMPENSATION + ITEM_Z_BAND);
        pose.scale(scale, scale, 1);
        g.graphics().renderItemDecorations(g.font(), stack, (int)Transform.snap(x), (int)Transform.snap(y));
        pose.popPose();
    }

    /**
     * Draws an entity standing on {@code x}/{@code y}, upright and facing the viewer, 16 pixels per block.
     */
    public static void entity(DLGuiGraphics g, LivingEntity entity, double x, double y) {
        entity(g, entity, x, y, 1, LightTexture.FULL_BRIGHT);
    }

    /**
     * Draws an entity standing on {@code x}/{@code y}, upright and facing the viewer.
     *
     * @param scale 1 draws one block of the entity as 16 pixels
     * @param light packed lightmap value, usually {@link LightTexture#FULL_BRIGHT}
     */
    public static void entity(DLGuiGraphics g, LivingEntity entity, double x, double y, float scale, int light) {
        entityFollowingAngle(g, entity, x, y, scale, 0, 0, light);
    }

    /**
     * Draws an entity with its own rotation, meaning it is not turned towards the viewer.
     * Advances the Z cursor by the depth the model occupies. Does nothing if the entity is {@code null}.
     *
     * @param transformation     applied on top of the upright base pose
     * @param cameraOrientation  orientation used for view-aligned parts such as name tags,
     *                           {@code null} keeps the orientation of the world camera
     * @param light              packed lightmap value, usually {@link LightTexture#FULL_BRIGHT}
     */
    @SuppressWarnings("deprecation")
    public static void entity(DLGuiGraphics g, LivingEntity entity, double x, double y, float scale, Matrix4f transformation, @Nullable Quaternionf cameraOrientation, int light) {
        if (entity == null) {
            return;
        }
        double band = MODEL_Z_BAND * scale;
        float s = 16 * scale;
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, band / 2.0D);
        pose.mulPoseMatrix((new Matrix4f()).scaling(s, s, -s));
        pose.mulPose(ENTITY_UPRIGHT);
        pose.mulPoseMatrix(transformation);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            entityRenderDispatcher.overrideCameraOrientation(new Quaternionf(cameraOrientation).conjugate());
        }

        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, pose, g.graphics().bufferSource(), light);
        });
        g.graphics().flush();
        entityRenderDispatcher.setRenderShadow(true);
        pose.popPose();
        Lighting.setupFor3DItems();
        g.advanceZ(band);
    }

    /**
     * Draws an entity standing on {@code x}/{@code y} that looks at the mouse cursor, 16 pixels per block.
     */
    public static void entityFollowingMouse(DLGuiGraphics g, LivingEntity entity, double x, double y) {
        entityFollowingMouse(g, entity, x, y, 1);
    }

    /**
     * Draws an entity standing on {@code x}/{@code y} that looks at the mouse cursor.
     */
    public static void entityFollowingMouse(DLGuiGraphics g, LivingEntity entity, double x, double y, float scale) {
        entityFollowingMouse(g, entity, x, y, scale, LightTexture.FULL_BRIGHT);
    }

    /**
     * Draws an entity standing on {@code x}/{@code y} that looks at the mouse cursor.
     *
     * @param light packed lightmap value, usually {@link LightTexture#FULL_BRIGHT}
     */
    public static void entityFollowingMouse(DLGuiGraphics g, LivingEntity entity, double x, double y, float scale, int light) {
        entityFollowingMouse(g, entity, x, y, scale, g.mouseX(), g.mouseY(), light);
    }

    /**
     * Draws an entity that looks at the given cursor position.
     *
     * @param mouseX mouse position in the same space as {@code x}, meaning the value handed to
     *               {@code GuiComponent#render} and not a screen coordinate
     * @param mouseY mouse position in the same space as {@code y}
     */
    public static void entityFollowingMouse(DLGuiGraphics g, LivingEntity entity, double x, double y, float scale, double mouseX, double mouseY, int light) {
        if (entity == null) {
            return;
        }
        float aX = (float)Math.atan((x - mouseX) / 40.0D);
        float aY = (float)Math.atan((y - entity.getEyeHeight() * 16.0F * scale - mouseY) / 40.0D);
        entityFollowingAngle(g, entity, x, y, scale, aX, aY, light);
    }

    /**
     * Draws an entity looking into the given direction. Body and head rotation of the entity are
     * changed for the draw call and restored afterwards. Does nothing if the entity is {@code null}.
     *
     * @param angleXComponent horizontal look factor, 0 faces the viewer, scaled by 40 degrees for the
     *                        head and 20 degrees for the body
     * @param angleYComponent vertical look factor, 0 looks straight ahead, scaled by 20 degrees
     */
    public static void entityFollowingAngle(DLGuiGraphics g, LivingEntity entity, double x, double y, float scale, float angleXComponent, float angleYComponent, int light) {
        if (entity == null) {
            return;
        }
        Quaternionf tilt = (new Quaternionf()).rotateX(angleYComponent * 20.0F * 0.017453292F);
        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + angleXComponent * 20.0F;
        entity.setYRot(180.0F + angleXComponent * 40.0F);
        entity.setXRot(-angleYComponent * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        entity(g, entity, x, y, scale, new Matrix4f().rotate(tilt), tilt, light);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
    }

    /**
     * Draws a block state as a 3D model with its front lower left corner on {@code x}/{@code y}, 16 pixels per block.
     *
     * @param renderType must match the layer of the block, otherwise nothing is drawn
     */
    public static void blockState(DLGuiGraphics g, BlockState state, double x, double y, RenderType renderType) {
        blockState(g, state, x, y, 1, renderType, new Matrix4f(), LightTexture.FULL_BRIGHT);
    }

    /**
     * Draws a block state as a 3D model.
     *
     * @param scale      1 draws the block as 16x16 pixels
     * @param renderType must match the layer of the block, otherwise nothing is drawn
     * @param light      packed lightmap value, usually {@link LightTexture#FULL_BRIGHT}
     */
    public static void blockState(DLGuiGraphics g, BlockState state, double x, double y, float scale, RenderType renderType, int light) {
        blockState(g, state, x, y, scale, renderType, new Matrix4f(), light);
    }

    /**
     * Draws a block state as a 3D model. The mesh is built once per block state and cached.
     *
     * @param renderType     must match the layer of the block, otherwise nothing is drawn
     * @param transformation applied in model space, where one block is 1 unit and the pivot is its corner
     */
    public static void blockState(DLGuiGraphics g, BlockState state, double x, double y, float scale, RenderType renderType, Matrix4f transformation, int light) {
        model(g, BLOCK_STATE_MODEL, x, y, scale, state, renderType, transformation, light);
    }

    /**
     * Draws a {@link DLModel} with its front lower left corner on {@code x}/{@code y}, 16 pixels per block.
     */
    public static void model(DLGuiGraphics g, DLModel model, double x, double y, BlockState state, RenderType renderType) {
        model(g, model, x, y, 1, state, renderType, new Matrix4f(), LightTexture.FULL_BRIGHT);
    }

    /**
     * Draws a {@link DLModel}.
     *
     * @param scale 1 draws one model unit as 16 pixels
     * @param light packed lightmap value, usually {@link LightTexture#FULL_BRIGHT}
     */
    public static void model(DLGuiGraphics g, DLModel model, double x, double y, float scale, BlockState state, RenderType renderType, int light) {
        model(g, model, x, y, scale, state, renderType, new Matrix4f(), light);
    }

    /**
     * Draws a {@link DLModel}. Advances the Z cursor by the depth the model occupies.
     * Does nothing if the model is {@code null}.
     *
     * @param state          block state the mesh is built from, and the cache key of the model
     * @param renderType     must be one of the render types the model builds faces for, otherwise nothing is drawn
     * @param transformation applied in model space, where one unit is 16 pixels and the pivot is the corner
     */
    public static void model(DLGuiGraphics g, DLModel model, double x, double y, float scale, BlockState state, RenderType renderType, Matrix4f transformation, int light) {
        if (model == null) {
            return;
        }
        double band = MODEL_Z_BAND * scale;
        float s = scale * 16;
        Lighting.setupForFlatItems();
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, band);
        pose.scale(1, 1, -1);
        pose.mulPoseMatrix((new Matrix4f()).scaling(s, s, s));
        pose.mulPoseMatrix(transformation);
        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        model.render(pose.last(), buffersource.getBuffer(renderType), DLModel.ModelType.BLOCK, state, ModelContext.EMPTY, DLColor.WHITE, light, 0);
        buffersource.endBatch();
        pose.popPose();
        Lighting.setupFor3DItems();
        g.advanceZ(band);
    }

    /**
     * Draws a vanilla tooltip box with its top left corner on the given position.
     * Does nothing for an empty line list.
     * @apiNote Tooltips belong into the deferred tooltip layer, otherwise following content covers them.
     *
     * @param maxWidth width at which the lines are wrapped
     */
    public static void tooltip(DLGuiGraphics g, double screenX, double screenY, List<? extends FormattedText> lines, int maxWidth) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        Font font = g.font();
        g.graphics().renderTooltip(font, TextUtils.splitToFormattedCharSequences(font, lines, maxWidth), (int)Transform.snap(screenX), (int)Transform.snap(screenY));
        g.graphics().flush();
    }

    /**
     * Draws a vanilla tooltip box next to the given position, using the same offset vanilla applies
     * to the cursor, so the position can be the mouse position.
     */
    public static void tooltipAt(DLGuiGraphics g, double x, double y, List<? extends FormattedText> lines, int maxWidth) {
        tooltip(g, x - 8, y - 16, lines, maxWidth);
    }
}
