package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;

@Mixin(value = Screen.class)
public interface ScreenMixin {    
    
    @Invoker("createTabEvent")
    FocusNavigationEvent.TabNavigation dragonlib$createTabEvent();

    @Invoker("createArrowEvent")
    FocusNavigationEvent.ArrowNavigation dragonlib$createArrowEvent(ScreenDirection direction);

    @Invoker("clearFocus")
    void dragonlib$clearFocus();

    // ALTERNATE SOLUTION
    // Unused, because it may also cause problems with other mods...
    
    /*    
    default ContainerEventHandler self() {
        return (ContainerEventHandler)this;
    }

    @SuppressWarnings("unchecked")
    default List<? extends GuiEventListener> getChildren() {
        return this instanceof IDragonLibContainer container ? container.childrenLayered() : self().children();
    }

    @Surrogate(method = "nextFocusPath", at = @At(value = "HEAD"))
    default ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (this instanceof IDragonLibContainer) {
            GuiEventListener guiEventListener = self().getFocused();
            if (guiEventListener != null) {
                ComponentPath componentPath = guiEventListener.nextFocusPath(event);
                if (componentPath != null) {
                    return ComponentPath.path(self(), componentPath);
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
        return ((ContainerEventHandler)this).nextFocusPath(event);
    }
    */
}
