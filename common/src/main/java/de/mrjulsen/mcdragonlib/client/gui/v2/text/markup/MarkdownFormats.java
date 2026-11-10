package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.input.KeyStrokes;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.FencedCodeRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.HeadingRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.HorizontalRuleRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.ImageRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.ListRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.ParagraphRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.QuoteRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block.TableRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.AutoLinkRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.CodeSpanRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.DelimiterRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.EscapeRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.LinkRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.StyleBlockRule;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyles;

public final class MarkdownFormats {

    public static final int HIGHLIGHT_BACKGROUND = 0x40FFD700;

    public static final MarkdownParser STANDARD = standard().build();
    public static final MarkdownParser EXTENDED = extended().build();

    private MarkdownFormats() {
    }

    public static MarkdownParser.Builder standard() {
        return MarkdownParser.builder("Markdown")
                .blocks(FencedCodeRule.DEFAULT, HorizontalRuleRule.DEFAULT, QuoteRule.DEFAULT, HeadingRule.DEFAULT,
                        TableRule.DEFAULT, ImageRule.DEFAULT, ListRule.DEFAULT, ParagraphRule.DEFAULT)
                .inline(EscapeRule.BACKSLASH, CodeSpanRule.BACKTICK, LinkRule.IMAGE, LinkRule.LINK, AutoLinkRule.WEB)
                .inline(new DelimiterRule("**", TextStyles.BOLD),
                        new DelimiterRule("__", TextStyles.BOLD),
                        new DelimiterRule("~~", TextStyles.STRIKETHROUGH),
                        new DelimiterRule("*", TextStyles.ITALIC),
                        new DelimiterRule("_", TextStyles.ITALIC))
                .shortcut(TextStyles.BOLD.name(), KeyStrokes.control(GLFW.GLFW_KEY_B))
                .shortcut(TextStyles.ITALIC.name(), KeyStrokes.control(GLFW.GLFW_KEY_I))
                .shortcut(TextStyles.STRIKETHROUGH.name(), KeyStrokes.controlShift(GLFW.GLFW_KEY_X));
    }

    public static MarkdownParser.Builder extended() {
        return MarkdownParser.builder("Markdown (Extended)")
                .blocks(FencedCodeRule.DEFAULT, HorizontalRuleRule.DEFAULT, QuoteRule.DEFAULT, HeadingRule.DEFAULT,
                        TableRule.DEFAULT, ImageRule.DEFAULT, ListRule.DEFAULT, ParagraphRule.DEFAULT)
                .inline(EscapeRule.BACKSLASH, CodeSpanRule.BACKTICK, LinkRule.IMAGE, LinkRule.LINK, AutoLinkRule.WEB)
                .inline(StyleBlockRule.DEFAULT,
                        new DelimiterRule("++", TextStyles.UNDERLINED),
                        new DelimiterRule("@@", TextStyles.OBFUSCATED),
                        new DelimiterRule("^^", TextStyles.SHADOW),
                        new DelimiterRule("==", style -> style.withBackgroundColor(HIGHLIGHT_BACKGROUND), "highlight"),
                        new DelimiterRule("**", TextStyles.BOLD),
                        new DelimiterRule("__", TextStyles.BOLD),
                        new DelimiterRule("~~", TextStyles.STRIKETHROUGH),
                        new DelimiterRule("*", TextStyles.ITALIC),
                        new DelimiterRule("_", TextStyles.ITALIC))
                .shortcut(TextStyles.BOLD.name(), KeyStrokes.control(GLFW.GLFW_KEY_B))
                .shortcut(TextStyles.ITALIC.name(), KeyStrokes.control(GLFW.GLFW_KEY_I))
                .shortcut(TextStyles.UNDERLINED.name(), KeyStrokes.control(GLFW.GLFW_KEY_U))
                .shortcut(TextStyles.STRIKETHROUGH.name(), KeyStrokes.controlShift(GLFW.GLFW_KEY_X))
                .alignment(StyleBlockRule.DEFAULT);
    }
}
