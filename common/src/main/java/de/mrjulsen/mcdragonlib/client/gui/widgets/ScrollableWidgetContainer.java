package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.Pair;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public abstract class ScrollableWidgetContainer extends WidgetContainer {

    protected double xScrollOffset;
    protected double yScrollOffset;

    public ScrollableWidgetContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public double getXScrollOffset() {
        return xScrollOffset;
    }

    public double getYScrollOffset() {
        return yScrollOffset;
    }

    public void setXScrollOffset(double v) {
        this.xScrollOffset = v;
    }

    public void setYScrollOffset(double v) {
        this.yScrollOffset = v;
    }

    @Override
    public boolean checkWidgetBounds() {
        return true;
    }

    @Override
    public Pair<Double, Double> checkWidgetBoundsOffset() {
        return Pair.of(xScrollOffset, yScrollOffset);
    }
    
    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        GuiUtils.enableScissor(graphics, x, y, getWidth(), getHeight());
        graphics.poseStack().translate(-xScrollOffset, -yScrollOffset, 0);
        renderMainLayerScrolled(graphics, (int)(mouseX + xScrollOffset), (int)(mouseY + yScrollOffset), partialTicks);
        GuiUtils.disableScissor(graphics);
    }
    
    public void renderMainLayerScrolled(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        GuiUtils.enableScissor(graphics, x, y, getWidth(), getHeight());
        renderBackLayerScrolled(graphics, mouseX, mouseY, partialTicks);
        GuiUtils.disableScissor(graphics);
    }
    
    public void renderBackLayerScrolled(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isInBounds(double mouseX, double mouseY) {
        return super.isInBounds(mouseX - xScrollOffset, mouseY - yScrollOffset);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX + xScrollOffset, mouseY + yScrollOffset, button);
    }

    @Override
    public void mouseSelectEvent(int mouseX, int mouseY) {
        super.mouseSelectEvent((int)(mouseX + xScrollOffset), (int)(mouseY + yScrollOffset));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int i, double f, double g) {
        return super.mouseDragged(mouseX + xScrollOffset, mouseY + yScrollOffset, i, f, g);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int i) {
        return super.mouseReleased(mouseX + xScrollOffset, mouseY + yScrollOffset, i);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX + xScrollOffset, mouseY + yScrollOffset);
    }

    @Override
    public boolean contextMenuMouseClickEvent(Screen screen, IDragonLibContainer<?> parent, int mouseX, int mouseY, int xOffset, int yOffset, int button, GuiAreaDefinition openingBounds) {
        return super.contextMenuMouseClickEvent(screen, parent, mouseX, mouseY, (int)xScrollOffset, (int)yScrollOffset, button, GuiAreaDefinition.of(this));
    }

    @Override
    public Optional<GuiEventListener> getChildAtImpl(int mouseX, int mouseY) {
        return super.getChildAtImpl((int)(mouseX + xScrollOffset), (int)(mouseY + yScrollOffset));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double f) {
        return super.mouseScrolled(mouseX + xScrollOffset, mouseY + yScrollOffset, f);
    }
}
