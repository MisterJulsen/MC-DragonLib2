package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Provides additional features which must be implemented by all DragonLib Containers that should be used in a DragonLib Screen.
 */
public interface IDragonLibContainer<T extends ContainerEventHandler & IDragonLibContainer<T>> extends IDragonLibWidget {

    @SuppressWarnings("unchecked")
    private T get() {
        return (T)this;
    }

    /**
     * DON'T CALL THIS METHOD BY YOURSELF, unless you are implementing a new type of {@code DLScreen}. This method is only called by {@code DLScreen} as this method iterates through all child widgets and containers by itself.
     * @param mouseX
     * @param mouseY
     */
    default void mouseSelectEvent(int mouseX, int mouseY) {        
        unselectAll();

        Optional<GuiEventListener> listener = get().getChildAtImpl(mouseX, mouseY);
        if (listener.isPresent() && listener.get() instanceof IDragonLibWidget widget) {
            widget.setMouseSelected(true);
        }
    }

    /**
     * Deselects all widgets and subwidgets. Called by {@code mouseSelectEvent}.
     */
    private void unselectAll() {
        get().children().forEach(x -> {                
            if (x instanceof IDragonLibWidget widget) {
                if (widget instanceof IDragonLibContainer container) {                    
                    container.unselectAll();
                }
                widget.setMouseSelected(false);
            }
        });
    }

    /**
     * This method must be called in the {@code getChildAt} Event to add all the new functionality.
     * @implNote
     * <pre>{@code
     *public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
     *    return getChildAtImpl((int)mouseX, (int)mouseY);
     *}
     * }</pre>
     * @param mouseX
     * @param mouseY
     * @return The targeted widget.
     */
    default Optional<GuiEventListener> getChildAtImpl(int mouseX, int mouseY) {
        Optional<GuiEventListener> menu = getContextMenuChildAtImpl(mouseX, mouseY);
        if (menu.isPresent()) {
            return menu;
        }

        ListIterator<? extends GuiEventListener> iterator = get().children().listIterator(get().children().size());
        while (iterator.hasPrevious()) {
            GuiEventListener guiEventListener = iterator.previous();
            if (!guiEventListener.isMouseOver(mouseX, mouseY)) continue;
                        
            if (guiEventListener instanceof IDragonLibContainer<?> handler) {
                Optional<GuiEventListener> child = handler.getChildAtImpl(mouseX, mouseY);
                if (child.isPresent()) {
                    return child;
                }
            }
            
            return Optional.of(guiEventListener);
        }
        return Optional.empty();
    }

    default Optional<GuiEventListener> getContextMenuChildAtImpl(int mouseX, int mouseY) {
        if (getContextMenu() != null && getContextMenu().isOpen()) {
            return Optional.of(getContextMenu());
        }

        ListIterator<? extends GuiEventListener> iterator = get().children().listIterator(get().children().size());
        while (iterator.hasPrevious()) {
            GuiEventListener guiEventListener = iterator.previous();
            if (guiEventListener instanceof IDragonLibContainer<?> widget) {
                Optional<GuiEventListener> listener = widget.getContextMenuChildAtImpl(mouseX, mouseY);                
                if (listener.isPresent()) {
                    return listener;
                }
            }

            if (guiEventListener instanceof IDragonLibWidget widget && widget.getContextMenu() != null && widget.getContextMenu().isOpen()) {
                return Optional.of(widget.getContextMenu());
            }
        }
        return Optional.empty();
    }

    /**
     * This method must be called in the {@code mouseClicked} Event to add all the new functionality. Must be called before all other widgets.
     * @implNote
     * <pre>{@code
     *public boolean mouseClicked(double mouseX, double mouseY, int button) {
     *    if (contextMenuMouseClickEvent((int)mouseX, (int)mouseY, button)) {
     *        return true;
     *    }
     *
     *    // ...
     *}
     *}</pre>
     * @param mouseX
     * @param mouseY
     * @param button
     * @return {@code true}, when a context menu could be opened.
     */
    @SuppressWarnings("unchecked")
    default boolean contextMenuMouseClickEvent(DLScreen screen, IDragonLibContainer<?> parent, int mouseX, int mouseY, int button) {

        Collection<GuiEventListener> listeners = new ArrayList<>(get().children());
        if (this instanceof GuiEventListener l) {
            listeners.add(l);
        }

        for (GuiEventListener listener : listeners) {
            if (listener instanceof IDragonLibContainer container && listener != this) {
                if (container.contextMenuMouseClickEvent(screen, container, mouseX, mouseY, button)) {
                    return true;
                }
            }
            
            if (listener instanceof IDragonLibWidget widget) {
                if (widget.contextMenuMouseClickHandler(mouseX, mouseY, button)) {
                    closeAllContextMenus(screen, widget);
                    return true;
                }
            }
        }
        
        if (parent == screen) {
            closeAllContextMenus(screen, null);
        }
        return false;
    }

    default void closeAllContextMenus(ContainerEventHandler container, IDragonLibWidget excluded) {
        container.children().forEach(x -> {
            if (x == excluded) {
                return;
            }

            if (x instanceof IDragonLibContainer dlContainer && dlContainer instanceof ContainerEventHandler childContainer) {
                closeAllContextMenus(childContainer, excluded);
            }

            if (x instanceof IDragonLibWidget widget) {
                if (widget.getContextMenu() != null) {
                    widget.getContextMenu().close();
                }
            }
        });

        if (container != excluded && container instanceof IDragonLibContainer cont && cont.getContextMenu() != null) {
            cont.getContextMenu().close();
        }
    }
}
