package de.mrjulsen.mcdragonlib.gui.v2.text.style;

@FunctionalInterface
public interface IStyleTransform {

    TextStyle apply(TextStyle style);

    static IStyleTransform flag(StyleFlag flag) {
        return style -> style.with(flag, true);
    }
}
