package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public abstract class WidgetContainer extends AbstractContainerEventHandler implements Widget, NarratableEntry, ITickable, IDragonLibContainer<WidgetContainer> {

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    
    protected boolean hovered;
    protected boolean active = true;
    protected boolean visible = true;
    protected float alpha = 1.0F;
    private boolean mouseSelected;

    protected final List<GuiEventListener> children = new ArrayList<>();
    protected final List<Widget> renderables = new ArrayList<>();

    protected final Font font;

    @SuppressWarnings("resource")
    public WidgetContainer(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.font = Minecraft.getInstance().font;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean setHovered(int mouseX, int mouseY) {
        return hovered = isMouseOver(mouseX, mouseY);
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        children().stream().filter(x -> x instanceof AbstractWidget).forEach(x -> ((AbstractWidget)x).active = active);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public final void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderMainLayer(new Graphics(poseStack), mouseX, mouseY, partialTicks);
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        return Optional.empty();//return getChildAtImpl((int)mouseX, (int)mouseY);
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        setHovered(mouseX, mouseY);
        
        Iterator<Widget> w = renderables.iterator();
        while (w.hasNext()) {
            w.next().render(graphics.poseStack(), mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        Iterator<Widget> w = renderables.iterator();
        while (w.hasNext()) {
            if (w.next() instanceof IDragonLibWidget layeredWidget) {
                layeredWidget.renderBackLayer(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        Iterator<Widget> w = renderables.iterator();
        while (w.hasNext()) {
            if (w.next() instanceof IDragonLibWidget layeredWidget) {
                layeredWidget.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
            }
        }

        IDragonLibContainer.super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        Iterator<? extends GuiEventListener> w = children().iterator();
        while (w.hasNext()) {
            if (w.next() instanceof ITickable tickableWidget) {
                tickableWidget.tick();
            }
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    protected <T extends GuiEventListener & Widget> T addRenderableWidget(T guiEventListener) {
        this.addRenderableOnly((Widget)guiEventListener);
        return this.addWidget(guiEventListener);
    }

    protected <T extends Widget> T addRenderableOnly(T widget) {
        this.renderables.add(widget);
        return widget;
    }

    protected <T extends GuiEventListener> T addWidget(T guiEventListener) {
        this.children.add(guiEventListener);
        return guiEventListener;
    }

    protected void removeWidget(GuiEventListener guiEventListener) {
        if (guiEventListener instanceof Widget) {
            this.renderables.remove((Widget)guiEventListener);
        }

        this.children.remove(guiEventListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.children.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (!isActive() || !isVisible()) {
            return false;
        }
        
        // vanilla code, but inverted
        ListIterator<? extends GuiEventListener> iterator = this.children().listIterator(this.children().size());
        while (iterator.hasPrevious()) {
            GuiEventListener guiEventListener = iterator.previous();

            if (!guiEventListener.mouseClicked(mouseX, mouseY, button)) continue;
            this.setFocused(guiEventListener);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.setDragging(true);
            }
            return true;
        }
        return isHovered();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override
    public void onFocusChangeEvent(boolean focus) {        
        if (!focus) {
            children().stream().filter(x -> x instanceof IDragonLibWidget).forEach(x -> ((IDragonLibWidget)x).onFocusChangeEvent(false));
            setFocused(null);
        }
    }
    
    @Override
    public void setFocused(GuiEventListener guiEventListener) {
        boolean sameWidget = getFocused() == guiEventListener;
        if (getFocused() != null && !sameWidget && getFocused() instanceof IDragonLibWidget w) {
            w.onFocusChangeEvent(false);
        }
        super.setFocused(guiEventListener);
        if (getFocused() != null && !sameWidget &&  getFocused() instanceof IDragonLibWidget w) {
            w.onFocusChangeEvent(true);
        }
    }

    @Override
    public DLContextMenu getContextMenu() {
        return null;
    }

    @Override
    public void setMenu(DLContextMenu menu) {}

    @Override
    public boolean isMouseSelected() {
        return mouseSelected;
    }

    @Override
    public void setMouseSelected(boolean selected) {
        this.mouseSelected = selected;
    }    
}
