package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import de.mrjulsen.mcdragonlib.mixin.StringSplitterAccessor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NewFontUtils {

    /**
     * Misst die Breite eines formatierten Strings bis (exklusiv) zum angegebenen Zeichen-Index.
     *
     * @param content Der rohe String mit Formatierung.
     * @param index   Der Char-Index, bis zu dem gemessen werden soll (exklusive).
     * @param style   Der Start-Style für den String.
     * @return Die Breite in Pixeln bis zum gegebenen Index.
     */
    public static float formattedWidthByIndex(String content, int index, Style style) {
        MutableFloat sum = new MutableFloat(0.0F);
        Font font = Minecraft.getInstance().font;
        StringSplitterAccessor accessor = (StringSplitterAccessor)font.getSplitter();
        FormattedCharSink sink = new FormattedCharSink() {
            @Override
            public boolean accept(int pos, Style s, int codePoint) {
                // pos ist der Char-Offset im String
                if (pos >= index) {
                    return false; // Abbruch
                }
                sum.add(accessor.dragonlib$getWidthProvider().getWidth(codePoint, s));
                return true;
            }
        };
        // Iteriere formatiert bis zum Abbruch
        StringDecomposer.iterateFormatted(content, style, sink);
        return sum.floatValue();
    }

    public static FormattedText substring(FormattedText text, int start, int end) {
        Font font = Minecraft.getInstance().font;
        StringSplitterAccessor accessor = (StringSplitterAccessor)font.getSplitter();

        AtomicInteger currentIndex = new AtomicInteger(0);
        MutableComponent result = TextUtils.empty();
        text.visit(
                (currentStyle, part) -> {
                    int currentIdx = currentIndex.get();
                    currentIndex.set(currentIdx + part.length());
                    if (currentIndex.get() < start || currentIdx > end) {
                        return Optional.empty();
                    }
                    result.append(TextUtils.text(part.substring(Math.max(start, currentIdx), Math.min(end, currentIdx + part.length()))).withStyle(currentStyle));
                    return Optional.ofNullable(null);
                },
                Style.EMPTY
        );
        return result;
    }


    /**
     * Variante für bereits aufgebautes FormattedText.
     */
    public static float formattedWidthByIndex(FormattedText content, int index, Style style) {
        MutableFloat sum = new MutableFloat(0.0F);
        Font font = Minecraft.getInstance().font;
        StringSplitterAccessor accessor = (StringSplitterAccessor)font.getSplitter();

        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicInteger currentIndex = new AtomicInteger(0);
        FormattedCharSink sink = (pos, s, codePoint) -> {
            if (currentIndex.getAndIncrement() >= index) {
                finished.set(true);
                return false;
            }
            sum.add(accessor.dragonlib$getWidthProvider().getWidth(codePoint, s));
            return true;
        };

        content.visit(
                (currentStyle, part) -> {
                    if (finished.get())
                        return Optional.ofNullable("");
                    return StringDecomposer.iterateFormatted(part, currentStyle, sink) ? Optional.empty() : Optional.ofNullable("");
                },
                style
        );
        return sum.floatValue();
    }

    /**
     * Variante für FormattedCharSequence.
     */
    public static float formattedWidthByIndex(FormattedCharSequence content, int index) {
        MutableFloat sum = new MutableFloat(0.0F);
        Font font = Minecraft.getInstance().font;
        StringSplitterAccessor accessor = (StringSplitterAccessor)font.getSplitter();
        FormattedCharSink sink = (pos, s, codePoint) -> {
            if (pos >= index) return false;
            sum.add(accessor.dragonlib$getWidthProvider().getWidth(codePoint, s));
            return true;
        };
        content.accept(sink);
        return sum.floatValue();
    }
}
