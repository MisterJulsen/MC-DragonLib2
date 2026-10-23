package de.mrjulsen.mcdragonlib.gui.v2.theme;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.mrjulsen.mcdragonlib.util.DLColor;

/**
 * A set of colors and flags for rendering, looked up by typed keys.
 * <p>Themes are immutable and stack: a theme may have a parent it falls back to, and on top of that
 * the lookup walks up the component tree, see GuiComponent#getThemeValue. So a theme only needs to
 * hold what it actually changes, and a key that is set nowhere ends up at its own fallback.</p>
 */
public final class Theme {

    /**
     * A theme key: its name and the value to use when no theme defines it.
     * <p>Keys are compared by identity, so hold on to the constant instead of building the same key
     * twice. Define your own with {@link Theme#key(String, Object)}.</p>
     */
    public record Key<T>(String id, T fallback) {}

    public static final Key<DLColor> TEXT = key("text", DLColor.WHITE);
    public static final Key<DLColor> TEXT_DISABLED = key("text_disabled", DLColor.fromInt(0xFF9E9E9E));
    public static final Key<DLColor> TEXT_HIGHLIGHT = key("text_highlight", DLColor.fromInt(0xFFFFFFA0));
    public static final Key<DLColor> TEXT_MUTED = key("text_muted", DLColor.fromInt(0xFF969696));
    /**
     * Text on a light background, e.g. inside a selection.
     */
    public static final Key<DLColor> TEXT_INVERTED = key("text_inverted", DLColor.fromInt(0xFF404040));
    /**
     * Whether text is drawn with a drop shadow.
     */
    public static final Key<Boolean> TEXT_SHADOW = key("text_shadow", Boolean.TRUE);

    public static final Key<DLColor> BACKGROUND = key("background", DLColor.fromInt(0xC8141414));
    public static final Key<DLColor> BORDER = key("border", DLColor.fromInt(0xFF5A5A5A));
    /**
     * Background of a selected entry or a text selection.
     */
    public static final Key<DLColor> SELECTION = key("selection", DLColor.fromInt(0x78FFFFFF));
    /**
     * Background of a hovered entry, weaker than {@link #SELECTION}.
     */
    public static final Key<DLColor> HIGHLIGHT = key("highlight", DLColor.fromInt(0x32FFFFFF));
    /**
     * Dimming behind a modal layer or a popup.
     */
    public static final Key<DLColor> SCRIM = key("scrim", DLColor.fromInt(0x6E000000));

    /**
     * The highlight color of the theme, for focus rings, progress bars and the like.
     */
    public static final Key<DLColor> ACCENT = key("accent", DLColor.fromInt(0xFF1572E6));
    public static final Key<DLColor> SUCCESS = key("success", DLColor.fromInt(0xFF0DB24D));
    public static final Key<DLColor> DANGER = key("danger", DLColor.fromInt(0xFFE83E4D));
    public static final Key<DLColor> WARNING = key("warning", DLColor.fromInt(0xFFE8BD3E));

    /**
     * The empty theme, where every key falls back to its own default. Not necessarily the one in use,
     * see {@link #getDefault()}.
     */
    public static final Theme DEFAULT = new Theme(null, Map.of());

    private static Theme current = DEFAULT;

    private final Theme parent;
    private final Map<Key<?>, Object> values;

    private Theme(Theme parent, Map<Key<?>, Object> values) {
        this.parent = parent;
        this.values = values;
    }

    /**
     * Defines a new theme key. Keep the result in a constant, since keys are compared by identity.
     *
     * @param fallback the value used while no theme defines this key
     */
    public static <T> Key<T> key(String id, T fallback) {
        return new Key<>(id, fallback);
    }

    /**
     * The theme used for components that do not define one themselves.
     */
    public static Theme getDefault() {
        return current;
    }

    /**
     * Installs the global theme, affecting every GUI that does not override the key in question. Null
     * restores {@link #DEFAULT}.
     */
    public static void setDefault(Theme theme) {
        current = theme == null ? DEFAULT : theme;
    }

    public static Builder builder() {
        return new Builder(null);
    }

    /**
     * A builder for a new theme that falls back to this one for everything it does not set.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * The value for this key, from this theme or its parents, or the key's fallback. Never null.
     */
    public <T> T get(Key<T> key) {
        T value = find(key);
        return value != null ? value : key.fallback();
    }

    @SuppressWarnings("unchecked")
    /**
     * The value for this key, or null if neither this theme nor its parents define it. Lets callers tell
     * an unset key from one set to its default value.
     */
    public <T> T find(Key<T> key) {
        Theme theme = this;
        while (theme != null) {
            Object value = theme.values.get(key);
            if (value != null) {
                return (T) value;
            }
            theme = theme.parent;
        }
        return null;
    }

    public boolean has(Key<?> key) {
        return find(key) != null;
    }

    /**
     * A copy of this theme with that one key changed, since themes are immutable.
     */
    public <T> Theme with(Key<T> key, T value) {
        Map<Key<?>, Object> copy = new HashMap<>(values);
        copy.put(key, Objects.requireNonNull(value));
        return new Theme(parent, Map.copyOf(copy));
    }

    /**
     * Collects key and value pairs for a new theme.
     */
    public static final class Builder {

        private final Theme parent;
        private final Map<Key<?>, Object> values = new HashMap<>();

        private Builder(Theme parent) {
            this.parent = parent;
        }

        public <T> Builder set(Key<T> key, T value) {
            values.put(key, Objects.requireNonNull(value));
            return this;
        }

        public Theme build() {
            return new Theme(parent, Map.copyOf(values));
        }
    }
}
