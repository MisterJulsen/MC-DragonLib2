package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;

public record TextStyle(
        Font font,
        boolean bold,
        boolean italic,
        boolean underlined,
        boolean strikethrough,
        boolean obfuscated,
        int color,
        boolean dropShadow,
        float scale,
        int highlightColor
) {
    public static final TextStyle EMPTY = new Builder().build();

    public static final TextStyle URL_STYLE = new Builder().color(0xFF5555FF).underlined(true).build();

    private static final String NBT_BOLD = "bold";
    private static final String NBT_ITALIC = "italic";
    private static final String NBT_UNDERLINED = "underlined";
    private static final String NBT_STRIKETHROUGH = "strikethrough";
    private static final String NBT_OBFUSCATED = "obfuscated";
    private static final String NBT_COLOR = "color";
    private static final String NBT_SHADOW = "shadow";
    private static final String NBT_SCALE = "scale";
    private static final String NBT_HIGHLIGHT_COLOR = "highlight_color";

    public TextStyle copy() {
        return new TextStyle(
                font,
                bold,
                italic,
                underlined,
                strikethrough,
                obfuscated,
                color,
                dropShadow,
                scale,
                highlightColor
        );
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(NBT_BOLD, bold);
        nbt.putBoolean(NBT_ITALIC, italic);
        nbt.putBoolean(NBT_UNDERLINED, underlined);
        nbt.putBoolean(NBT_STRIKETHROUGH, strikethrough);
        nbt.putBoolean(NBT_OBFUSCATED, obfuscated);
        nbt.putInt(NBT_COLOR, color);
        nbt.putBoolean(NBT_SHADOW, dropShadow);
        nbt.putFloat(NBT_SCALE, scale);
        nbt.putInt(NBT_HIGHLIGHT_COLOR, highlightColor);
        return nbt;
    }

    public static TextStyle fromNbt(CompoundTag nbt) {
        return new Builder()
                .bold(nbt.getBoolean(NBT_BOLD))
                .italic(nbt.getBoolean(NBT_ITALIC))
                .underlined(nbt.getBoolean(NBT_UNDERLINED))
                .strikethrough(nbt.getBoolean(NBT_STRIKETHROUGH))
                .obfuscated(nbt.getBoolean(NBT_OBFUSCATED))
                .shadow(nbt.getBoolean(NBT_SHADOW))
                .color(nbt.getInt(NBT_COLOR))
                .size((int)(Minecraft.getInstance().font.lineHeight * nbt.getFloat(NBT_SCALE)))
                .highlightColor(nbt.getInt(NBT_HIGHLIGHT_COLOR))
                .build();
    }

    public Style toStyle() {
        return Style.EMPTY
                .withBold(bold)
                .withItalic(italic)
                .withUnderlined(underlined)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated)
                .withColor(color)
            ;
    }

    public static TextStyle fromStyle(Style style) {
        return new Builder()
            .bold(style.isBold())
            .italic(style.isItalic())
            .underlined(style.isUnderlined())
            .obfuscated(style.isObfuscated())
            .strikethrough(style.isStrikethrough())
            .color(style.getColor().getValue())
            .build();
    }

    public static class Builder {
        private Font font = Minecraft.getInstance().font;
        private boolean bold;
        private boolean italic;
        private boolean underlined;
        private boolean strikethrough;
        private boolean obfuscated;
        private int color = 0xFFFFFFFF;
        private boolean dropShadow;
        private int size = Minecraft.getInstance().font.lineHeight;
        private int highlightColor = 0;

        public Builder font(Font font) {
            this.font = font;
            return this;
        }
        public Builder bold(boolean b) {
            this.bold = b;
            return this;
        }
        public Builder italic(boolean b) {
            this.italic = b;
            return this;
        }
        public Builder strikethrough(boolean b) {
            this.strikethrough = b;
            return this;
        }
        public Builder underlined(boolean b) {
            this.underlined = b;
            return this;
        }
        public Builder obfuscated(boolean b) {
            this.obfuscated = b;
            return this;
        }
        public Builder color(int color) {
            this.color = color;
            return this;
        }
        public Builder shadow(boolean b) {
            this.dropShadow = b;
            return this;
        }
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder highlightColor(int highlightColor) {
            this.highlightColor = highlightColor;
            return this;
        }

        public TextStyle build() {
            return new TextStyle(
                    font,
                    bold,
                    italic,
                    underlined,
                    strikethrough,
                    obfuscated,
                    color,
                    dropShadow,
                    (float)size / font.lineHeight,
                    highlightColor
            );
        }
    }
}
