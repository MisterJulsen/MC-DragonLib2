package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.IKeyStroke;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block.BlockContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block.IBlockRule;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline.IInlineRule;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline.InlineParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import net.minecraft.network.chat.Component;

public final class MarkdownParser implements ITextFormatParser {

    private final String name;
    private final List<IBlockRule> blockRules;
    private final InlineParser inlineParser;
    private final ILineAlignment lineAlignment;
    private final int spacesPerIndent;
    private final List<TextAction> formatActions;
    private final String reservedCharacters;
    private final String lineStartReservedCharacters;
    private final Map<BlockKind, IBlockLayout> blockLayouts = new HashMap<>();
    public int markupColor = 0xFFA0A0A0;
    public int markupAlpha = 0x80;
    private final Map<BlockKind, IBlockDecorator> blockDecorators = new HashMap<>();

    private MarkdownParser(Builder builder) {
        this.name = builder.name;
        this.blockRules = List.copyOf(builder.blockRules);
        this.inlineParser = new InlineParser(builder.inlineRules);
        this.lineAlignment = builder.lineAlignment;
        this.spacesPerIndent = builder.spacesPerIndent;

        List<TextAction> inlineActions = new ArrayList<>();
        StringBuilder inlineReserved = new StringBuilder();
        for (IInlineRule rule : inlineParser.rules()) {
            inlineActions.addAll(rule.formatActions());
            appendDistinct(inlineReserved, rule.reservedCharacters());
        }

        List<TextAction> blockActions = new ArrayList<>();
        StringBuilder blockReserved = new StringBuilder();
        for (IBlockRule rule : blockRules) {
            blockActions.addAll(rule.formatActions());
            appendDistinct(blockReserved, rule.reservedCharacters());
        }

        List<TextAction> actions = new ArrayList<>(inlineActions);
        if (!inlineActions.isEmpty() && !blockActions.isEmpty()) {
            actions.add(TextAction.SEPARATOR);
        }
        actions.addAll(blockActions);

        this.formatActions = withShortcuts(actions, builder.shortcuts);
        this.reservedCharacters = inlineReserved.toString();
        this.lineStartReservedCharacters = blockReserved.toString();
    }

    private static List<TextAction> withShortcuts(List<TextAction> actions, Map<String, IKeyStroke> shortcuts) {
        if (shortcuts.isEmpty()) {
            return List.copyOf(actions);
        }
        List<TextAction> result = new ArrayList<>(actions.size());
        for (TextAction action : actions) {
            TextAction resolved = action.hasChildren()
                    ? TextAction.submenu(action.id(), withShortcuts(action.children(), shortcuts))
                    : action;
            IKeyStroke stroke = shortcuts.get(resolved.id());
            result.add(stroke == null ? resolved : resolved.withShortcut(stroke));
        }
        return List.copyOf(result);
    }

