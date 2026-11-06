package de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu;

import java.util.function.BooleanSupplier;

public abstract class AbstractContextMenuItem {

    @FunctionalInterface
    public interface ClickAction {
        ClickAction NONE = (menu, mouseX, mouseY, button) -> {};
        void click(DLContextMenu menu, double mouseX, double mouseY, int button);
    }

    protected BooleanSupplier enabled = () -> true;
    protected ClickAction clickAction = ClickAction.NONE;
    protected boolean closeOnClick = true;
    protected AbstractContextMenuItem[] subItems = new AbstractContextMenuItem[0];

    protected AbstractContextMenuItem() {
    }

    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    public boolean shouldCloseOnClick() {
        return closeOnClick;
    }

    public AbstractContextMenuItem[] getSubItems() {
        return subItems;
    }
}
