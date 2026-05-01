package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.action;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class InteractiveElement {

    public static interface IInteraction {
        boolean highlightOnHover();
    }

    public record ClickAction(String actionName, String value) implements IInteraction {

        public static final String OPEN_URL = "open_url";
        public static final String COPY_TO_CLIPBAORD = "copy_to_clipboard";
        @Override
        public boolean highlightOnHover() {
            return true;
        }
    }

    public record HoverAction(Component text) implements IInteraction {
        @Override
        public boolean highlightOnHover() {
            return false;
        }
    }

    public int start;
    public int end;

    public Map<Class<? extends IInteraction>, IInteraction> interactions = new HashMap<>();

    public InteractiveElement(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public <T extends IInteraction> InteractiveElement withAction(T action) {
        interactions.put(action.getClass(), action);
        return this;
    }

    public <T extends IInteraction> boolean hasActionFromType(Class<T> type) {
        return interactions.containsKey(type);
    }

    public <T extends IInteraction> T getActionFromType(Class<T> type) {  
        return (T)interactions.get(type);
    }

    public boolean shouldHighlightOnHover() {
        for (IInteraction el : interactions.values()) {
            if (el.highlightOnHover()) return true;
        }
        return false;
    }

    public void copyFrom(InteractiveElement other) {
        interactions.putAll(other.interactions);
    }

    public boolean contains(int codePointIndex) {
        return codePointIndex >= start && codePointIndex < end;
    }

    public boolean intersects(int otherStart, int otherEnd) {
        return Math.max(this.start, otherStart) < Math.min(this.end, otherEnd);
    }

    public int length() {
        return end - start;
    }
}
