package de.mrjulsen.mcdragonlib.gui.v2.widget.contextmenu;

import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.input.Hotkey;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ContextMenuItem extends AbstractContextMenuItem {

    public enum CheckType {
        CHECKMARK,
        RADIO_BUTTON
    }

    private Component text = TextUtils.empty();
    private DLSprite icon = DLSprite.empty();
    private Hotkey hotkey = null;
    private String hotkeyId = null;
    private BooleanSupplier checked = () -> false;
    private CheckType checkType = CheckType.CHECKMARK;

    protected ContextMenuItem() {
    }

    public Component getText() {
        return text;
    }

    public DLSprite getIcon() {
        return icon;
    }

    public Hotkey getHotkey() {
        return hotkey;
    }

    public String getHotkeyId() {
        return hotkeyId;
    }

    public BooleanSupplier getChecked() {
        return checked;
    }

    public CheckType getCheckType() {
        return checkType;
    }


    public static class Builder {
        private BooleanSupplier enabled = () -> true;
        private ClickAction clickAction = null;
        private Component text = TextUtils.empty();
        private DLSprite icon = null;
        private Hotkey hotkey = null;
        private String hotkeyId = null;
        private boolean closeOnClick = true;
        private BooleanSupplier checked = () -> false;
        private CheckType checkType = CheckType.CHECKMARK;
        private ContextMenuItem[] subItems = new ContextMenuItem[0];

        public Builder clickAction(ClickAction clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        public Builder enabled(BooleanSupplier enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder text(Component text) {
            this.text = text;
            return this;
        }

        public Builder icon(DLSprite icon) {
            this.icon = icon;
            return this;
        }

        public Builder hotkey(Hotkey hotkey) {
            this.hotkey = hotkey;
            return this;
        }

        public Builder hotkey(String hotkeyId) {
            this.hotkeyId = hotkeyId;
            return this;
        }

        public Builder closeOnClick(boolean closeOnClick) {
            this.closeOnClick = closeOnClick;
            return this;
        }

        public Builder subItems(ContextMenuItem... items) {
            this.subItems = items;
            return this;
        }

        public Builder subItems(Collection<ContextMenuItem> items) {
            this.subItems = items.toArray(ContextMenuItem[]::new);
            return this;
        }

        public Builder checked(BooleanSupplier checked) {
            this.checked = checked;
            return this;
        }

        public Builder checkType(CheckType checkType) {
            this.checkType = checkType;
            return this;
        }

        public ContextMenuItem build() {
            ContextMenuItem item = new ContextMenuItem();
            item.enabled = enabled;
            item.clickAction = clickAction;
            item.text = Objects.requireNonNullElse(text, TextUtils.empty());
            item.icon = Objects.requireNonNullElse(icon, DLSprite.empty());
            item.hotkey = hotkey;
            item.hotkeyId = hotkeyId;
            item.closeOnClick = closeOnClick;
            item.subItems = subItems;
            item.checked = Objects.requireNonNullElse(checked, () -> false);
            item.checkType = Objects.requireNonNullElse(checkType, CheckType.CHECKMARK);
            return item;
        }
    }
}
