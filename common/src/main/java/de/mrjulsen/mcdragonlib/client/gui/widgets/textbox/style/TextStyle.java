package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

import de.mrjulsen.mcdragonlib.util.DLColor;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

public record TextStyle(
        int flags,
        int color,
        int backgroundColor,
        float scale,
        ResourceLocation font,
        String link,
        String tooltip
) {
    public static final int NO_COLOR = 0x00000000;

    public static final TextStyle DEFAULT = new TextStyle(0, 0xFFFFFFFF, NO_COLOR, 1.0F, null, null, null);

    public boolean has(StyleFlag flag) {
        return flag != null && flag.isSet(flags);
    }

    public boolean shadow() {
        if (flags == 0) {
            return false;
        }
        for (StyleFlag flag : StyleFlag.all()) {
            if (flag.isSet(flags) && flag.effect().shadow()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBackground() {
        return (backgroundColor >>> 24) != 0;
    }

    public boolean hasLink() {
        return link != null && !link.isEmpty();
    }

    public TextStyle with(StyleFlag flag, boolean value) {
        int next = value ? flag.set(flags) : flag.clear(flags);
        return next == flags ? this : new TextStyle(next, color, backgroundColor, scale, font, link, tooltip);
    }

    public TextStyle toggle(StyleFlag flag) {
        return with(flag, !flag.isSet(flags));
    }

    public TextStyle withColor(int argb) {
        return argb == color ? this : new TextStyle(flags, argb, backgroundColor, scale, font, link, tooltip);
    }

    public TextStyle withColor(DLColor color) {
        return withColor(color.getAsARGB());
    }

    public TextStyle withBackgroundColor(int argb) {
        return argb == backgroundColor ? this : new TextStyle(flags, color, argb, scale, font, link, tooltip);
    }

    public TextStyle withScale(float scale) {
        return scale == this.scale ? this : new TextStyle(flags, color, backgroundColor, scale, font, link, tooltip);
    }

    public TextStyle withFont(ResourceLocation font) {
        return new TextStyle(flags, color, backgroundColor, scale, font, link, tooltip);
    }

    public TextStyle withLink(String link) {
        return new TextStyle(flags, color, backgroundColor, scale, font, link, tooltip);
    }

    public TextStyle withTooltip(String tooltip) {
        return new TextStyle(flags, color, backgroundColor, scale, font, link, tooltip);
    }

    public boolean hasTooltip() {
        return tooltip != null && !tooltip.isEmpty();
    }

    public TextStyle merge(TextStyle other) {
        return new TextStyle(
                flags | other.flags,
                other.color != DEFAULT.color ? other.color : color,
                other.hasBackground() ? other.backgroundColor : backgroundColor,
                other.scale != DEFAULT.scale ? other.scale : scale,
                other.font != null ? other.font : font,
                other.hasLink() ? other.link : link,
                other.hasTooltip() ? other.tooltip : tooltip
        );
    }

    public Style applyTo(Style style) {
        Style result = style;
        if (flags != 0) {
            for (StyleFlag flag : StyleFlag.all()) {
                if (flag.isSet(flags)) {
                    result = flag.effect().applyVanilla(result);
                }
            }
        }
        return result;
    }

    public Style toVanillaStyle() {
        Style style = applyTo(Style.EMPTY.withColor(TextColor.fromRgb(color & 0x00FFFFFF)));
        return font != null ? style.withFont(font) : style;
    }

    public static TextStyle fromVanillaStyle(Style style) {
        int flags = 0;
        for (StyleFlag flag : StyleFlag.all()) {
            flags = flag.setIf(flags, flag.effect().matchesVanilla(style));
        }

        TextColor color = style.getColor();
        int argb = color != null ? 0xFF000000 | color.getValue() : DEFAULT.color;
        return new TextStyle(flags, argb, NO_COLOR, 1.0F, style.getFont(), null, null);
    }
}
