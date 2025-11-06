package de.mrjulsen.mcdragonlib.util.properties;

public class BooleanProperty extends Property<Boolean> {

    private final boolean falseWeakness;

    /**
     * Creates a new {@code BooleanProperty}.
     * @param defaultValue The default value of the property, which can be restored if necessary.
     * @param falseWeakness Indicates whether {@code false} values ​​should be preferred in the inheritance.
     * In other words, if {@code falseWeakness} is set to {@code true} and either the
     * user-defined value or the inherited value is {@code false}, the result will be {@code false}. If
     * {@code falseWeakness} is set to {@code false}, the end result will be {@code true},
     * if either the user-defined value or the inherited value is {@code true}.
     */
    public BooleanProperty(boolean defaultValue, boolean falseWeakness) {
        super(defaultValue);
        this.falseWeakness = falseWeakness;
    }

    @Override
    public Boolean get() {
        return shouldOverrideLocal() && getInheritedValue().orElse(false) != falseWeakness ? getInheritedValue().orElse(getValue()) : getValue();
    }


    public boolean toggle() {
        return set(!getValue());
    }
}
