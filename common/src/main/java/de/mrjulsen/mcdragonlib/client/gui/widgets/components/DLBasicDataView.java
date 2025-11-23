package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

public class DLBasicDataView<T> extends DLAbstractDataView<T, DLBasicDataView.DLBasicItem<T>> {

    public DLBasicDataView(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    protected DLBasicItem<T> defaultItemBuilder(T item) {
        return new DLBasicItem<>(this, item);
    }

    public static class DLBasicItem<T> extends DLAbstractDataView.DLDataViewItem<T, DLBasicDataView<T>> {
        public DLBasicItem(DLBasicDataView<T> collectionComponentRef, T item) {
            super(collectionComponentRef, item);
        }
    }
}
