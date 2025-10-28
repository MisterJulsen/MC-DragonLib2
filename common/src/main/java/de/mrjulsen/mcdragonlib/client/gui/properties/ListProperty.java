package de.mrjulsen.mcdragonlib.client.gui.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

public class ListProperty<T> extends Property<List<T>> implements List<T> {

    public static enum ListOperation {
        ADD, REMOVE, RETAIN, CLEAR;
    }

    @FunctionalInterface
    public static interface IListPropertyUpdateCallback<T> {
        void update(List<T> value, ListOperation operation);
    }

    private Optional<IListPropertyUpdateCallback<T>> onUpdate = Optional.empty();

    public ListProperty() {
        super(new ArrayList<>());
    }

    public ListProperty(List<T> defaultValue) {
        super(new ArrayList<>(defaultValue));
    }

    
    public <P extends ListProperty<T>> P withUpdateCallback(IListPropertyUpdateCallback<T> callback) {
        this.onUpdate = Optional.ofNullable(callback);
        return (P)this;
    }

    protected Optional<IListPropertyUpdateCallback<T>> getOnUpdateCallback() {
        return onUpdate;
    }


    @Override
    public List<T> get() {
        return Collections.unmodifiableList(super.get());
    }

    @Override
    public List<T> getValue() {
        return Collections.unmodifiableList(super.getValue());
    }

    @Override
    public List<T> getDefaultValue() {
        return Collections.unmodifiableList(super.getDefaultValue());
    }
    
    @Override
    public Optional<List<T>> getInheritedValue() {
        return super.getInheritedValue().map(Collections::unmodifiableList);
    }

    @Override
    public List<T> set(List<T> t) {
        clear();
        addAll(t);
        return get();
    }


    
    protected List<T> modifyInput(List<T> input) {
        return getModificationCallback().map(x -> x.update(get(), input)).orElse(input);
    }

    protected void runAfterChange(ListOperation operation) {
        List<T> old = get();
        getOnUpdateCallback().ifPresent(x -> x.update(old, operation));
        getAfterChangeCallback().ifPresent(x -> x.update(old, get()));
    }

    protected List<T> getList() {
        return super.getValue();
    }



    @Override
    public boolean add(T o) {
        return addAll(List.of(o));
    }

    @Override
    public void add(int index, T o) {
        addAll(index, List.of(o));
    }

    public boolean addAll(T... c) {
        boolean b = this.getList().addAll(modifyInput(ImmutableList.copyOf(c)));
        runAfterChange(ListOperation.ADD);
        return b;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean b = this.getList().addAll(modifyInput(ImmutableList.copyOf(c)));
        runAfterChange(ListOperation.ADD);
        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean b = this.getList().addAll(index, modifyInput(ImmutableList.copyOf(c)));
        runAfterChange(ListOperation.ADD);
        return b;
    }

    @Override
    public void clear() {
        this.getList().clear();
        List<T> currentState = get();
        List<T> modifiedState = modifyInput(List.of());
        if (currentState != modifiedState) {
            this.getList().clear();
            this.getList().addAll(modifiedState);
        }
        runAfterChange(ListOperation.CLEAR);
    }

    @Override
    public boolean contains(Object o) {
        return this.getList().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.getList().containsAll(c);
    }

    @Override
    public T get(int index) {
        return this.getList().get(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.getList().indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return this.getList().isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return this.getList().iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.getList().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.getList().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return this.getList().listIterator(index);
    }

    @Override
    public boolean remove(Object o) {
        return this.getList().remove(o);
    }

    @Override
    public T remove(int index) {
        T o = this.getList().remove(index);
        List<T> currentState = get();
        List<T> modifiedState = modifyInput(currentState);
        if (currentState != modifiedState) {
            this.getList().clear();
            this.getList().addAll(modifiedState);
        }
        runAfterChange(ListOperation.REMOVE);
        return o;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean b = this.getList().removeAll(c);
        List<T> currentState = get();
        List<T> modifiedState = modifyInput(currentState);
        if (currentState != modifiedState) {
            this.getList().clear();
            this.getList().addAll(modifiedState);
        }
        runAfterChange(ListOperation.REMOVE);
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean b = this.getList().retainAll(c);
        List<T> currentState = get();
        List<T> modifiedState = modifyInput(currentState);
        if (currentState != modifiedState) {
            this.getList().clear();
            this.getList().addAll(modifiedState);
        }
        runAfterChange(ListOperation.RETAIN);
        return b;
    }

    @Override
    public T set(int index, T o) {
        T t = this.getList().set(index, o);
        List<T> currentState = get();
        List<T> modifiedState = modifyInput(currentState);
        if (currentState != modifiedState) {
            this.getList().clear();
            this.getList().addAll(modifiedState);
        }
        return t;
    }

    @Override
    public int size() {
        return this.getList().size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {        
        return this.getList().subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return this.getList().toArray();
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] arr) {
        return this.getList().toArray(arr);
    }

    
    
}
