package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DLButton extends Button implements IDragonLibWidget {
    
    @SuppressWarnings({ "unchecked", "resource" })
    public <T extends DLButton> DLButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Consumer<T> pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, (btn) -> pOnPress.accept((T)btn), DEFAULT_NARRATION);
        this.font = Minecraft.getInstance().font;
    }

    public DLButton(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pMessage, (btn) -> {});
    }

    private DLContextMenu menu;
    private boolean mouseSelected;
    protected final Font font;
    protected AreaStyle style = AreaStyle.NATIVE;

    

    public void setRenderStyle(AreaStyle style) {
        this.style = style;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
    }

    public void onHoverChange(int mouseX, int mouseY, boolean isHovering) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        GuiUtils.setTint(1.0F, 1.0F, 1.0F, this.alpha);
        renderMainLayer(new Graphics(graphics, graphics.pose()), mouseX, mouseY, partialTicks);
    }

    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GuiUtils.setTexture(DragonLib.NATIVE_WIDGETS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        
        DynamicGuiRenderer.renderArea(graphics, getX(), getY(), width, height, style, isActive() ? (isFocused() || isMouseSelected() ? ButtonState.SELECTED : ButtonState.BUTTON) : ButtonState.RAISED);

        int j = active ? DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED;
        GuiUtils.drawString(graphics, font, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, this.getMessage(), j | Mth.ceil(this.alpha * 255.0F) << 24, EAlignment.CENTER, true);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        IDragonLibWidget.super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);

        boolean wasHovering = isHovered;
        setHovered(mouseX, mouseY);
        if (wasHovering != isHovered) {
            onHoverChange(mouseX, mouseY, isHovered);
        }
    }

    public boolean setHovered(int mouseX, int mouseY) {
        return isHovered = mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override
    public DLContextMenu getContextMenu() {
        return menu;
    }

    @Override
    public void onFocusChangeEvent(boolean focus) {
        setFocused(focus);
    }

    @Override
    public boolean isMouseSelected() {
        return mouseSelected;
    }

    @Override
    public void setMouseSelected(boolean selected) {
        this.mouseSelected = selected;
    }
}
