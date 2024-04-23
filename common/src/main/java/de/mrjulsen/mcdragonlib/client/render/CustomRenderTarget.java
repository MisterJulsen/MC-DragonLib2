package de.mrjulsen.mcdragonlib.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;

public class CustomRenderTarget extends RenderTarget {

    public CustomRenderTarget(boolean useDepth) {
        super(useDepth);
    }

    public static CustomRenderTarget create(Window mainWindow) {
        CustomRenderTarget framebuffer = new CustomRenderTarget(true);
        framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.enableStencil();
        return framebuffer;
    }

    @SuppressWarnings("resource")
    public void renderWithAlpha(float alpha) {
        Window window = Minecraft.getInstance().getWindow();

        float vx = (float) window.getGuiScaledWidth();
        float vy = (float) window.getGuiScaledHeight();
        float tx = (float) viewWidth / (float) width;
        float ty = (float) viewHeight / (float) height;

        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(() -> Minecraft.getInstance().gameRenderer.blitShader);
        RenderSystem.getShader().setSampler("DiffuseSampler", colorTextureId);

        bindRead();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        bufferbuilder.vertex(0, vy, 0).color(1, 1, 1, alpha).uv(0, 0).endVertex();
        bufferbuilder.vertex(vx, vy, 0).color(1, 1, 1, alpha).uv(tx, 0).endVertex();
        bufferbuilder.vertex(vx, 0, 0).color(1, 1, 1, alpha).uv(tx, ty).endVertex();
        bufferbuilder.vertex(0, 0, 0).color(1, 1, 1, alpha).uv(0, ty).endVertex();

        tessellator.end();
        unbindRead();
    }

    private boolean stencilEnabled = false;
    
    /**
    * Attempts to enable 8 bits of stencil buffer on this FrameBuffer.
    * Modders must call this directly to set things up.
    * This is to prevent the default cause where graphics cards do not support stencil bits.
    * <b>Make sure to call this on the main render thread!</b>
    */
    public void enableStencil() {
        if (stencilEnabled) return;
        stencilEnabled = true;
        this.resize(viewWidth, viewHeight, net.minecraft.client.Minecraft.ON_OSX);
    }

    /**
    * Returns wither or not this FBO has been successfully initialized with stencil bits.
    * If not, and a modder wishes it to be, they must call enableStencil.
    */
    public boolean isStencilEnabled() {
        return this.stencilEnabled;
    }

}
