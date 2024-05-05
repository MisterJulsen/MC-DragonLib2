package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class DLCycleButton<T> extends DLButton {
    static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    private final Component name;
    private int index;
    private T value;
    private final DLCycleButton.ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<DLCycleButton<T>, MutableComponent> narrationProvider;
    private final DLCycleButton.OnValueChange<T> onValueChange;
    private final boolean displayOnlyValue;

    DLCycleButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Component pName, int pIndex,
            T pValue, DLCycleButton.ValueListSupplier<T> pValues, Function<T, Component> pValueStringifier,
            Function<DLCycleButton<T>, MutableComponent> pNarrationProvider,
            DLCycleButton.OnValueChange<T> pOnValueChange, boolean pDisplayOnlyValue) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.name = pName;
        this.index = pIndex;
        this.value = pValue;
        this.values = pValues;
        this.valueStringifier = pValueStringifier;
        this.narrationProvider = pNarrationProvider;
        this.onValueChange = pOnValueChange;
        this.displayOnlyValue = pDisplayOnlyValue;
    }

    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }

    }

    private void cycleValue(int pDelta) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + pDelta, list.size());
        T t = list.get(this.index);
        this.updateValue(t);
        this.onValueChange.onValueChange(this, t);
    }

    private T getCycledValue(int pDelta) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + pDelta, list.size()));
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pDelta > 0.0D) {
            this.cycleValue(-1);
        } else if (pDelta < 0.0D) {
            this.cycleValue(1);
        }

        return true;
    }

    public void setValue(T pValue) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(pValue);
        if (i != -1) {
            this.index = i;
        }

        this.updateValue(pValue);
    }

    private void updateValue(T pValue) {
        Component component = this.createLabelForValue(pValue);
        this.setMessage(component);
        this.value = pValue;
    }

    private Component createLabelForValue(T pValue) {
        return (Component) (this.displayOnlyValue ? this.valueStringifier.apply(pValue) : this.createFullName(pValue));
    }

    private MutableComponent createFullName(T pValue) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(pValue));
    }

    public T getValue() {
        return this.value;
    }

    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput arg) {
        arg.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            T object = this.getCycledValue(1);
            Component component = this.createLabelForValue(object);
            if (this.isFocused()) {
                arg.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                arg.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return wrapDefaultNarrationMessage(
                (Component) (this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
    }

    public static <T> DLCycleButton.Builder<T> builder(Function<T, Component> pValueStringifier) {
        return new DLCycleButton.Builder<>(pValueStringifier);
    }

    public static DLCycleButton.Builder<Boolean> booleanBuilder(Component pComponentOn,
            Component pComponentOff) {
        return (new DLCycleButton.Builder<Boolean>((p_168902_) -> {
            return p_168902_ ? pComponentOn : pComponentOff;
        })).withValues(BOOLEAN_OPTIONS);
    }

    public static DLCycleButton.Builder<Boolean> onOffBuilder() {
        return (new DLCycleButton.Builder<Boolean>((p_168891_) -> {
            return p_168891_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
        })).withValues(BOOLEAN_OPTIONS);
    }

    public static DLCycleButton.Builder<Boolean> onOffBuilder(boolean pInitialValue) {
        return onOffBuilder().withInitialValue(pInitialValue);
    }

    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private Function<DLCycleButton<T>, MutableComponent> narrationProvider = DLCycleButton::createDefaultNarrationMessage;
        private DLCycleButton.ValueListSupplier<T> values = DLCycleButton.ValueListSupplier
                .create(ImmutableList.of());
        private boolean displayOnlyValue;

        public Builder(Function<T, Component> pValueStringifier) {
            this.valueStringifier = pValueStringifier;
        }

        public DLCycleButton.Builder<T> withValues(List<T> pValues) {
            this.values = DLCycleButton.ValueListSupplier.create(pValues);
            return this;
        }

        @SafeVarargs
        public final DLCycleButton.Builder<T> withValues(T... pValues) {
            return this.withValues(ImmutableList.copyOf(pValues));
        }

        public DLCycleButton.Builder<T> withValues(List<T> pDefaultList, List<T> pSelectedList) {
            this.values = DLCycleButton.ValueListSupplier.create(DLCycleButton.DEFAULT_ALT_LIST_SELECTOR,
                    pDefaultList, pSelectedList);
            return this;
        }

        public DLCycleButton.Builder<T> withValues(BooleanSupplier pAltListSelector, List<T> pDefaultList,
                List<T> pSelectedList) {
            this.values = DLCycleButton.ValueListSupplier.create(pAltListSelector, pDefaultList, pSelectedList);
            return this;
        }

        public DLCycleButton.Builder<T> withInitialValue(T pInitialValue) {
            this.initialValue = pInitialValue;
            int i = this.values.getDefaultList().indexOf(pInitialValue);
            if (i != -1) {
                this.initialIndex = i;
            }

            return this;
        }

        public DLCycleButton.Builder<T> withCustomNarration(
                Function<DLCycleButton<T>, MutableComponent> pNarrationProvider) {
            this.narrationProvider = pNarrationProvider;
            return this;
        }

        public DLCycleButton.Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public DLCycleButton<T> create(int pX, int pY, int pWidth, int pHeight, Component pName) {
            return this.create(pX, pY, pWidth, pHeight, pName, (p_168946_, p_168947_) -> {
            });
        }

        public DLCycleButton<T> create(int pX, int pY, int pWidth, int pHeight, Component pName,
                DLCycleButton.OnValueChange<T> pOnValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T t = (T) (this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
                Component component = this.valueStringifier.apply(t);
                Component component1 = (Component) (this.displayOnlyValue ? component
                        : CommonComponents.optionNameValue(pName, component));
                return new DLCycleButton<>(pX, pY, pWidth, pHeight, component1, pName, this.initialIndex, t,
                        this.values, this.valueStringifier, this.narrationProvider, pOnValueChange,
                        this.displayOnlyValue);
            }
        }
    }

    public interface OnValueChange<T> {
        void onValueChange(DLCycleButton<T> pCycleButton, T pValue);
    }

    @FunctionalInterface
    public interface TooltipSupplier<T> extends Function<T, List<FormattedCharSequence>> {
    }

    interface ValueListSupplier<T> {
        List<T> getSelectedList();

        List<T> getDefaultList();

        static <T> DLCycleButton.ValueListSupplier<T> create(List<T> pValues) {
            final List<T> list = ImmutableList.copyOf(pValues);
            return new DLCycleButton.ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return list;
                }

                public List<T> getDefaultList() {
                    return list;
                }
            };
        }

        static <T> DLCycleButton.ValueListSupplier<T> create(final BooleanSupplier pAltListSelector,
                List<T> pDefaultList, List<T> pSelectedList) {
            final List<T> list = ImmutableList.copyOf(pDefaultList);
            final List<T> list1 = ImmutableList.copyOf(pSelectedList);
            return new DLCycleButton.ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return pAltListSelector.getAsBoolean() ? list1 : list;
                }

                public List<T> getDefaultList() {
                    return list;
                }
            };
        }
    }
}
