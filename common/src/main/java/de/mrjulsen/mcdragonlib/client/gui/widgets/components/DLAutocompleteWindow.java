package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Point;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.VirtualProperty;

public class DLAutocompleteWindow<T> extends DLWindow {

    private final DLAutocompleteListBox<T> listBox;
    private final DLRichTextEditBox parentComponent;
    boolean supressTextUpdate;
    
    private final EventListenerId keyEventId;
    private final EventListenerId focusEventId;

    public final VirtualProperty<Predicate<T>> filter;
    public final VirtualProperty<List<T>> suggestions;

    public DLAutocompleteWindow(DLWindowManager manager, DLRichTextEditBox parentComponent) {
        super(manager);
        this.parentComponent = parentComponent;
        Point pos = parentComponent.toScreenCoordinates();
        setPosition(pos.x(), pos.y() + parentComponent.height() + 2);
        setSize(parentComponent.width(), 120);
        topLevel.set(true);
        
        this.listBox = addComponent(new DLAutocompleteListBox<>(this, parentComponent, 1, 1, width() - 2, height() - 2));
        listBox.anchor.set(EAlign.values());

        this.filter = new VirtualProperty<Predicate<T>>(item -> true,
            () -> listBox.filter.get(),
            (func) -> listBox.filter.set(func)
        );
        
        this.suggestions = new VirtualProperty<List<T>>(List.of(),
            () -> listBox.items.get(),
            (items) -> listBox.items.set(items)
        ).withAfterPropertyChangedCallback((o, n) -> {
            this.listBox.selectedItems.set(List.of(listBox.items.get().get(0)));
        });

        listBox.addEventListener(DLAbstractCollectionComponent.FilterChangedEvent.class, (s, e) -> {
            this.listBox.selectFirst();
            return false;
        });

        keyEventId = parentComponent.addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (s, e) -> {
            switch (e.keyCode()) {
                case GLFW.GLFW_KEY_UP:
                    changeIndex(-1);
                    return true;
                case GLFW.GLFW_KEY_DOWN:
                    changeIndex(1);
                    return true;
                case GLFW.GLFW_KEY_TAB:
                case GLFW.GLFW_KEY_ENTER:
                    supressTextUpdate = true;
                    if (!listBox.selectedItems.get().isEmpty()) {
                        parentComponent.text.get().set(listBox.textFormat.get().apply(listBox.selectedItems.get().get(0)).getString());
                    }
                    getWindowManager().closeWindow(this);
                    supressTextUpdate = false;
                    return true;
            };
            return false;
        }, 100);
        
        focusEventId = parentComponent.addEventListener(DLGuiStandardEvents.FocusChangedEvent.class, (s, e) -> {
            if (getWindowManager().getFocusedWindow() != this && !e.focus()) {
                getWindowManager().closeWindow(this);
            }
            return false;
        });
        
        addEventListener(DLWindow.WindowFocusEvent.class, (src, e) -> {
            getWindowManager().invokeLater(() -> {
                if (!e.focus() && !parentComponent.isFocused()) {
                    getWindowManager().closeWindow(this);
                }
            });
            return false;
        });
    }

    private void changeIndex(int direction) {
        if (!this.listBox.selectedItems.get().isEmpty()) {
            int currentIndex = this.listBox.visibleCurrentIndex();
            int index = MathUtils.clamp(currentIndex + direction, 0, this.listBox.visibleItems.size() - 1);
            this.listBox.selectByVisibleIndex(index);
        }
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0xDD000000), DLColor.fromInt(0xFFDBDBDB));
    }

    @Override
    public void close() throws Exception {
        this.parentComponent.removeEventListener(DLGuiStandardEvents.KeyPressEvent.class, keyEventId);
        this.parentComponent.removeEventListener(DLGuiStandardEvents.FocusChangedEvent.class, focusEventId);
        super.close();
    }

    public boolean supressTextUpdate() {
        return supressTextUpdate;
    }
    
}
