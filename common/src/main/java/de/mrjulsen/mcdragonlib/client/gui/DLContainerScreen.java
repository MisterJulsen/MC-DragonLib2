package de.mrjulsen.mcdragonlib.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibContainer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.mixin.AbstractWidgetAccessor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DLContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IDragonLibContainer<DLContainerScreen<T>> {

    protected final List<DLTooltip> tooltips = new ArrayList<>();
    protected DLContextMenu contextMenu;
    private boolean mouseSelected;

    private int allowedLayerIndex = DEFAULT_LAYER_INDEX;
    private int layerIndex = DEFAULT_LAYER_INDEX;

    public final Consumer<DLButton> NO_BUTTON_CLICK_ACTION = (a) -> {};
    public final BiConsumer<DLCycleButton<?>, ?> NO_CYCLE_BUTTON_VALUE_CHANGE_ACTION = (a, b) -> {};
    public final BiConsumer<DLEditBox, Boolean> NO_EDIT_BOX_FOCUS_CHANGE_ACTION = (a, b) -> {};
    public final BiConsumer<DLSlider, Double> NO_SLIDER_CHANGE_VALUE_ACTION = (a, b) -> {};

    public DLContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public void renderScreenBackground(Graphics graphics) {
        renderBackground(graphics.poseStack());
    }    

    @Override
    protected void init() {
        super.init();
        tooltips.clear();
    }    

    @Override
    protected void containerTick() {
        super.containerTick();
        this.renderables.stream().filter(x -> x instanceof ITickable).forEach(x -> ((ITickable)x).tick());
    }

    protected void onDone() {}

    @Override
    public void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        Iterator<Widget> w = this.renderables.iterator();
        while (w.hasNext()) {
            Widget widget = (Widget)w.next();
            if (widget instanceof IDragonLibWidget layeredWidget && layeredWidget.visible() && (!checkWidgetBounds() || DLUtils.rectanglesIntersecting(layeredWidget.x(), layeredWidget.y(), layeredWidget.width(), layeredWidget.height(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height()))) {
                layeredWidget.renderBackLayer(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        Iterator<Widget> w = this.renderables.iterator();
        while (w.hasNext()) {
            Widget widget = (Widget)w.next();
            if (widget instanceof IDragonLibWidget layeredWidget && layeredWidget.visible() && (!checkWidgetBounds() || DLUtils.rectanglesIntersecting(layeredWidget.x(), layeredWidget.y(), layeredWidget.width(), layeredWidget.height(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height()))) {
                layeredWidget.renderFrontLayer(graphics, mouseX, mouseY, partialTick);
            }
        }
        DLItemButton.renderAllItemButtonTooltips(this, graphics, mouseX, mouseY);
        tooltips.forEach(x -> x.render(this, graphics, mouseX, mouseY));

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 500);
        if (getContextMenu() != null) {
            IDragonLibContainer.super.renderFrontLayer(graphics, mouseX, mouseY, partialTick);
        }
        graphics.poseStack().popPose();
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        for (Widget widget : this.renderables) {
            if ((widget instanceof IDragonLibWidget d && (!d.visible() || (checkWidgetBounds() && !DLUtils.rectanglesIntersecting(d.x(), d.y(), d.width(), d.height(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height())))) ||
                (widget instanceof AbstractWidget abs && (!abs.visible || (checkWidgetBounds() && !DLUtils.rectanglesIntersecting(abs.x, abs.y, abs.getWidth(), abs.getHeight(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height()))))) {
                continue;
            }
            widget.render(graphics.poseStack(), mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        mouseSelectEvent(mouseX, mouseY);

        Graphics graphics = new Graphics(poseStack);

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, -100);
        renderBackLayer(graphics, mouseX, mouseY, partialTick);
        graphics.poseStack().popPose();

        renderMainLayer(graphics, mouseX, mouseY, partialTick);

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 100);
        renderFrontLayer(graphics, mouseX, mouseY, partialTick);
        graphics.poseStack().popPose();
    }    

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (contextMenuMouseClickEvent(this, this, (int)mouseX, (int)mouseY, 0, 0, button, GuiAreaDefinition.of(this))) {
            return true;
        }
        
        // vanilla code, but inverted
        ListIterator<? extends GuiEventListener> iterator = childrenLayered().listIterator(childrenLayered().size());
        while (iterator.hasPrevious()) {
            GuiEventListener guiEventListener = iterator.previous();

            if (!guiEventListener.mouseClicked(mouseX, mouseY, button)) continue;
            this.setFocused(guiEventListener);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        return getChildAtImpl((int)mouseX, (int)mouseY);
    }

    @Override
    protected <W extends GuiEventListener & NarratableEntry> W addWidget(W guiEventListener) {
        return super.addWidget(guiEventListener);
    }

    protected DLTooltip addTooltip(DLTooltip tooltip) {
        this.tooltips.add(tooltip);
        return tooltip;
    }

    protected boolean removeTooltips(Predicate<? super DLTooltip> condition) {
        return tooltips.removeIf(condition);
    }

    protected DLButton addButton(int x, int y, int width, int height, Component text, Consumer<DLButton> onClick, DLTooltip tooltip) {
        DLButton btn = GuiUtils.createButton(x, y, width, height, text, onClick);
        return addRenderableWidget(btn, x, y, width, height, tooltip);
	}

    protected <E extends Enum<E> & ITranslatableEnum> DLCycleButton<E> addCycleButton(String modid, Class<E> clazz, int x, int y, int width, int height, Component text, E initialValue, BiConsumer<DLCycleButton<?>, E> onValueChanged, DLTooltip tooltip) {
        DLCycleButton<E> btn = GuiUtils.createCycleButton(modid, clazz, x, y, width, height, text, initialValue, onValueChanged);
        return addRenderableWidget(btn, x, y, width, height, tooltip);
	}

    protected DLCycleButton<Boolean> addOnOffButton(int x, int y, int width, int height, Component text, boolean initialValue, BiConsumer<DLCycleButton<?>, Boolean> onValueChanged, DLTooltip tooltip) {
        DLCycleButton<Boolean> btn = GuiUtils.createOnOffButton(x, y, width, height, text, initialValue, onValueChanged);
        return addRenderableWidget(btn, x, y, width, height, tooltip);
	}

    protected DLEditBox addEditBox(int x, int y, int width, int height, String text, Component hint, boolean drawBg, Consumer<String> onValueChanged, BiConsumer<DLEditBox, Boolean> onFocusChanged, DLTooltip tooltip) {
        DLEditBox box = GuiUtils.createEditBox(x, y, width, height, font, text, hint, drawBg, onValueChanged, onFocusChanged);
        return this.addRenderableWidget(box, x, y, width, height, tooltip);
    }

    protected DLSlider addSlider(int x, int y, int width, int height, Component prefix, Component suffix, double min, double max, double step, double initialValue, boolean drawLabel, BiConsumer<DLSlider, Double> onValueChanged, Consumer<DLSlider> onUpdateMessage, DLTooltip tooltip) {
        DLSlider slider = GuiUtils.createSlider(x, y, width, height, prefix, suffix, min, max, step, initialValue, drawLabel, onValueChanged, onUpdateMessage);        
        return this.addRenderableWidget(slider, x, y, width, height, tooltip);
    }

    protected <W extends AbstractWidget> W addRenderableWidget(W widget, int x, int y, int width, int height, DLTooltip tooltip) {
        if (tooltip != null && !tooltip.equals(DLTooltip.empty())) {
            addTooltip(tooltip.assignedTo(widget));
        }

        widget.x = x;
        widget.y = y;
        widget.setWidth(width);
        ((AbstractWidgetAccessor)widget).setHeight(height);
        
		return addRenderableWidget(widget);
    }

    /**
     * Wrapper for {@code Minecraft.getInstance().setScreen()}
     * @param screen
     */
    public static void setScreen(DLScreen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double pDelta) {
        return containerMouseScrolled(mouseX, mouseY, pDelta);
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
    public void onFocusChangeEvent(boolean focus) {
        if (!focus) {
            this.children().stream().filter(x -> x instanceof IDragonLibWidget).forEach(x -> ((IDragonLibWidget)x).onFocusChangeEvent(false));
        }
    }

    @Override
    public boolean changeFocus(boolean focus) {
        return changeFocusImpl(focus);
    }

    @Override
    public DLContextMenu getContextMenu() {
        return contextMenu;
    }

    @Override
    public void setMenu(DLContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }    
    
    @Override
    public int getAllowedLayer() {
        return allowedLayerIndex;
    }

    @Override
    public void setAllowedLayer(int index) {
        this.allowedLayerIndex = index;
    }

    @Override
    public void setWidgetLayerIndex(int layerIndex) {
        this.layerIndex = layerIndex;
    }

    @Override
    public int getWidgetLayerIndex() {
        return layerIndex;
    }


    @Override
    public boolean isMouseSelected() {
        return mouseSelected;
    }

    @Override
    public void setMouseSelected(boolean selected) {
        this.mouseSelected = selected;
    }



    @Override
    public int x() {
        return 0;
    }

    @Override
    public int y() {
        return 0;
    }    

    @Override
    public void set_x(int x) {}

    @Override
    public void set_y(int y) {}

    @Override
    public void set_width(int w) {}

    @Override
    public void set_height(int h) {}

    @Override
    public void set_visible(boolean b) {}

    @Override
    public boolean visible() {
        return true;
    }

    @Override
    public void set_active(boolean b) {}

    @Override
    public boolean active() {
        return true;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }
}
