package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

public class DLBasicDataView<T> extends DLAbstractDataView<T, DLBasicDataView.DLBasicItem<T>> {

    private int requiredHeight = 0;

    public DLBasicDataView(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    protected DLBasicItem<T> defaultItemBuilder(T item) {
        return new DLBasicItem<>(this, item);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void layoutComponents() {
        int currentY = 0;
        for (DLDataViewItem<?, ?> itm : contentPanel.getComponentsOfType(DLDataViewItem.class, true)) {
            DLBasicItem<T> item = (DLBasicItem<T>)itm;
            setItemX(item, 0);
            setItemY(item, currentY);
            setItemWidth(item, width());
            currentY += item.height();
        }
        requiredHeight = currentY;
    }

    public int getRequiredHeight() {
        return requiredHeight;
    }    

    public static class DLBasicItem<T> extends DLAbstractDataView.DLDataViewItem<T, DLBasicDataView<T>> {
        public DLBasicItem(DLBasicDataView<T> collectionComponentRef, T item) {
            super(collectionComponentRef, item);
        }
    }
}
