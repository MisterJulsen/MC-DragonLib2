package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.Collection;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Provides additional features which must be implemented by all DragonLib Components that should be used in a DragonLib Screen.
 */
public interface IDragonLibWidget {

    void setVisible(boolean b);
    boolean isVisible();
    void setActive(boolean b);
    boolean isActive();
    /**
     * Use this value if you don't want the context menu to open by right-clicking.
     */
    public static final int NO_CONTEXT_MENU_BUTTON = -1;

    /**
     * Called when the focus of this widget changes.
     * @param focus The new focus.
     */
    void onFocusChangeEvent(boolean focus);      
    
    /**
     * The lowest render layer that is rendered before all other layers.
     * @param graphics
     * @param mouseX
     * @param mouseY
     * @param partialTicks
     */
    default void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {}
    /**
     * The default render layer. Recommended for default rendering.
     * @param graphics
     * @param mouseX
     * @param mouseY
     * @param partialTicks
     */
    default void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {}
    /**
     * The upper render layer. Intended to be used for context menus or tooltips.
     * @param graphics
     * @param mouseX
     * @param mouseY
     * @param partialTicks
     */
    default void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (getContextMenu() != null) {
            getContextMenu().renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * @return The context menu assigned to this widget or {@code null} if there is no context menu.
     */
    DLContextMenu getContextMenu();

    /**
     * Sets the context menu for this widget.
     * @param menu
     */
    void setMenu(DLContextMenu menu);

    /**
     * Similar to {@code isHovered()}, but it only returns {@code true} for the top most widget that is hovered.
     */
    boolean isMouseSelected();

    void setMouseSelected(boolean selected);

    int getX();
    int getY();

    /**
     * The button that must be used to open the context menu. Use {@code NO_CONTEXT_MENU_BUTTON} to prevent the context menu from opening by user inputs.
     * @return
     */
    default int getContextMenuOpenButton() {
        return GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }
    
    default boolean contextMenuMouseClickHandler(int mouseX, int mouseY, int button, int xOffset, int yOffset, GuiAreaDefinition openingBounds) {
        if (getContextMenu() == null) {
            return false;
        }

        boolean b = getContextMenu().mouseClicked(mouseX, mouseY, button);

        if (b) {
            return true;
        }

        if (getContextMenuOpenButton() != NO_CONTEXT_MENU_BUTTON && button == getContextMenuOpenButton() && (openingBounds == null || openingBounds.isInBounds(mouseX, mouseY))) {
            return getContextMenu().open((int)mouseX, (int)mouseY + yOffset, (int)mouseX, (int)mouseY);
        }

        return false;
    }

    default <T extends ContainerEventHandler, S extends IDragonLibWidget> void closeAllContextMenussssss(Collection<GuiEventListener> listeners, T self, S selected) {
        listeners.stream().filter(x -> x instanceof IDragonLibWidget w && x != selected && w.getContextMenu() != null).forEach(x -> {
            ((IDragonLibWidget)x).getContextMenu().close();
        });        
    }
}
