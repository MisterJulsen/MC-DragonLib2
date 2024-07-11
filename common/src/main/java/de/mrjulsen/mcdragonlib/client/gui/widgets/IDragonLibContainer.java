package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.data.Pair;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * Provides additional features which must be implemented by all DragonLib Containers that should be used in a DragonLib Screen.
 */
public interface IDragonLibContainer<T extends ContainerEventHandler & IDragonLibContainer<T>> extends IDragonLibWidget {

    public static final int DEFAULT_LAYER_INDEX = 0;

    @SuppressWarnings("unchecked")
    private T get() {
        return (T)this;
    }
    
    void setAllowedLayer(int index);
    int getAllowedLayer();
    void setWidgetLayerIndex(int index);
    int getWidgetLayerIndex();

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
     * Whether scrolling should be consumed by this widgte or not. If not, widgets behind this widget can be scrolled.
     * @return
     */
    boolean consumeScrolling(double mouseX, double mouseY);

    /**
     * Deselects all widgets and subwidgets. Called by {@code mouseSelectEvent}.
     */
    private void unselectAll() {
        childrenLayered().forEach(x -> {                
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

        ListIterator<? extends GuiEventListener> iterator = childrenLayered().listIterator(childrenLayered().size());
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

        ListIterator<? extends GuiEventListener> iterator = childrenLayered().listIterator(childrenLayered().size());
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

    default List<GuiEventListener> getWidgetsReversed() {
        List<GuiEventListener> listeners = new LinkedList<>(childrenLayered());
        Collections.reverse(listeners);
        if (this instanceof GuiEventListener l) {
            listeners.add(l);
        }
        return listeners;
    }

    default List<? extends GuiEventListener> childrenLayered() {
        return get().children().stream().filter(x -> 
            ((x instanceof IDragonLibContainer container && container.getWidgetLayerIndex() >= this.getAllowedLayer()) || this.getAllowedLayer() == 0) &&
            ((x instanceof IDragonLibWidget wdgt && wdgt.visible()) || (x instanceof AbstractWidget absw && absw.visible))
        ).toList();
    }

    /**
     * Checks if the widgets are inside the bounds of the container before rendering.
     * @return {@code true} if the check should be enabled.
     */
    default boolean checkWidgetBounds() {
        return false;
    }

    default Pair<Double, Double> checkWidgetBoundsOffset() {
        return Pair.of(0D, 0D);
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
    default boolean contextMenuMouseClickEvent(DLScreen screen, IDragonLibContainer<?> parent, int mouseX, int mouseY, int xOffset, int yOffset, int button, GuiAreaDefinition openingBounds) {
        
        List<GuiEventListener> listeners = getWidgetsReversed();

        for (GuiEventListener listener : listeners) {
            if (listener instanceof IDragonLibContainer container && listener != this) {
                if (container.contextMenuMouseClickEvent(screen, container, mouseX, mouseY, xOffset, yOffset, button, openingBounds)) {
                    return true;
                }
            }
            
            if (listener instanceof IDragonLibWidget widget) {
                if (widget.contextMenuMouseClickHandler(mouseX, mouseY, button, xOffset, yOffset, openingBounds)) {
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

    default boolean containerMouseScrolled(double mouseX, double mouseY, double delta) {

        List<GuiEventListener> listeners = getWidgetsReversed();

        for (GuiEventListener listener : listeners) {
            if (listener instanceof IDragonLibContainer container && listener != this && listener.isMouseOver(mouseX, mouseY) && container.containerMouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
            
            if (listener instanceof IDragonLibWidget widget && ((listener instanceof IExtendedAreaWidget ext && ext.isInArea(mouseX, mouseY)) || widget.isMouseSelected()) && listener.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }

        return consumeScrolling(mouseX, mouseY);
    }


    //#region HACKY FIXES

    /* Some hacky fixes, but they work. Soo... here be dragons!
     *  COPY OF: ContainerEventHandler 
     *  Mixin cannot be used because it will cause problems with other mods (e.g. YACL)
     *  Changes made in this code (only applies to DragonLib):
     *      - children() --> childrenLayered()
     */

    default ComponentPath dragonlib$nextFocusPath(FocusNavigationEvent event) {
        GuiEventListener guiEventListener = get().getFocused();
        if (guiEventListener != null) {
            ComponentPath componentPath = guiEventListener.nextFocusPath(event);
            if (componentPath != null) {
                return ComponentPath.path(get(), componentPath);
            }
        }

        if (event instanceof FocusNavigationEvent.TabNavigation tabNavigation) {
            return this.dragonlib$handleTabNavigation(tabNavigation);
        } else if (event instanceof FocusNavigationEvent.ArrowNavigation arrowNavigation) {
            return this.dragonlib$handleArrowNavigation(arrowNavigation);
        } else {
            return null;
        }
    }

    
    private ComponentPath dragonlib$handleTabNavigation(FocusNavigationEvent.TabNavigation pTabNavigation) {
        boolean flag = pTabNavigation.forward();
        GuiEventListener guieventlistener = get().getFocused();
        List<? extends GuiEventListener> list = new ArrayList<>(childrenLayered());
        Collections.sort(list, Comparator.comparingInt((p_289623_) -> {
            return p_289623_.getTabOrderGroup();
        }));
        int j = list.indexOf(guieventlistener);
        int i;
        if (guieventlistener != null && j >= 0) {
            i = j + (flag ? 1 : 0);
        } else if (flag) {
            i = 0;
        } else {
            i = list.size();
        }

        ListIterator<? extends GuiEventListener> listiterator = list.listIterator(i);
        BooleanSupplier booleansupplier = flag ? listiterator::hasNext : listiterator::hasPrevious;
        Supplier<? extends GuiEventListener> supplier = flag ? listiterator::next : listiterator::previous;

        while (booleansupplier.getAsBoolean()) {
            GuiEventListener guieventlistener1 = supplier.get();
            ComponentPath componentpath = guieventlistener1.nextFocusPath(pTabNavigation);
            if (componentpath != null) {
                return ComponentPath.path(get(), componentpath);
            }
        }

        return null;
    }

    private ComponentPath dragonlib$handleArrowNavigation(FocusNavigationEvent.ArrowNavigation pArrowNavigation) {
        GuiEventListener guieventlistener = get().getFocused();
        if (guieventlistener == null) {
            ScreenDirection screendirection = pArrowNavigation.direction();
            ScreenRectangle screenrectangle1 = get().getRectangle().getBorder(screendirection.getOpposite());
            return ComponentPath.path(get(), this.nextFocusPathInDirection(screenrectangle1, screendirection,
                    (GuiEventListener) null, pArrowNavigation));
        } else {
            ScreenRectangle screenrectangle = guieventlistener.getRectangle();
            return ComponentPath.path(get(), this.nextFocusPathInDirection(screenrectangle, pArrowNavigation.direction(),
                    guieventlistener, pArrowNavigation));
        }
    }

    private ComponentPath nextFocusPathInDirection(ScreenRectangle pRectangle, ScreenDirection pDirection, @Nullable GuiEventListener pListener, FocusNavigationEvent pEvent) {
        ScreenAxis screenaxis = pDirection.getAxis();
        ScreenAxis screenaxis1 = screenaxis.orthogonal();
        ScreenDirection screendirection = screenaxis1.getPositive();
        int i = pRectangle.getBoundInDirection(pDirection.getOpposite());
        List<GuiEventListener> list = new ArrayList<>();

        for (GuiEventListener guieventlistener : childrenLayered()) {
            if (guieventlistener != pListener) {
                ScreenRectangle screenrectangle = guieventlistener.getRectangle();
                if (screenrectangle.overlapsInAxis(pRectangle, screenaxis1)) {
                    int j = screenrectangle.getBoundInDirection(pDirection.getOpposite());
                    if (pDirection.isAfter(j, i)) {
                        list.add(guieventlistener);
                    } else if (j == i && pDirection.isAfter(screenrectangle.getBoundInDirection(pDirection),
                            pRectangle.getBoundInDirection(pDirection))) {
                        list.add(guieventlistener);
                    }
                }
            }
        }

        Comparator<GuiEventListener> comparator = Comparator.comparing((p_264674_) -> {
            return p_264674_.getRectangle().getBoundInDirection(pDirection.getOpposite());
        }, pDirection.coordinateValueComparator());
        Comparator<GuiEventListener> comparator1 = Comparator.comparing((p_264676_) -> {
            return p_264676_.getRectangle().getBoundInDirection(screendirection.getOpposite());
        }, screendirection.coordinateValueComparator());
        list.sort(comparator.thenComparing(comparator1));

        for (GuiEventListener guieventlistener1 : list) {
            ComponentPath componentpath = guieventlistener1.nextFocusPath(pEvent);
            if (componentpath != null) {
                return componentpath;
            }
        }

        return this.nextFocusPathVaguelyInDirection(pRectangle, pDirection, pListener, pEvent);
    }

    private ComponentPath nextFocusPathVaguelyInDirection(ScreenRectangle pRectangle, ScreenDirection pDirection, @Nullable GuiEventListener pListener, FocusNavigationEvent pEvent) {
        ScreenAxis screenaxis = pDirection.getAxis();
        ScreenAxis screenaxis1 = screenaxis.orthogonal();
        List<Pair<GuiEventListener, Long>> list = new ArrayList<>();
        ScreenPosition screenposition = ScreenPosition.of(screenaxis, pRectangle.getBoundInDirection(pDirection),
                pRectangle.getCenterInAxis(screenaxis1));

        for (GuiEventListener guieventlistener : childrenLayered()) {
            if (guieventlistener != pListener) {
                ScreenRectangle screenrectangle = guieventlistener.getRectangle();
                ScreenPosition screenposition1 = ScreenPosition.of(screenaxis,
                        screenrectangle.getBoundInDirection(pDirection.getOpposite()),
                        screenrectangle.getCenterInAxis(screenaxis1));
                if (pDirection.isAfter(screenposition1.getCoordinate(screenaxis),
                        screenposition.getCoordinate(screenaxis))) {
                    long i = Vector2i.distanceSquared(screenposition.x(), screenposition.y(), screenposition1.x(),
                            screenposition1.y());
                    list.add(Pair.of(guieventlistener, i));
                }
            }
        }

        list.sort(Comparator.comparingDouble(Pair::getSecond));

        for (Pair<GuiEventListener, Long> pair : list) {
            ComponentPath componentpath = pair.getFirst().nextFocusPath(pEvent);
            if (componentpath != null) {
                return componentpath;
            }
        }

        return null;
    }
    //#endregion
}
