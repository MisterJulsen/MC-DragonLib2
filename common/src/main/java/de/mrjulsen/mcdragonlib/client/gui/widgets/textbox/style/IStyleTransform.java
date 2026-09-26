package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

@FunctionalInterface
public interface IStyleTransform {

    TextStyle apply(TextStyle style);

    static IStyleTransform flag(StyleFlag flag) {
        return style -> style.with(flag, true);
    }
}
