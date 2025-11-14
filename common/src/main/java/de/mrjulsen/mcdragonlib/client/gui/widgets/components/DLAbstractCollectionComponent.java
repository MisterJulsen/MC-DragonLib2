package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.function.Function;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEventListener;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

@SupportsEvents({
    DLAbstractCollectionComponent.ListLayoutChangedEvent.class
})
public abstract class DLAbstractCollectionComponent<T, I extends DLAbstractCollectionComponent.DLCollectionItem<T, ?>> extends DLGuiComponent {
    
    public record ListLayoutChangedEvent() implements IEvent {}


    protected static abstract class DLCollectionItem<T, L extends DLAbstractCollectionComponent<T, ?>> extends DLGuiComponent {
    
        protected final L collectionComponentRef;
        protected final T item;

        protected DLCollectionItem(L collectionComponentRef, T item, int w, int h) {
            super(0, 0, w, h);
            inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
            this.collectionComponentRef = collectionComponentRef;
            this.item = item;
        }
        
        @Override
        public final void setHeight(double height) {
            if (!collectionComponentRef.itemResizeAllowed.get()) {
                throw new IllegalStateException("Cannot change the size of list items after they have been created.");
            }
            super.setHeight(height);
            collectionComponentRef.layoutComponentsInternal();
        }
        
        @Override
        public final void setWidth(double width) {
            if (!collectionComponentRef.itemResizeAllowed.get()) {
                throw new IllegalStateException("Cannot change the size of list items after they have been created.");
            }
            super.setWidth(width);
            collectionComponentRef.layoutComponentsInternal();
        }

        @Override
        public final void setX(double x) {            
            throw new IllegalStateException("Cannot change the position of list items after they have been created.");
        }

        @Override
        public final void setY(double y) {            
            throw new IllegalStateException("Cannot change the position of list items after they have been created.");
        }

        void setCollectionX(int x) {
            super.setX(x);
        }

        void setCollectionY(int y) {
            super.setY(y);
        }

        void setCollectionW(int w) {
            super.setWidth(w);
        }

        void setCollectionH(int h) {
            super.setHeight(h);
        }
    }



    public final ListProperty<T> items = new ListProperty<T>()
        .withAfterPropertyChangedCallback((o, val) -> {
            createComponents();
            layoutComponentsInternal();
        });

    public final Property<Function<T, I>> itemBuilder = new Property<Function<T, I>>(this::defaultItemBuilder)
        .withAfterPropertyChangedCallback((a, b) -> {
            createComponents();
            layoutComponentsInternal();
        });

    public final BooleanProperty itemResizeAllowed = new BooleanProperty(false, false);


    protected final DLPanel contentPanel;

    public DLAbstractCollectionComponent(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.contentPanel = new DLPanel(0, 0, width(), height());
        contentPanel.inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
        this.contentPanel.anchor.set(EAlign.values());
        addComponent(contentPanel);

        final IEventListener<DLGuiComponent, DLGuiStandardEvents.ComponentPosAndSizeChanged> resizeEvent = (src, event) -> {
            layoutComponentsInternal();
            return false;
        };

        addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, resizeEvent);
        contentPanel.addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, resizeEvent);
    }

    protected void createComponents() {
        contentPanel.clearComponents();
        for (T item : items.get()) {
            I listItem = itemBuilder.get().apply(item);
            contentPanel.addComponent(listItem);
        }
    }

    protected final void layoutComponentsInternal() {
        layoutComponents();
        invokeEvent(this, new ListLayoutChangedEvent());
    }

    protected abstract void layoutComponents();
    protected abstract I defaultItemBuilder(T item);

    

    protected final void setItemX(I item, int x) {
        item.setCollectionX(x);
    }
    protected final void setItemY(I item, int y) {
        item.setCollectionY(y);
    }
    protected final void setItemWidth(I item, int width) {
        item.setCollectionW(width);
    }
    protected final void setItemHeight(I item, int height) {
        item.setCollectionH(height);
    }
}
