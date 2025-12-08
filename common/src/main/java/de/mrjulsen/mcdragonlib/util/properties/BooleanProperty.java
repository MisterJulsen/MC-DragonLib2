package de.mrjulsen.mcdragonlib.util.properties;

public class BooleanProperty extends Property<Boolean> {

    /**
     * Create a new BooleanProperty with the specified default and inheritance behavior.
     *
     * @param defaultValue the default boolean value for this property
     */
    public BooleanProperty(boolean defaultValue) {
        super(defaultValue);
    }

    /**
     * Toggle the locally stored boolean value and return the requested new value.
     *
     * <p>This delegates to {@link #set(Object)} with the negated current local value.
     * The modification and after-change callbacks (if any) are applied as usual by {@code set}.</p>
     *
     * @return the new boolean value that was requested to be set
     */
    public boolean toggle() {
        return set(!get());
    }
}
