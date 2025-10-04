package de.mrjulsen.mcdragonlib.client.newgui.widgets;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface Mask {
    String mask(String text);

    public static class PasswordMask implements Mask {
        protected final char maskCharacter;

        public PasswordMask() {
            this.maskCharacter = '*';
        }

        public PasswordMask(char maskChar) {
            this.maskCharacter = maskChar;
        }

        @Override
        public String mask(String text) {
            if (text == null) return "";
            return String.valueOf(maskCharacter).repeat(text.length());
        }

        public char getMaskCharacter() {
            return maskCharacter;
        }
    }
}