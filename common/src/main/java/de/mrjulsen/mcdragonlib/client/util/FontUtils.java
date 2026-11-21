/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.mutable.MutableFloat;

import com.mojang.blaze3d.font.GlyphInfo;

import de.mrjulsen.mcdragonlib.mixin.client.BakedGlyphAccessor;
import de.mrjulsen.mcdragonlib.mixin.client.FontAccessor;
import de.mrjulsen.mcdragonlib.mixin.client.StringSplitterAccessor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

/**
 * Text helpers. The static measuring/substring helpers are safe to use. The instance API (glyph UV manipulation) is deprecated.
 */
public class FontUtils {
    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public Font font;
    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public FontSet fontSet;
    
    protected static record UVData(float u0, float v0, float u1, float v1) {}
    protected static final Map<Integer, Deque<UVData>> uvStack = new HashMap<>();

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public FontUtils(ResourceLocation fontStyle) {
        this.font = Minecraft.getInstance().font;
        this.fontSet = ((FontAccessor)this.font).dragonlib$invokeGetFontSet(fontStyle);
    }    

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public BakedGlyphAccessor getGlyphAccessor(int charCode) {
        return (BakedGlyphAccessor)getGlyph(charCode);
    }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public void pushUV(int charCode) {
        BakedGlyphAccessor glyph = getGlyphAccessor(charCode);
        pushUV(charCode, glyph.dragonlib$getU0(), glyph.dragonlib$getV0(), glyph.dragonlib$getU1(), glyph.dragonlib$getV1());
    }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    protected void pushUV(int charCode, float u0, float v0, float u1, float v1) {
        if (!uvStack.containsKey(charCode)) {
            uvStack.put(charCode, new ArrayDeque<>());
        }
        uvStack.get(charCode).addLast(new UVData(u0, v0, u1, v1));
    }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public boolean popUV(int charCode) {
        if (!uvStack.containsKey(charCode)) {
            return false;
        }

        UVData data = uvStack.get(charCode).pollLast();
        if (uvStack.get(charCode).isEmpty()) {
            uvStack.remove(charCode);
        }
        
        BakedGlyphAccessor glyph = getGlyphAccessor(charCode);
        glyph.dragonlib$setU0(data.u0());
        glyph.dragonlib$setV0(data.v0());
        glyph.dragonlib$setU1(data.u1());
        glyph.dragonlib$setV1(data.v1());
        return true;
    }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public GlyphInfo getGlyphInfo(int charCode) {
        return fontSet.getGlyphInfo(charCode, false);
    }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public BakedGlyph getGlyph(int charCode) {
        return fontSet.getGlyph(charCode);
    }

    /** @deprecated Modifies shared glyphs and caches the FontSet, which breaks after resource/language reloads (corrupted font). Use {@link de.mrjulsen.mcdragonlib.client.util.ClippingBufferSource} for clipped text instead. */
    @Deprecated(forRemoval = true)
    public void reset() {
        uvStack.clear();
    }




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