    private static void appendDistinct(StringBuilder target, String characters) {
        for (int i = 0; i < characters.length(); i++) {
            char c = characters.charAt(i);
            if (target.indexOf(String.valueOf(c)) < 0) {
                target.append(c);
            }
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public String name() {
        return name;
    }

    public List<IBlockRule> blockRules() {
        return blockRules;
    }

    public InlineParser inlineParser() {
        return inlineParser;
    }

    @Override
    public ParsedLine parseLine(CharSequence line, int incomingState) {
        BlockContext context = new BlockContext(line, incomingState, inlineParser, spacesPerIndent, MarkupText.dimmed(markupColor, markupAlpha));
        for (IBlockRule rule : blockRules) {
            ParsedLine parsed = rule.parse(context);
            if (parsed != null) {
                return aligned(parsed, line, context.contentStart());
            }
            context.reset();
        }
        return ParsedLine.paragraph(Collections.emptyList());
    }

    @Override
    public int nextState(CharSequence line, int incomingState) {
        int contentStart = MarkupText.indentOf(line);
        for (IBlockRule rule : blockRules) {
            int state = rule.nextState(line, contentStart, incomingState);
            if (state != IBlockRule.STATE_UNCHANGED) {
                return state;
            }
        }
        int state = incomingState;
        for (IBlockRule rule : blockRules) {
            state = rule.resetState(state);
        }
        return state;
    }

    @Override
    public IBlockLayout blockLayout(BlockKind kind) {
        if (blockLayouts.containsKey(kind)) {
            return blockLayouts.get(kind);
        }
        IBlockLayout layout = null;
        for (IBlockRule rule : blockRules) {
            layout = rule.layoutOf(kind);
            if (layout != null) {
                break;
            }
        }
        blockLayouts.put(kind, layout);
        return layout;
    }

    @Override
    public IBlockDecorator blockDecorator(BlockKind kind) {
        if (blockDecorators.containsKey(kind)) {
            return blockDecorators.get(kind);
        }
        IBlockDecorator decorator = null;
        for (IBlockRule rule : blockRules) {
            decorator = rule.decoratorOf(kind);
            if (decorator != null) {
                break;
            }
        }
        blockDecorators.put(kind, decorator);
        return decorator;
    }

    @Override
    public String indentUnit() {
        return " ".repeat(spacesPerIndent);
    }

    @Override
    public String continuationPrefix(ParsedLine line) {
        String indent = " ".repeat(Math.max(0, line.indent()) * spacesPerIndent);
        for (IBlockRule rule : blockRules) {
            String prefix = rule.continuationPrefix(line, indent);
            if (prefix != null) {
                return prefix;
            }
        }
        return "";
    }

    @Override
    public String inlineDelimiter(StyleFlag flag) {
        return inlineParser.delimiterFor(flag);
    }

    @Override
    public List<TextAction> additionalActions(DLTextBox textBox) {
        ArrayList<TextAction> actions = new ArrayList<>(formatActions);
        actions.add(0, TextAction.menu("paste_plain", target -> textBox.pastePlainFromClipboard()));
        actions.add(0, TextAction.menu("copy_plain", target -> textBox.copySelectionAsPlainText()));
        return actions;
    }

    @Override
    public List<TextAction> contextActions(TextContext context) {
        List<TextAction> actions = new ArrayList<>();
        for (IInlineRule rule : inlineParser.rules()) {
            addDistinct(actions, rule.contextActions(context));
        }
        for (IBlockRule rule : blockRules) {
            addDistinct(actions, rule.contextActions(context));
        }
        return actions;
    }

    @Override
    public List<Component> contextTooltip(TextContext context) {
        for (IInlineRule rule : inlineParser.rules()) {
            List<Component> tooltip = rule.contextTooltip(context);
            if (!tooltip.isEmpty()) {
                return tooltip;
            }
        }
        for (IBlockRule rule : blockRules) {
            List<Component> tooltip = rule.contextTooltip(context);
            if (!tooltip.isEmpty()) {
                return tooltip;
            }
        }
        return List.of();
    }

    private static void addDistinct(List<TextAction> target, List<TextAction> actions) {
        for (TextAction action : actions) {
            boolean known = false;
            for (TextAction existing : target) {
                if (!action.id().isEmpty() && existing.id().equals(action.id())) {
                    known = true;
                    break;
                }
            }
            if (!known) {
                target.add(action);
            }
        }
    }

    @Override
    public String reservedCharacters() {
        return reservedCharacters;
    }

    @Override
    public String lineStartReservedCharacters() {
        return lineStartReservedCharacters;
    }

    @Override
    public MarkupSnippet styleBlock(String directives, String content) {
        return inlineParser.styleBlock(directives, content);
    }

    private ParsedLine aligned(ParsedLine parsed, CharSequence line, int contentStart) {
        if (lineAlignment == null || !parsed.kind().allowsAlignment()) {
            return parsed;
        }
        ETextAlignment alignment = lineAlignment.alignmentOf(line, contentStart, line.length());
        return alignment == null ? parsed : parsed.withAlign(alignment);
    }

    public static final class Builder {

        private final String name;
        private final List<IBlockRule> blockRules = new ArrayList<>();
        private final List<IInlineRule> inlineRules = new ArrayList<>();
        private final Map<String, IKeyStroke> shortcuts = new LinkedHashMap<>();
        private ILineAlignment lineAlignment;
        private int spacesPerIndent = 2;

        private Builder(String name) {
            this.name = name;
        }

        public Builder blocks(IBlockRule... rules) {
            Collections.addAll(blockRules, rules);
            return this;
        }

        public Builder inline(IInlineRule... rules) {
            Collections.addAll(inlineRules, rules);
            return this;
        }

        public Builder shortcut(String actionId, IKeyStroke stroke) {
            shortcuts.put(actionId, stroke);
            return this;
        }

        public Builder alignment(ILineAlignment alignment) {
            this.lineAlignment = alignment;
            return this;
        }

        public Builder spacesPerIndent(int spaces) {
            this.spacesPerIndent = Math.max(1, spaces);
            return this;
        }

        public MarkdownParser build() {
            return new MarkdownParser(this);
        }
    }
}
