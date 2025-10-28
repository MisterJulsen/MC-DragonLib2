package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext;

import de.mrjulsen.mcdragonlib.mixin.FontAccessor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.mutable.MutableFloat;

public class TextSegment {

    private static final String NBT_TEXT = "Text";
    private static final String NBT_STYLE = "Style";

    public record ImmutableTextSegment(String text, TextStyle style) {}

    private StringBuilder text;
    private TextStyle style;

    public TextSegment(StringBuilder text, TextStyle style) {
        this.text = text;
        this.style = style;
    }
    public StringBuilder stringBuilder() {
        return text;
    }
    public String text() {
        return text.toString();
    }
    public TextStyle style() {
        return style;
    }

    public int charLength() {
        return stringBuilder().length();
    }

    public int length() {
        return stringBuilder().codePointCount(0, text.length());
    }

    public int codePointLength() {
        return stringBuilder().codePointCount(0, text.length());
    }

    public boolean isEmpty() {
        return stringBuilder().isEmpty();
    }

    public TextSegment copy() {
        return new TextSegment(new StringBuilder(this.text), this.style.copy());
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(NBT_TEXT, text.toString());
        nbt.put(NBT_STYLE, style.toNbt());
        return nbt;
    }

    public static TextSegment fromNbt(CompoundTag nbt) {
        return new TextSegment(new StringBuilder(nbt.getString(NBT_TEXT)), TextStyle.fromNbt(nbt.getCompound(NBT_STYLE)));
    }

    public Component toComponent() {
        return TextUtils.text(text()).withStyle(style().toStyle());
    }

    public static TextSegment fromComponent(Component component) {
        return new TextSegment(new StringBuilder(component.getString()), TextStyle.fromStyle(component.getStyle()));
    }

    public float width() {
        if (text == null || text.length() == 0) {
            return 0.0F;
        }

        MutableFloat mutableFloat = new MutableFloat();
        FontAccessor accessor = (FontAccessor) style().font();
        FontSet fontSet = accessor.dragonlib$invokeGetFontSet(Style.DEFAULT_FONT);
        iterate(this, 0, (idx, style, c) -> {
            mutableFloat.add(fontSet.getGlyphInfo(c, accessor.dragonlib$filterFishyGlyphs()).getAdvance(style().bold()));
            return true;
        }, length());
        return mutableFloat.floatValue() * style().scale();
    }

    public ImmutableTextSegment copyImmutable() {
        return copyImmutable(0, length());
    }

    public ImmutableTextSegment copyImmutable(int codePointStartIndex, int codePointEndIndex) {
        if (codePointStartIndex < 0 || codePointEndIndex > length() || codePointStartIndex > codePointEndIndex) {
            throw new IndexOutOfBoundsException("Cannot copy immutable part from code point index " + codePointStartIndex + " to " + codePointEndIndex + " for segment with code point length " + length());
        }
        String originalString = stringBuilder().toString();
        int charStartIndex = originalString.offsetByCodePoints(0, codePointStartIndex);
        int charEndIndex = originalString.offsetByCodePoints(0, codePointEndIndex);
        return new ImmutableTextSegment(originalString.substring(charStartIndex, charEndIndex), style().copy());
    }

    @FunctionalInterface
    public interface CharCallback {
        boolean accept(int codePointIndexInIteration, TextStyle style, int codePoint);
    }

    private static final int REPLACEMENT_CHAR_CODEPOINT = 0xFFFD;

    public static boolean iterate2(TextSegment segment, int skip, CharCallback sink, int count) {
        final StringBuilder sb = segment.stringBuilder();
        final int charLength = sb.length();

        if (skip < 0 || count <= 0) {
            return true;
        }

        int currentCodePointIndex = 0;
        int iteratedCodePoints = 0;
        int charIdx = 0;

        while (charIdx < charLength && iteratedCodePoints < count) {
            int codePoint = sb.codePointAt(charIdx);
            int charsForThisCodePoint = Character.charCount(codePoint);

            if (currentCodePointIndex >= skip) {
                if (!sink.accept(iteratedCodePoints, segment.style(), codePoint)) {
                    return false;
                }
                iteratedCodePoints++;
            }

            currentCodePointIndex++;
            charIdx += charsForThisCodePoint;
        }
        return true;
    }

    public static boolean iterate(TextSegment segment, int skipCodePoints, CharCallback sink, int countCodePoints) {
        final StringBuilder sb = segment.stringBuilder();
        final int charLength = sb.length();

        if (skipCodePoints < 0 || countCodePoints <= 0) {
            return true;
        }

        int currentOverallCodePointIndex = 0;
        int iteratedCodePointsInThisCall = 0;
        int charIdx = 0;

        while (charIdx < charLength && iteratedCodePointsInThisCall < countCodePoints) {
            int codePoint = sb.codePointAt(charIdx);
            int charsForThisCodePoint = Character.charCount(codePoint);

            if (currentOverallCodePointIndex >= skipCodePoints) {
                if (!sink.accept(iteratedCodePointsInThisCall, segment.style(), codePoint)) {
                    return false;
                }
                iteratedCodePointsInThisCall++;
            }

            currentOverallCodePointIndex++;
            charIdx += charsForThisCodePoint;
        }
        return true;
    }
}
