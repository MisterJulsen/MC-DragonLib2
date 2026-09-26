package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

public final class MarkupState {

    private static final int MAX_BITS = 32;

    private static int allocatedBits;

    private MarkupState() {
    }

    public static int allocateFlag() {
        return allocateField(1);
    }

    public static int allocateField(int bits) {
        int size = Math.max(1, bits);
        if (allocatedBits + size > MAX_BITS) {
            throw new IllegalStateException("No markup state bits left to allocate.");
        }
        int mask = ((1 << size) - 1) << allocatedBits;
        allocatedBits += size;
        return mask;
    }

    public static boolean isSet(int state, int mask) {
        return (state & mask) != 0;
    }

    public static int set(int state, int mask, boolean value) {
        return value ? state | mask : state & ~mask;
    }

    public static int toggle(int state, int mask) {
        return state ^ mask;
    }

    public static int get(int state, int mask) {
        return (state & mask) >>> Integer.numberOfTrailingZeros(mask);
    }

    public static int with(int state, int mask, int value) {
        int shifted = (value << Integer.numberOfTrailingZeros(mask)) & mask;
        return (state & ~mask) | shifted;
    }
}
