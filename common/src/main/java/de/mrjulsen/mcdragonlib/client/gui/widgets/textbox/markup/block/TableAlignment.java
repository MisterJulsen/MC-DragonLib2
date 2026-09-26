package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public final class TableAlignment {

    public static final char LEFT = 'l';
    public static final char CENTER = 'c';
    public static final char RIGHT = 'r';

    private TableAlignment() {
    }

    public static char code(ETextAlignment alignment) {
        return switch (alignment) {
            case CENTER -> CENTER;
            case RIGHT -> RIGHT;
            default -> LEFT;
        };
    }

    public static ETextAlignment of(char code) {
        return switch (code) {
            case CENTER -> ETextAlignment.CENTER;
            case RIGHT -> ETextAlignment.RIGHT;
            default -> ETextAlignment.LEFT;
        };
    }

    public static ETextAlignment[] parse(String codes) {
        if (codes == null || codes.isEmpty()) {
            return null;
        }
        ETextAlignment[] alignments = new ETextAlignment[codes.length()];
        for (int i = 0; i < codes.length(); i++) {
            alignments[i] = of(codes.charAt(i));
        }
        return alignments;
    }
}
