package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.events.IEventListener;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;

public abstract class DLAbstractDataView<T, I extends DLAbstractDataView.DLDataViewItem<T, ?>> extends DLGuiComponent {

    public static record DataSlot(String name, Component displayName, double size, SizeMode mode) {}
    public static record DataSlotComponent(String name, DLGuiComponent component) {}
    public static enum SizeMode { FIXED, PERCENTAGE; }

    public final ListProperty<T> items = new ListProperty<T>()
        .withAfterPropertyChangedCallback((o, val) -> {
            createComponents();
            layoutComponents();
        });

    public final ListProperty<DataSlot> dataSlots = new ListProperty<DataSlot>()
        .withAfterPropertyChangedCallback((o, val) -> {
            createComponents();
            layoutComponents();
        });

    public final Property<Function<T, I>> itemBuilder = new Property<Function<T, I>>(this::defaultItemBuilder)
        .withAfterPropertyChangedCallback((a, b) -> {
            createComponents();
            layoutComponents();
        });

    public final BooleanProperty itemResizeAllowed = new BooleanProperty(false, false);


    protected final DLPanel contentPanel;

    public DLAbstractDataView(int x, int y, int w, int h) {
        super(x, y, w, h);

        this.contentPanel = new DLPanel(0, 0, width(), height());
        this.contentPanel.inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
        this.contentPanel.anchor.set(EAlign.values());
        addComponent(contentPanel);

        final IEventListener<DLGuiComponent, DLGuiStandardEvents.ComponentPosAndSizeChanged> resizeEvent = (src, event) -> {
            layoutComponents();
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
    

    protected static abstract class DLDataViewItem<T, L extends DLAbstractDataView<T, ?>> extends DLGuiComponent {        
    
        protected final L collectionComponentRef;
        protected final T item;
        
        protected final DLPanel contentPanel;

        public final ListProperty<DataSlotComponent> subComponents = new ListProperty<>();

        protected DLDataViewItem(L collectionComponentRef, T item) {
            super(0, 0, 1, 1);
            inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
            this.collectionComponentRef = collectionComponentRef;
            this.item = item;            

            this.contentPanel = new DLPanel(0, 0, width(), height());
            this.contentPanel.inputConsumptionPolicy.set((type) -> type != ConsumptionType.SCROLL);
            this.contentPanel.anchor.set(EAlign.values());
            addComponent(contentPanel);
            
            addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, (s, e) -> {
                if (e.widthChanged()) {
                    refresh();
                }
                return false;
            });
        }

        protected void refresh() {
            this.contentPanel.clearComponents();
            Map<String, DLGuiComponent> content = subComponents.stream().collect(Collectors.toMap(x -> x.name(), x -> x.component()));

            int totalWidth = collectionComponentRef.width();
            int fixedWidthSum = collectionComponentRef.dataSlots.stream().filter(s -> s.mode() == SizeMode.FIXED).mapToInt(s -> (int)s.size()).sum();
            int remainingWidth = Math.max(totalWidth - fixedWidthSum, 0);
            double percentTotal = collectionComponentRef.dataSlots.stream().filter(s -> s.mode == SizeMode.PERCENTAGE).mapToDouble(s -> s.size()).sum();

            Map<String, Integer> widths = new HashMap<>();

            for (DataSlot s : collectionComponentRef.dataSlots) {
                if (s.mode() == SizeMode.FIXED) {
                    widths.put(s.name(), (int)s.size());
                } else {
                    double part = (percentTotal == 0 ? 0 : (s.size() / percentTotal));
                    widths.put(s.name(), (int)Math.round(remainingWidth * part));
                }
            }

            int x = 0;
            int maxH = 0;

            for (DataSlot s : collectionComponentRef.dataSlots) {
                int w = widths.get(s.name());
                DLGuiComponent c = content.get(s.name());
                if (c != null) {
                    int h = c.height();
                    c.setPosition(x, 0);
                    c.setSize(w, h);
                    maxH = Math.max(maxH, h);
                    contentPanel.addComponent(c);
                }
                x += w;
            }
            setCollectionH(maxH);
        }



        
        @Override
        public final void setHeight(double height) {
            if (!collectionComponentRef.itemResizeAllowed.get()) {
                throw new IllegalStateException("Cannot change the size of list items after they have been created.");
            }
            super.setHeight(height);
            collectionComponentRef.layoutComponents();
        }
        
        @Override
        public final void setWidth(double width) {
            if (!collectionComponentRef.itemResizeAllowed.get()) {
                throw new IllegalStateException("Cannot change the size of list items after they have been created.");
            }
            super.setWidth(width);
            collectionComponentRef.layoutComponents();
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
}
