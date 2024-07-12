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
import de.mrjulsen.mcdragonlib.mixin.ScreenMixin;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class DLScreen extends Screen implements IDragonLibContainer<DLScreen> {

    protected final List<DLTooltip> tooltips = new ArrayList<>();
    protected DLContextMenu menu;
    private boolean mouseSelected;

    private int allowedLayerIndex = DEFAULT_LAYER_INDEX;
    private int layerIndex = DEFAULT_LAYER_INDEX;

    public final Consumer<DLButton> NO_BUTTON_CLICK_ACTION = (a) -> {};
    public final BiConsumer<DLCycleButton<?>, ?> NO_CYCLE_BUTTON_VALUE_CHANGE_ACTION = (a, b) -> {};
    public final BiConsumer<DLEditBox, Boolean> NO_EDIT_BOX_FOCUS_CHANGE_ACTION = (a, b) -> {};
    public final BiConsumer<DLSlider, Double> NO_SLIDER_CHANGE_VALUE_ACTION = (a, b) -> {};

    protected DLScreen(Component title) {
        super(title);
    }

    @Override
    public final boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        tooltips.clear();
    }

    @Override
    public void tick() {
        super.tick();
        this.renderables.stream().filter(x -> x instanceof ITickable).forEach(x -> ((ITickable)x).tick());
    }

    protected void onDone() {}

    @Override
    public void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        Iterator<Renderable> w = this.renderables.iterator();
        while (w.hasNext()) {
            Renderable widget = (Renderable)w.next();
            if (widget instanceof IDragonLibWidget layeredWidget && layeredWidget.visible() && (!checkWidgetBounds() || DLUtils.rectanglesIntersecting(layeredWidget.x(), layeredWidget.y(), layeredWidget.width(), layeredWidget.height(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height()))) {
                layeredWidget.renderBackLayer(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        Iterator<Renderable> w = this.renderables.iterator();
        while (w.hasNext()) {
            Renderable widget = (Renderable)w.next();
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
        for (Renderable widget : this.renderables) {
            if ((widget instanceof IDragonLibWidget d && (!d.visible() || (checkWidgetBounds() && !DLUtils.rectanglesIntersecting(d.x(), d.y(), d.width(), d.height(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height())))) ||
                (widget instanceof AbstractWidget abs && (!abs.visible || (checkWidgetBounds() && !DLUtils.rectanglesIntersecting(abs.getX(), abs.getY(), abs.getWidth(), abs.getHeight(), this.x() + checkWidgetBoundsOffset().getFirst(), this.y() + checkWidgetBoundsOffset().getSecond(), this.width(), this.height()))))) {
                continue;
            }
            widget.render(graphics.graphics(), mouseX, mouseY, partialTicks);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        mouseSelectEvent(mouseX, mouseY);

        Graphics graphics = new Graphics(guiGraphics, guiGraphics.pose());

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
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
    }

    public void renderScreenBackground(Graphics graphics) {
        renderBackground(graphics.graphics());
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
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
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

    protected <T extends Enum<T> & ITranslatableEnum> DLCycleButton<T> addCycleButton(String modid, Class<T> clazz, int x, int y, int width, int height, Component text, T initialValue, BiConsumer<DLCycleButton<?>, T> onValueChanged, DLTooltip tooltip) {
        DLCycleButton<T> btn = GuiUtils.createCycleButton(modid, clazz, x, y, width, height, text, initialValue, onValueChanged);
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

        widget.setX(x);
        widget.setY(y);
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
            this.childrenLayered().stream().filter(x -> x instanceof IDragonLibWidget).forEach(x -> ((IDragonLibWidget)x).onFocusChangeEvent(false));
        }
    }

    //#region HACKY FOCUS FIX
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        } else if (this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            FocusNavigationEvent event;
            switch (keyCode) {
                case 258:
                    event = ((ScreenMixin)this).dragonlib$createTabEvent();
                    break;
                case 259:
                case 260:
                case 261:
                default:
                    event = null;
                    break;
                case 262:
                    event = ((ScreenMixin)this).dragonlib$createArrowEvent(ScreenDirection.RIGHT);
                    break;
                case 263:
                    event = ((ScreenMixin)this).dragonlib$createArrowEvent(ScreenDirection.LEFT);
                    break;
                case 264:
                    event = ((ScreenMixin)this).dragonlib$createArrowEvent(ScreenDirection.DOWN);
                    break;
                case 265:
                    event = ((ScreenMixin)this).dragonlib$createArrowEvent(ScreenDirection.UP);
            }

            FocusNavigationEvent focusNavigationEvent = (FocusNavigationEvent)event;
            if (focusNavigationEvent != null) {
                ComponentPath componentPath = nextFocusPath(focusNavigationEvent);
                if (componentPath == null && focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
                    ((ScreenMixin)this).dragonlib$clearFocus();
                    componentPath = nextFocusPath(focusNavigationEvent);
                }

                if (componentPath != null) {
                    this.changeFocus(componentPath);
                }
            }

            return false;
        }
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return dragonlib$nextFocusPath(event); // VERY HACKY! Plz help...
    }
    //#endregion

    @Override
    public DLContextMenu getContextMenu() {
        return menu;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
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
}
