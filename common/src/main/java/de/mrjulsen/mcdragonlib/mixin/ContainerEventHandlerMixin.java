package de.mrjulsen.mcdragonlib.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.mojang.datafixers.util.Pair;

import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibContainer;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin<T extends ContainerEventHandler & IDragonLibContainer<T>> {

    default ContainerEventHandler getVanilla() {
        return (ContainerEventHandler)this;
    }

    @SuppressWarnings("unchecked")
    default List<? extends GuiEventListener> getChildren() {
        return this instanceof IDragonLibContainer container ? container.childrenLayered() : getVanilla().children();
    }
    
    @Overwrite
    default ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation pTabNavigation) {
        boolean flag = pTabNavigation.forward();
        GuiEventListener guieventlistener = getVanilla().getFocused();
        List<? extends GuiEventListener> list = new ArrayList<>(getChildren());
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
                return ComponentPath.path(getVanilla(), componentpath);
            }
        }

        return null;
    }

    @Overwrite
    default ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation pArrowNavigation) {
        GuiEventListener guieventlistener = getVanilla().getFocused();
        if (guieventlistener == null) {
            ScreenDirection screendirection = pArrowNavigation.direction();
            ScreenRectangle screenrectangle1 = getVanilla().getRectangle().getBorder(screendirection.getOpposite());
            return ComponentPath.path(getVanilla(), this.nextFocusPathInDirection(screenrectangle1, screendirection,
                    (GuiEventListener) null, pArrowNavigation));
        } else {
            ScreenRectangle screenrectangle = guieventlistener.getRectangle();
            return ComponentPath.path(getVanilla(), this.nextFocusPathInDirection(screenrectangle, pArrowNavigation.direction(),
                    guieventlistener, pArrowNavigation));
        }
    }

    @Overwrite
    default ComponentPath nextFocusPathInDirection(ScreenRectangle pRectangle, ScreenDirection pDirection, @Nullable GuiEventListener pListener, FocusNavigationEvent pEvent) {
        ScreenAxis screenaxis = pDirection.getAxis();
        ScreenAxis screenaxis1 = screenaxis.orthogonal();
        ScreenDirection screendirection = screenaxis1.getPositive();
        int i = pRectangle.getBoundInDirection(pDirection.getOpposite());
        List<GuiEventListener> list = new ArrayList<>();

        for (GuiEventListener guieventlistener : getChildren()) {
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

    @Overwrite
    default ComponentPath nextFocusPathVaguelyInDirection(ScreenRectangle pRectangle, ScreenDirection pDirection, @Nullable GuiEventListener pListener, FocusNavigationEvent pEvent) {
        ScreenAxis screenaxis = pDirection.getAxis();
        ScreenAxis screenaxis1 = screenaxis.orthogonal();
        List<Pair<GuiEventListener, Long>> list = new ArrayList<>();
        ScreenPosition screenposition = ScreenPosition.of(screenaxis, pRectangle.getBoundInDirection(pDirection),
                pRectangle.getCenterInAxis(screenaxis1));

        for (GuiEventListener guieventlistener : getChildren()) {
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
}
