package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class DLCycleButton<T> extends DLAbstractButton {

    public static final int INVALID = -1;

    public record TextFormatChangedEvent() implements IEvent {}
    public record SelectedItemChanged<T>(Optional<T> item, int index) implements IEvent {}

    public final ListProperty<T> items = new ListProperty<T>()
            .withUpdateCallback((val, operation) -> updateSelectedItem(operation == ListProperty.ListOperation.ADD));
    public final Property<Optional<T>> selectedItem = new Property<>(Optional.empty());
    public final NumberProperty<Integer> selectedIndex = new NumberProperty<>(INVALID, INVALID, Integer.MAX_VALUE);
    public final BooleanProperty cycling = new BooleanProperty(true);

    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final Property<Function<Optional<T>, Component>> textFormat = new Property<>((item) -> TextUtils.text(text.get().getString()).append(": ").append(item.map(Object::toString).orElse("")));
    public final Property<Function<Optional<T>, DLSprite>> icon = new Property<>((item) -> null);

    public final EventListenerId defaultClickActionEventId;

    public DLCycleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.textFormat.addChangeListener((o, n) -> {
            invalidateLayout();
            invokeEvent(this, new TextFormatChangedEvent());
        });

        this.selectedIndex.addChangeListener((o, n) -> {
            if (o.equals(n)) {
                return;
            }
            int idx = n;
            if (n < 0 || n >= items.size()) {
                idx = INVALID;
            }
            Optional<T> item = Optional.ofNullable(idx == INVALID ? null : items.get(n));
            selectedItem.set(item);
            invokeEvent(this, new DLCycleButton.SelectedItemChanged<>(item, n));
        });

        this.selectedItem.addChangeListener((o, n) -> {
            if (!Objects.equals(o.orElse(null), n.orElse(null))) {
                selectedIndex.set(n.map(items::indexOf).orElse(INVALID));
            }
        });


        defaultClickActionEventId = addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
            if (items.isEmpty()) {
                return;
            }

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
        }, GuiEvent.PRIORITY_LOW);
    }

    public static DLCycleButton<Boolean> createOnOffButton(int x, int y, int w, int h) {
        return createBooleanButton(x, y, w, h, CommonComponents.OPTION_ON, CommonComponents.OPTION_OFF);
    }

    public static DLCycleButton<Boolean> createYesNoButton(int x, int y, int w, int h) {
        return createBooleanButton(x, y, w, h, CommonComponents.GUI_YES, CommonComponents.GUI_NO);
    }

    public static DLCycleButton<Boolean> createBooleanButton(int x, int y, int w, int h, Component onText, Component offText) {
        DLCycleButton<Boolean> btn = new DLCycleButton<>(x, y, w, h);
        List<Boolean> bools = List.of(false, true);
        btn.items.addAll(bools);
        AtomicBoolean modification = new AtomicBoolean(false);
        btn.items.withUpdateCallback((val, operation) -> {
            if (modification.get()) return;
            modification.set(true);
            DragonLib.LOGGER.warn("Cannot change the items of an On-Off-Cycle Button.");
            int idx = btn.selectedIndex.get();
            btn.items.setAll(bools);
            btn.selectedIndex.set(idx);
            modification.set(false);
        });
        btn.selectedIndex.set(0);
        btn.textFormat.set((item) -> TextUtils.text(btn.text.get().getString()).append(": ").append(item.map(b -> b ? onText : offText).orElse(TextUtils.empty())));
        return btn;
    }

    protected void updateSelectedItem(boolean firstIfUnselected) {
        if (selectedItem.get().isPresent() && this.items.contains(selectedItem.get().get())) {
            this.selectedIndex.set(this.items.indexOf(selectedItem.get().get()));
        } else {
            this.selectedIndex.set(firstIfUnselected ? 0 : INVALID);
        }
    }

    @Override
    protected Size measureContent(double availableWidth, double availableHeight) {
        double width = 0;
        double height = 0;
        Component text;
        Optional<T> item;

        for (T itm : items) {
            item = Optional.ofNullable(itm);
            text = textFormat.get().apply(item);
            width = Math.max(width, TextUtils.textWidth(TextUtils.font(), text) + TEXT_PADDING * 2);
            DLSprite sprite = icon.get().apply(item);
            if (sprite != null && !sprite.isEmpty()) {
                width += sprite.getWidth() + ICON_GAP;
                height = Math.max(height, sprite.getHeight());
            }
        }

        return Size.of(width, Math.max(DEFAULT_BUTTON_HEIGHT, height));
    }

    @Override
    protected DLSprite getIcon() {
        return icon.get().apply(selectedItem.get());
    }

    @Override
    protected Component getText() {
        return textFormat.get().apply(selectedItem.get());
    }
}
