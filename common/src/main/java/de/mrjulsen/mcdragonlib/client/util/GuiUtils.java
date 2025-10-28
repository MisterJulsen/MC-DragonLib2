package de.mrjulsen.mcdragonlib.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel.ModelType;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class GuiUtils {

    public static enum TextureFillMode {
        STRETCH,
        TILE
    }
    

    public static double mouseXOnScreen() {
        return Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth();
    }

    public static double mouseYOnScreen() {
        return Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();
    }

    public static double getScreenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static double getScreenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public static void enableScissor(DLGuiGraphics graphics, Rectangle area) {
        enableScissor(graphics, (int)area.x(), (int)area.y(), (int)area.width(), (int)area.height());
    }

    public static void enableScissor(DLGuiGraphics graphics, int x, int y, int w, int h) {
        int scale = (int)Minecraft.getInstance().getWindow().getGuiScale();    
        RenderSystem.enableScissor(x * scale, Minecraft.getInstance().getWindow().getHeight() - (y + h) * scale, w * scale, h * scale);   
    }

    public static void disableScissor(DLGuiGraphics graphics) {
        RenderSystem.disableScissor();
    }

    public static void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static FormattedCharSequence toFormattedCharSequence(FormattedText text) {
        return text instanceof Component ? ((Component)text).getVisualOrderText() : Language.getInstance().getVisualOrder(text);
    }
    
    
    public static <T extends FormattedText> List<FormattedCharSequence> splitToFormattedCharSequences(Font font, Collection<T> components, int maxWidth) {
        List<FormattedCharSequence> lines = new ArrayList<>(components.size());
        for (T component : components) {
            lines.addAll(font.split(component, maxWidth));
        }
        return lines;
    }
    
    public static <T extends FormattedText> List<FormattedText> splitText(Font font, Collection<T> components, int maxWidth) {
        List<FormattedText> lines = new ArrayList<>(components.size());
        for (T component : components) {
            lines.addAll(font.getSplitter().splitLines(component, maxWidth, Style.EMPTY));
        }
        return lines;
    }

    public static void drawTooltip(DLGuiGraphics graphics, Font font, int x, int y, List<? extends FormattedText> lines, int maxWidth) {
        graphics.graphics().renderTooltip(font, splitToFormattedCharSequences(font, lines, maxWidth), x, y);
    }

    public static void drawTooltipDirectlyAt(DLGuiGraphics graphics, Font font, int x, int y, List<? extends FormattedText> lines, int maxWidth) {
        drawTooltip(graphics, font, x - 8, y - 16, lines, maxWidth);
    }

    public static <T extends Enum<T> & ITranslatableEnum> List<Component> getEnumTooltipData(Class<T> enumClass, int maxWidth) {
        List<Component> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.add(enumValue.getEnumDescriptionTranslation());
        c.add(TextUtils.text(" "));
        for (T t : enumClass.getEnumConstants()) {
            c.add(TextUtils.text("> ").withStyle(ChatFormatting.BOLD).append(t.getValueTranslation()).withStyle(ChatFormatting.BOLD));
            c.add(t.getValueDescriptionTranslation().withStyle(ChatFormatting.GRAY));
        }
        return c;
    }



    public static void setTexture(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void setTexture(int textureId) {
        RenderSystem.setShaderTexture(0, textureId);
    }

    public static void setTint(DLColor color) {
        float a = color.getAlphaF();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void resetTint() {
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawTexture(ResourceLocation texture, DLGuiGraphics graphics, int x, int y, int w, int h, int u, int v, int uW, int vH, TextureFillMode mode) {
        drawTexture(texture, graphics, x, y, w, h, u, v, uW, vH, mode, 256, 256);
    }

    public static void drawTexture(ResourceLocation texture, DLGuiGraphics graphics, int x, int y, int w, int h, int u, int v, int uW, int vH, TextureFillMode mode, int textureWidth, int textureHeight) {
        switch (mode) {
            case TILE -> {
                int i = (int)Math.ceil((float)w / (float)uW);
                int k = (int)Math.ceil((float)h / (float)vH);
                for (int a = 0; a < i; a++) {
                    for (int b = 0; b < k; b++) {
                        int mW = Math.min((a + 1) * uW, w) - (a * uW);
                        int mH = Math.min((b + 1) * vH, h) - (b * vH);
                        graphics.graphics().blit(texture, x + (uW * a), y + (vH * b), mW, mH, u, v, mW, mH, textureWidth, textureHeight);
                    }
                }
            }
            default -> graphics.graphics().blit(texture, x, y, w, h, u, v, uW, vH, textureWidth, textureHeight);
        }
    }

    public static void drawTexture(int textureId, DLGuiGraphics graphics, int x, int y, int w, int h, int u, int v, int uW, int vH, TextureFillMode mode, int textureWidth, int textureHeight) {
        switch (mode) {
            case TILE -> {
                int i = (int)Math.ceil((float)w / (float)uW);
                int k = (int)Math.ceil((float)h / (float)vH);
                for (int a = 0; a < i; a++) {
                    for (int b = 0; b < k; b++) {
                        int mW = Math.min((a + 1) * uW, w) - (a * uW);
                        int mH = Math.min((b + 1) * vH, h) - (b * vH);
                        blit(graphics.graphics(), textureId, x + (uW * a), y + (vH * b), mW, mH, u, v, mW, mH, textureWidth, textureHeight);
                    }
                }
            }
            default -> blit(graphics.graphics(), textureId, x, y, w, h, u, v, uW, vH, textureWidth, textureHeight);
        }
        
    }

    public static void drawTexture(DLTexture texture, DLGuiGraphics graphics, int x, int y, int w, int h, int u, int v, int uW, int vH, TextureFillMode mode) {
        if (texture.usesTextureId() || texture.getTexture().isEmpty()) {
            drawTexture(texture.getTextureId(), graphics, x, y, w, h, u, v, uW, vH, mode, texture.width(), texture.height());
        } else {            
            drawTexture(texture.getTexture().get(), graphics, x, y, w, h, u, v, uW, vH, mode, texture.width(), texture.height());
        }
    }
    
    public static void drawTexture(DLTexture texture, DLGuiGraphics graphics, int x, int y, int w, int h, int u, int v) {
        drawTexture(texture, graphics, x, y, w, h, u, v, w, h, TextureFillMode.STRETCH);
    }
    
    public static void drawTexture(DLTexture texture, DLGuiGraphics graphics, int x, int y, int w, int h) {
        drawTexture(texture, graphics, x, y, w, h, 0, 0, w, h, TextureFillMode.STRETCH);
    }


    /* COPY OF: GuiGraphics */
    private static void innerBlit(PoseStack pose, int textureId, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = pose.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY1, (float)pBlitOffset).uv(pMinU, pMinV).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY2, (float)pBlitOffset).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY2, (float)pBlitOffset).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY1, (float)pBlitOffset).uv(pMaxU, pMinV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
    
    private static void blit(GuiGraphics graphics, int textureId, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
        blit(graphics, textureId, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }

    private static void blit(GuiGraphics graphics, int textureId, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, int pUWidth, int pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight) {
        innerBlit(graphics.pose(), textureId, pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / (float)pTextureWidth, (pUOffset + (float)pUWidth) / (float)pTextureWidth, (pVOffset + 0.0F) / (float)pTextureHeight, (pVOffset + (float)pVHeight) / (float)pTextureHeight);
    }
    /* END */



    public static void fill(DLGuiGraphics graphics, Rectangle area, DLColor color) {
        fill(graphics, (int)area.x(), (int)area.y(), (int)area.width(), (int)area.height(), color);
    }

    public static void fill(DLGuiGraphics graphics, int x, int y, int w, int h, DLColor color) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.graphics().fill(x, y, x + w, y + h, color.getAsARGB());
        RenderSystem.disableBlend();
    }

    public static void fillGradient(DLGuiGraphics graphics, Rectangle area, DLColor colorA, DLColor colorB, EAlign align) {
        fillGradient(graphics, (int)area.x(), (int)area.y(), (int)area.width(), (int)area.height(), colorA, colorB, align);
    }

    public static void fillGradient(DLGuiGraphics graphics, int x, int y, int w, int h, DLColor colorA, DLColor colorB, EAlign align) {
        DLColor[] vertexColors = new DLColor[4];
        for (int i = 0; i < vertexColors.length; i++) {
            vertexColors[(align.ordinal() + i) % vertexColors.length] = (i < 2 ? colorA : colorB);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(graphics.poseStack().last().pose(), x + w, y, 0).color(vertexColors[0].getAsARGB()).endVertex();
        buffer.vertex(graphics.poseStack().last().pose(), x, y, 0).color(vertexColors[1].getAsARGB()).endVertex();
        buffer.vertex(graphics.poseStack().last().pose(), x, y + h, 0).color(vertexColors[2].getAsARGB()).endVertex();
        buffer.vertex(graphics.poseStack().last().pose(), x + w, y + h, 0).color(vertexColors[3].getAsARGB()).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
    }

    public static void drawBox(DLGuiGraphics graphics, Rectangle area, DLColor fillColor, DLColor borderColor) {
        drawBox(graphics, (int)area.x(), (int)area.y(), (int)area.width(), (int)area.height(), fillColor, borderColor);
    }

    public static void drawBox(DLGuiGraphics graphics, int x, int y, int w, int h, DLColor fillColor, DLColor borderColor) {
        fill(graphics, x, y, w, h, fillColor);
        fill(graphics, x, y, w, 1, borderColor);
        fill(graphics, x, y + h - 1, w, 1, borderColor);
        fill(graphics, x, y + 1, 1, h - 2, borderColor);
        fill(graphics, x + w - 1, y + 1, 1, h - 2, borderColor);
    }

    public static void drawString(DLGuiGraphics graphics, Font font, int x, int y, String text, DLColor color, ETextAlignment alignment, boolean dropShadow) {
        drawString(graphics, font, x, y, TextUtils.text(text), color, alignment, dropShadow);
    }

    public static void drawString(DLGuiGraphics graphics, Font font, int x, int y, FormattedText text, DLColor color, ETextAlignment alignment, boolean dropShadow) {
        int width = font.width(text);
        int offset = 0;
        switch (alignment) {
            default:
            case LEFT:
                break;
            case CENTER:
                offset = -width / 2;
                break;
            case RIGHT:
                offset = -width;
                break;
        }

        graphics.graphics().drawString(font, toFormattedCharSequence(text), x + offset, y, color.getAsARGB(), dropShadow);
    }

    public static void renderItem(DLGuiGraphics graphics, ItemStack stack, int x, int y) {
        renderItem(graphics, stack, x, y, 1, true);
    }

    public static void renderItem(DLGuiGraphics graphics, ItemStack stack, int x, int y, float scale, boolean drawDecorations) {
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(x, y, 0);
        graphics.poseStack().scale(scale, scale, 1);
        graphics.graphics().renderItem(stack, 0, 0);
        if (drawDecorations) {
            graphics.graphics().renderItemDecorations(Minecraft.getInstance().font, stack, 0, 0);
        }
        graphics.poseStack().popPose();
    }

    public static void renderEntity(DLGuiGraphics graphics, int x, int y, LivingEntity entity) {
        renderEntity(graphics, x, y, 1, entity, LightTexture.FULL_BRIGHT);
    }

    public static void renderEntity(DLGuiGraphics graphics, int x, int y, float scale, LivingEntity entity, int light) {
        renderEntity(graphics, x, y, scale, entity, new Matrix4f(), new Quaternionf(), light);
    }

    @SuppressWarnings("deprecation")
    public static void renderEntity(DLGuiGraphics graphics, int x, int y, float scale, LivingEntity entity, Matrix4f transformation, @Nullable Quaternionf cameraOrientation, int light) {
        float s = 16 * scale;
        graphics.poseStack().pushPose();
        graphics.poseStack().translate((double)x, (double)y, 16 * (scale + 1));
        graphics.poseStack().mulPoseMatrix((new Matrix4f()).scaling(s, s, -s));
        graphics.poseStack().mulPoseMatrix(transformation);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            cameraOrientation.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(cameraOrientation);
        }

        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, graphics.poseStack(), graphics.graphics().bufferSource(), light);
        });
        graphics.graphics().flush();
        entityRenderDispatcher.setRenderShadow(true);
        graphics.poseStack().popPose();
        Lighting.setupFor3DItems();
    }
    

    public static void renderEntityFollowingMouse(DLGuiGraphics graphics, int x, int y, LivingEntity entity) {
        renderEntityFollowingMouse(graphics, x, y, 1, entity);
    }

    public static void renderEntityFollowingMouse(DLGuiGraphics graphics, int x, int y, float scale, LivingEntity entity) {
        renderEntityFollowingMouse(graphics, x, y, scale, (float)Minecraft.getInstance().mouseHandler.xpos(), (float)Minecraft.getInstance().mouseHandler.ypos(), entity, LightTexture.FULL_BRIGHT);
    }

    public static void renderEntityFollowingMouse(DLGuiGraphics graphics, int x, int y, float scale, float screenMouseX, float screenMouseY, LivingEntity entity, int light) {
        Matrix4f transformation = graphics.poseStack().last().pose();
        float aX = (float)Math.atan((double)((transformation.m30() + x - screenMouseX) / 40.0F));
        float aY = (float)Math.atan((double)((transformation.m31() + y - (screenMouseY + entity.getEyeHeight() * (16 * scale))) / 40.0F));
        renderEntityFollowingAngle(graphics, x, y, scale, aX, aY, entity, light);
    }

    public static void renderEntityFollowingAngle(DLGuiGraphics graphics, int x, int y, float scale, float angleXComponent, float angleYComponent, LivingEntity entity, int light) {
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf1);
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
        Matrix4f matrix = new Matrix4f().rotate(quaternionf);
        renderEntity(graphics, x, y, scale, entity, matrix, quaternionf1, light);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
    }
    

    public static void renderBlockState(DLGuiGraphics graphics, int x, int y, BlockState state, RenderType renderType) {
        renderBlockState(graphics, x, y, 1, state, renderType, new Matrix4f(), LightTexture.FULL_BRIGHT);
    }

    public static void renderBlockState(DLGuiGraphics graphics, int x, int y, float scale, BlockState state, RenderType renderType, int light) {
        renderBlockState(graphics, x, y, scale, state, renderType, new Matrix4f(), light);
    }

    public static void renderBlockState(DLGuiGraphics graphics, int x, int y, float scale, BlockState state, RenderType renderType, Matrix4f transformation, int light) {
        DLModel model = new DLModel() {
            @Override
            protected Mesh getMesh(ModelType type, BakedModel originalModel, BlockState state, RandomSource random, ModelContext context) {
                Mesh mesh = BasicMesh.fromBlock(state, random);
                mesh.rotate(Axis.ZP.rotationDegrees(180), new Vector3f(0.5f));
                return mesh;
            }
        };
        renderModel(graphics, x, y, scale, model, state, renderType, transformation, light);
    }
    

    public static void renderModel(DLGuiGraphics graphics, int x, int y, DLModel model, BlockState state, RenderType renderType) {
        renderModel(graphics, x, y, 1, model, state, renderType, new Matrix4f(), LightTexture.FULL_BRIGHT);
    }

    public static void renderModel(DLGuiGraphics graphics, int x, int y, float scale, DLModel model, BlockState state, RenderType renderType, int light) {
        renderModel(graphics, x, y, scale, model, state, renderType, new Matrix4f(), light);
    }

    public static void renderModel(DLGuiGraphics graphics, int x, int y, float scale, DLModel model, BlockState state, RenderType renderType, Matrix4f transformation, int light) {
        float s = scale * 16;
        Lighting.setupForFlatItems();
        PoseStack stack = graphics.poseStack();
        stack.pushPose();
        stack.translate((double)x, (double)y, 16 * (scale + 1));
        stack.mulPoseMatrix((new Matrix4f()).scaling((float)s, (float)s, (float)(s)));
        stack.mulPoseMatrix(transformation);
        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        model.render(graphics.poseStack().last(), buffersource.getBuffer(renderType), ModelType.BLOCK, state, ModelContext.EMPTY, DLColor.WHITE, LightTexture.FULL_BRIGHT, 0);
        buffersource.endBatch();
        stack.popPose();
        Lighting.setupFor3DItems();
    }
}
