package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ITextTooltipProvider {

    ITextTooltipProvider STYLE_ONLY = (context, format, style) -> {
        String tooltip = context == null ? null : context.tooltip();
        return tooltip == null ? DLTooltip.EMPTY : new DLTooltip(List.of(TextUtils.text(tooltip)), style.tooltipMaxWidth);
    };

    ITextTooltipProvider DEFAULT = (context, format, style) -> {
        DLTooltip own = STYLE_ONLY.tooltipFor(context, format, style);
        if (!own.lines().isEmpty()) {
            return own;
        }
        List<Component> lines = format == null ? List.of() : format.contextTooltip(context);
        return lines.isEmpty() ? DLTooltip.EMPTY : new DLTooltip(lines, style.tooltipMaxWidth);
    };

    ITextTooltipProvider NONE = (context, format, style) -> DLTooltip.EMPTY;

    DLTooltip tooltipFor(TextContext context, ITextFormatParser format, TextBoxStyle style);
}
