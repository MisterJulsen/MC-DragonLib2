package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.function.Function;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.LayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

@SupportsEvents({
    DLAbstractCollectionComponent.ListLayoutChangedEvent.class,
    DLAbstractCollectionComponent.FilterChangedEvent.class
})
public abstract class DLAbstractCollectionComponent<T, I extends DLAbstractCollectionComponent.DLCollectionItem<T, ?>> extends DLGuiComponent {
    
    public record ListLayoutChangedEvent(LayoutResult layoutResult) implements IEvent {}
    public record FilterChangedEvent() implements IEvent {}


    public static abstract class DLCollectionItem<T, L extends DLAbstractCollectionComponent<T, ?>> extends DLGuiComponent {
    
        protected final L collectionComponentRef;
        protected final T item;

        protected DLCollectionItem(L collectionComponentRef, T item, int w, int h) {
            super(0, 0, w, h);
            inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
            this.collectionComponentRef = collectionComponentRef;
            this.item = item;
        }

        public T getItem() {
            return item;
        }
    }



    public final ListProperty<T> items = new ListProperty<T>()
        .withAfterPropertyChangedCallback((o, val) -> {
            createComponents();
        });

    public final Property<Function<T, I>> itemBuilder = new Property<Function<T, I>>(this::defaultItemBuilder)
        .withAfterPropertyChangedCallback((a, b) -> {
            createComponents();
        });

    public final BooleanProperty itemResizeAllowed = new BooleanProperty(false, false);
        
    public final Property<Predicate<T>> filter = new Property<Predicate<T>>((item) -> true)
        .withAfterPropertyChangedCallback((a, b) -> {
            createComponents();
            invokeEvent(this, new FilterChangedEvent());
        });
        


    protected final DLPanel contentPanel;

    public DLAbstractCollectionComponent(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.contentPanel = new DLPanel(0, 0, width(), height());
        this.contentPanel.inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
        this.contentPanel.anchor.set(EAlign.values());
        FlowLayout layout = new FlowLayout();
        layout.wrap.set(false);
        layout.fillCrossAxis.set(true);
        layout.flowDirection.set(Direction.VERTICAL);
        this.contentPanel.layout.set(layout);
        this.contentPanel.addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            invokeEvent(contentPanel, new ListLayoutChangedEvent(e.layoutResult()));
            return false;
        });
        addComponent(contentPanel);
    }

    protected void createComponents() {
        contentPanel.clearComponents();
        for (T item : items.get()) {
            if (!filter.get().test(item)) {
                continue;
            }
            I listItem = itemBuilder.get().apply(item);
            contentPanel.addComponent(listItem);
        }
    }

    protected abstract I defaultItemBuilder(T item);
}
