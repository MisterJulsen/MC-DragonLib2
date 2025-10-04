package de.mrjulsen.mcdragonlib.client.newgui.widgets.components;

import java.util.Optional;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents.ClickEvent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.BooleanProperty;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.ListProperty;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.NumberProperty;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.Property;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.ListProperty.ListOperation;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@SupportsEvents({
    DLCycleButton.SelectedItemChanged.class
})
public class DLCycleButton<T> extends DLButton {

    public record SelectedItemChanged<T>(Optional<T> item, int index) implements IEvent {}
    
    public final ListProperty<T> items = new ListProperty<T>()
        .withUpdateCallback((val, operation) -> updateSelectedItem(operation == ListOperation.ADD));
    public final NumberProperty<Integer> selectedIndex = new NumberProperty<>(-1, -1, Integer.MAX_VALUE);
    public final Property<Optional<T>> selectedItem = new Property<>(Optional.empty());
    public final Property<ITextFormatter<DLCycleButton<T>>> textFormat = new Property<>((src) -> TextUtils.text(src.text.get().getString()).append(": ").append(src.selectedItem.get().map(x -> x.toString()).orElse("")).withStyle(src.text.get().getStyle()));
    public final BooleanProperty cycling = new BooleanProperty(true, false);
 
    public DLCycleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.selectedIndex.withAfterPropertyChangedCallback((o, n) -> {
            if (!o.equals(n)) {
                Optional<T> item = Optional.ofNullable(n >= items.size() || n < 0 ? null : items.get(n));
                selectedItem.set(item);
                invokeEvent(this, new SelectedItemChanged<>(item, n));
            }
        });
        
        this.selectedItem.withAfterPropertyChangedCallback((o, n) -> {
            if (o.orElse(null) != n.orElse(null)) {
                selectedIndex.set(n.map(a -> items.indexOf(a)).orElse(-1));
            }
        });
    }

    @Override
    public boolean defaultButtonClickAction(DLGuiComponent src, ClickEvent event) {
        int targetIndex = selectedIndex.get();
        if (Screen.hasShiftDown()) {
            targetIndex--;
        } else {
            targetIndex++;
        }

        if (targetIndex < 0) {
            targetIndex = (cycling.get() ? items.size() - 1 : 0);
        } else if (targetIndex >= items.size()) {
            targetIndex = (cycling.get() ? 0 : items.size() - 1);
        }
        this.selectedIndex.set(targetIndex);

        return super.defaultButtonClickAction(src, event);
    }

    @Override
    public void renderMainLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        RenderSystem.enableBlend();
        DLWindowManager manager = getWindowManager();
        GuiUtils.setTint(backgroundTint.get().getAsARGB());
        if (!enabled.get()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DISABLED);
        } else if (isMouseDown() && (manager != null ? manager.getMouseDownButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT : true)) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.DOWN_SELECTED);
        } else if (isSelected()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.SELECTED);
        } else {            
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, ButtonState.NORMAL);
        }
        GuiUtils.setTint(textColor.get().getAsARGB());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, width() / 2 + (isMouseDown() ? 1 : 0), height() / 2 + (isMouseDown() ? 1 : 0) - Minecraft.getInstance().font.lineHeight / 2, textFormat.get().combine(this), enabled.get() ? DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.CENTER, true);
        GuiUtils.resetTint();
    }

    protected void updateSelectedItem(boolean firstIfUnselected) {
        if (selectedItem.get().isPresent() && this.items.contains(selectedItem.get().get())) {
            this.selectedIndex.set(this.items.indexOf(selectedItem.get().get()));
        } else {
            this.selectedIndex.set(firstIfUnselected ? 0 : -1);
        }
    }    
}
