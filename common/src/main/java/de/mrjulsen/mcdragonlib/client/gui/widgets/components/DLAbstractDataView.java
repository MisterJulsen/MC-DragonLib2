package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.util.properties.ListProperty;
import net.minecraft.network.chat.Component;

public abstract class DLAbstractDataView<T, I extends DLAbstractDataView.DLDataViewItem<T, ?>> extends DLAbstractCollectionComponent<T, I> {

    public static record DataSlot(String name, Component displayName, double size, SizeMode mode) {}
    public static record DataSlotComponent(String name, DLGuiComponent component) {}
    public static enum SizeMode { FIXED, PERCENTAGE; }

    public final ListProperty<DataSlot> dataSlots = new ListProperty<DataSlot>()
        .withAfterPropertyChangedCallback((o, val) -> {
            createComponents();
            layoutComponents();
        });

    public DLAbstractDataView(int x, int y, int w, int h) {
        super(x, y, w, h);
    }
    

    protected static abstract class DLDataViewItem<T, L extends DLAbstractDataView<T, ?>> extends DLAbstractCollectionComponent.DLCollectionItem<T, L> {        
            
        protected final DLPanel contentPanel;

        public final ListProperty<DataSlotComponent> subComponents = new ListProperty<>();

        protected DLDataViewItem(L collectionComponentRef, T item) {
            super(collectionComponentRef, item, 1, 1);

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
    }
}
