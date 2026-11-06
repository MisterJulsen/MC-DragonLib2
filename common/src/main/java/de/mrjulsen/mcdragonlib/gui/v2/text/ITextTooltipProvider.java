package de.mrjulsen.mcdragonlib.gui.v2.text;

import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.Tooltip;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.TextContext;
import de.mrjulsen.mcdragonlib.gui.v2.text.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.Component;

/**
 * Decides which tooltip the text under the mouse gets, see
 * de.mrjulsen.mcdragonlib.gui.v2.widget.DLTextBox#tooltipProvider.
 */
@FunctionalInterface
public interface ITextTooltipProvider {

    /**
     * Only the tooltip the markup itself set on the span, ignoring what the format could add.
     */
    ITextTooltipProvider STYLE_ONLY = (context, format, style) -> {
        String tooltip = context == null ? null : context.tooltip();
        return tooltip == null ? Tooltip.EMPTY : new Tooltip(List.of(TextUtils.text(tooltip)), style.tooltipMaxWidth);
    };

    /**
     * The span's own tooltip, falling back to whatever the format offers for that context, e.g. the
     * target of a link.
     */
    ITextTooltipProvider DEFAULT = (context, format, style) -> {
        Tooltip own = STYLE_ONLY.tooltipFor(context, format, style);
        if (!own.isEmpty()) {
            return own;
        }
        List<Component> lines = format == null ? List.of() : format.contextTooltip(context);
        return lines.isEmpty() ? Tooltip.EMPTY : new Tooltip(lines, style.tooltipMaxWidth);
    };

    /**
     * Never shows a tooltip.
     */
    ITextTooltipProvider NONE = (context, format, style) -> Tooltip.EMPTY;

    /**
     * @return the tooltip for that position, or {@link Tooltip#EMPTY} for none. Never null.
     */
    Tooltip tooltipFor(TextContext context, ITextFormatParser format, TextBoxStyle style);
}
