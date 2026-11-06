package de.mrjulsen.mcdragonlib.gui.v2.widget;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Insets;
import de.mrjulsen.mcdragonlib.gui.v2.Tooltip;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.text.ITextTooltipProvider;
import de.mrjulsen.mcdragonlib.gui.v2.text.TextFormat;
import de.mrjulsen.mcdragonlib.gui.v2.text.document.TextDocument;
import de.mrjulsen.mcdragonlib.gui.v2.text.document.TextRange;
import de.mrjulsen.mcdragonlib.gui.v2.text.layout.LayoutEngine;
import de.mrjulsen.mcdragonlib.gui.v2.text.layout.LineLayout;
import de.mrjulsen.mcdragonlib.gui.v2.text.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.gui.v2.text.layout.VisualRow;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.ITextActionTarget;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.gui.v2.text.markup.TextContext;
import de.mrjulsen.mcdragonlib.gui.v2.text.render.TextBoxRenderContext;
import de.mrjulsen.mcdragonlib.gui.v2.text.render.TextBoxRenderer;
import de.mrjulsen.mcdragonlib.gui.v2.text.style.StyleFlag;
import de.mrjulsen.mcdragonlib.gui.v2.text.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.gui.v2.text.style.TextStyle;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.EVerticalAlignment;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

/**
 * A read only label that renders markup: headings, lists, tables, links and the rest of what the
 * {@link TextFormat} parsers understand.
 *
 * <p>It is the display-only counterpart of {@link DLTextBox}: no caret, no selection and no scrolling.
 * Text that does not fit is cut off and marked with an {@link #ellipsis}, see {@link #isTruncated()}.
 * With {@link #autoHeight} the label resizes itself to its content instead.</p>
 */
public class DLFormattedLabel extends GuiComponent implements ITextActionTarget {

    public record LinkClickedEvent(String target) implements IEvent {}

    public static final String DEFAULT_ELLIPSIS = "...";
    public static final int UNLIMITED = 0;

    private static final int MEASURE_LINE_LIMIT = 4096;

    private final TextDocument document = new TextDocument();
    private final LayoutEngine layoutEngine;
    private final TextBoxRenderer renderer;

    private String hoveredLink;
    private Tooltip hoveredTooltip = Tooltip.EMPTY;
    private float contentHeight;
    private int visibleRows = UNLIMITED;
    private boolean truncated;

    public final Property<String> text = new Property<>("");
    public final BooleanProperty lineWrap = new BooleanProperty(true);
    /**
     * Whether the label sets its own height to fit the text. Takes precedence over
     * {@link #limitToHeight}.
     */
    public final BooleanProperty autoHeight = new BooleanProperty(false);
    public final Property<ETextAlignment> align = new Property<>(ETextAlignment.LEFT);
    public final Property<EVerticalAlignment> verticalAlign = new Property<>(EVerticalAlignment.TOP);
    public final NumberProperty<Float> lineHeight = new NumberProperty<>(LayoutEngine.DEFAULT_LINE_HEIGHT, LayoutEngine.MIN_LINE_HEIGHT, LayoutEngine.MAX_LINE_HEIGHT);
    public final NumberProperty<Float> lineSpacing = new NumberProperty<>(0.0F, 0.0F, 256.0F);
    /**
     * The largest number of rows drawn, or {@link #UNLIMITED} for as many as fit.
     */
    public final NumberProperty<Integer> maxLines = new NumberProperty<>(UNLIMITED, UNLIMITED, Integer.MAX_VALUE);
    /**
     * Whether the text is cut off at the label's height.
     */
    public final BooleanProperty limitToHeight = new BooleanProperty(true);
    public final Property<String> ellipsis = new Property<>(DEFAULT_ELLIPSIS);
    public final Property<TextStyle> baseStyle = new Property<>(TextStyle.DEFAULT);
    public final BooleanProperty drawBackground = new BooleanProperty(false);
    public final NumberProperty<Integer> indentWidth = new NumberProperty<>(12, 0, 256);
    public final Property<TextFormat> format = new Property<>(TextFormat.MARKDOWN_EXTENDED);
    /**
     * Whether {@link #copyText()} and {@link #copyPlainText()} do anything.
     */
    public final BooleanProperty copyEnabled = new BooleanProperty(false);
    public final Property<ITextTooltipProvider> tooltipProvider = new Property<>(ITextTooltipProvider.DEFAULT);

    /**
     * Creates a label that sizes its own height to the text.
     */
    public DLFormattedLabel(int x, int y, int width) {
        this(x, y, width, 0);
        autoHeight.set(true);
    }

    public DLFormattedLabel(int x, int y, int width, int height) {
        super(x, y, width, height);
        document.setReadOnly(true);

        this.layoutEngine = new LayoutEngine(document, TextMeasurer.ofDefaultFont(), TextFormat.MARKDOWN_EXTENDED.parser(), TextBoxStyle.VANILLA);
        this.layoutEngine.setShowMarkup(false);
        this.renderer = createRenderer(document, layoutEngine);

        padding.set(Insets.ZERO);
        Tooltip.attach(this, () -> hoveredTooltip);

        text.withAfterPropertyChangedCallback((o, v) -> setSource(v));
        lineWrap.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        autoHeight.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        padding.addChangeListener((o, v) -> refreshLayout());
        maxLines.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        lineHeight.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setLineHeight(v);
            refreshLayout();
        });
        lineSpacing.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setLineSpacing(v);
            refreshLayout();
        });
        limitToHeight.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        ellipsis.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        verticalAlign.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        align.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setDefaultAlignment(v);
            refreshLayout();
        });
        baseStyle.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setBaseStyle(v);
            refreshLayout();
        });
        indentWidth.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setIndentWidth(v);
            refreshLayout();
        });
        format.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setParser((v == null ? TextFormat.PLAIN : v).parser());
            refreshLayout();
        });

        addEventListener(GuiEvents.MouseMoveEvent.class, (src, e) -> onMouseMoved(e.localX(src), e.localY(src)));
        addEventListener(GuiEvents.MouseDownEvent.class, (src, e) -> {
            if (onMousePressed(e.button())) {
                e.consume();
            }
        });
        addEventListener(GuiEvents.BoundsChangedEvent.class, (src, e) -> {
            if (e.sizeChanged()) {
                refreshLayout();
            }
        });
        addEventListener(GuiEvents.MouseLeaveEvent.class, (src, e) -> clearHover());
        addEventListener(GuiEvents.ScrollEvent.class, (src, e) -> clearHover());

        refreshLayout();
    }

    protected TextBoxRenderer createRenderer(TextDocument document, LayoutEngine layout) {
        return new TextBoxRenderer(document, layout);
    }

    public void setText(String source) {
        text.set(source);
    }

    private void setSource(String source) {
        clearHover();
        document.setReadOnly(false);
        document.setText(source == null ? "" : source);
        document.setReadOnly(true);
        layoutEngine.invalidateAll();
        refreshLayout();
    }

    public void load(ResourceLocation location) {
        String txt = Minecraft.getInstance().getResourceManager().getResource(location).map(x -> {
            try (InputStream io = x.open()) {
                return new String(io.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                DragonLib.LOGGER.warn("Failed to load resource at {}", location, e);
            }
            return null;
        }).orElse("");
        setText(txt);
    }

    public TextDocument document() {
        return document;
    }

    public LayoutEngine layout() {
        return layoutEngine;
    }

    public ITextFormatParser parser() {
        return layoutEngine.parser();
    }

    public TextStyle styleAt(int offset) {
        return layoutEngine.styleAt(offset);
    }

    public TextBoxRenderer renderer() {
        return renderer;
    }

    /**
     * The colors and metrics in effect, resolved from the theme.
     */
    public TextBoxStyle boxStyle() {
        return getThemeValue(TextBoxStyle.THEME_KEY);
    }

    protected Insets insets() {
        Insets value = padding.get();
        return value == null ? Insets.ZERO : value;
    }

    public int textLeft() {
        return (int) insets().left();
    }

    public int textTop() {
        return (int) insets().top() + Math.round(verticalOffset());
    }

    public int textWidth() {
        return Math.max(0, width() - (int) insets().horizontal());
    }

    public int textHeight() {
        return Math.max(0, height() - (int) insets().vertical());
    }

    public float textContentHeight() {
        return contentHeight;
    }

    public float textContentWidth() {
        return layoutEngine.widestLine();
    }

    public String getPlainText() {
        return layoutEngine.plainText(0, document.length());
    }

    public void copyPlainText() {
        if (copyEnabled.get()) {
            setClipboard(getPlainText());
        }
    }

    public void copyText() {
        if (copyEnabled.get()) {
            setClipboard(document.getText());
        }
    }

    @Override
    public void openLink(String link) {
        if (link != null && !link.isEmpty()) {
            invokeEvent(this, new LinkClickedEvent(link), true);
        }
    }

    @Override
    public void copyToClipboard(String text) {
        setClipboard(text);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public String getSelectedText() {
        return "";
    }

    @Override
    public boolean toggleInlineStyle(StyleFlag flag) {
        return false;
    }

    @Override
    public void toggleInlineMarkup(String prefix, String suffix) {
    }

    @Override
    public void toggleLinePrefix(String prefix) {
    }

    @Override
    public void applyStyleBlock(String directives) {
    }

    @Override
    public void insertText(String text) {
    }

    @Override
    public void replaceRange(int from, int to, String text) {
    }

    protected static void setClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(text);
        }
    }

    /**
     * Whether the text did not fit and was cut off.
     */
    public boolean isTruncated() {
        return truncated;
    }

    public int visibleRowCount() {
        return visibleRows;
    }

    protected float verticalOffset() {
        if (autoHeight.get()) {
            return 0.0F;
        }
        return Math.max(0.0F, verticalAlign.get().offset(textHeight(), contentHeight));
    }

    /**
     * Re-fits the layout engine to the current width, re-measures the content and, with
     * {@link #autoHeight}, resizes the label.
     */
    protected void refreshLayout() {
        syncStyle();
        syncMeasurer();
        layoutEngine.setWrapWidth(lineWrap.get() ? Math.max(1, textWidth()) : LayoutEngine.NO_WRAP);
        layoutEngine.setViewportWidth(textWidth());
        measure();

        if (autoHeight.get()) {
            int measured = Math.round(contentHeight) + (int) insets().vertical();
            if (measured != height()) {
                setHeight(measured);
            }
        }
    }

    /**
     * Pushes the theme's style into the layout engine, which needs it for measuring.
     */
    protected void syncStyle() {
        TextBoxStyle style = boxStyle();
        if (layoutEngine.style() != style) {
            layoutEngine.setStyle(style);
        }
    }

    /**
     * Picks up a new font instance, e.g. after a resource reload.
     */
    protected void syncMeasurer() {
        Font font = Minecraft.getInstance().font;
        if (layoutEngine.measurer().font() != font) {
            layoutEngine.setMeasurer(new TextMeasurer(font));
        }
    }

    protected void measure() {
        int rowLimit = maxLines.get() > UNLIMITED ? maxLines.get() : Integer.MAX_VALUE;
        float available = autoHeight.get() || !limitToHeight.get() ? Float.MAX_VALUE : textHeight();
        int lineCount = Math.min(document.lineCount(), MEASURE_LINE_LIMIT);

        float used = 0.0F;
        int rows = 0;
        boolean cut = false;

        for (int line = 0; line < lineCount && !cut; line++) {
            LineLayout lineLayout = layoutEngine.layoutOf(line);
            for (VisualRow row : lineLayout.rows()) {
                if (rows >= rowLimit || used + row.height() > available) {
                    cut = true;
                    break;
                }
                used += row.height();
                rows++;
            }
        }

        if (!cut && document.lineCount() > MEASURE_LINE_LIMIT) {
            used = layoutEngine.heights().totalHeight();
            rows = UNLIMITED;
        }

        this.contentHeight = used;
        this.visibleRows = cut ? rows : UNLIMITED;
        this.truncated = cut;
    }

    /**
     * The height the full text needs, ignoring {@link #maxLines} and the label's own height.
     */
    public float measureContentHeight() {
        int lines = document.lineCount();
        if (lines > MEASURE_LINE_LIMIT) {
            return layoutEngine.heights().totalHeight();
        }
        float total = 0.0F;
        for (int line = 0; line < lines; line++) {
            total += layoutEngine.layoutOf(line).height();
        }
        return total;
    }

    @Override
    protected Size measureContent(double availableWidth, double availableHeight) {
        return Size.of(Math.max(0, textContentWidth()), Math.max(0, measureContentHeight()));
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        syncStyle();
        if (drawBackground.get()) {
            GuiUtils.withTint(getThemeValue(DLTextBox.THEME_KEY_BACKGROUND_COLOR), () ->
                    getThemeValue(DLTextBox.THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight()));
        }

        Rectangle textClip = textAreaOnScreen();
        if (textClip.width() <= 0 || textClip.height() <= 0) {
            return;
        }

        g.pushClip(textClip);
        try {
            renderer.render(g, createRenderContext(textClip));
        } finally {
            g.popClip();
        }
    }

    protected Rectangle textAreaOnScreen() {
        return getTransform().toScreen(textLeft(), insets().top(),
                Math.max(0, textWidth()), Math.max(0, textHeight()));
    }

    protected TextBoxRenderContext createRenderContext() {
        return createRenderContext(null);
    }

    protected TextBoxRenderContext createRenderContext(Rectangle textClip) {
        return new TextBoxRenderContext(
                textLeft(), textTop(), textWidth(), textHeight(),
                0.0D, 0.0D,
                -1, TextRange.EMPTY,
                false, isEnabled(),
                false, false,
                hoveredLink, null, null, null,
                textClip,
                visibleRows, truncated ? ellipsis.get() : null
        );
    }

    protected void onMouseMoved(double localX, double localY) {
        TextContext context = contextAt(localX, localY);
        StyledSpan span = context.span();
        if (span == null) {
            clearHover();
            return;
        }

        hoveredLink = span.style().hasLink() ? span.style().link() : null;
        ITextTooltipProvider provider = tooltipProvider.get();
        Tooltip hint = provider == null ? Tooltip.EMPTY : provider.tooltipFor(context, parser(), boxStyle());
        hoveredTooltip = hint == null ? Tooltip.EMPTY : hint;
        cursor.set(hoveredLink != null ? CursorType.HAND : null);
    }

    public void clearHover() {
        hoveredLink = null;
        hoveredTooltip = Tooltip.EMPTY;
        cursor.set(null);
    }

    /**
     * What the text at that local position is, i.e. its line, span and parsed block.
     */
    public TextContext contextAt(double localX, double localY) {
        if (!isInsideContent(localY)) {
            return TextContext.NONE;
        }
        int line = layoutEngine.heights().lineAtOffset((float) (localY - textTop()));
        return new TextContext(localX, localY, line, document.lineStart(line), document.getLine(line),
                renderer.spanAt(createRenderContext(), localX, localY), layoutEngine.layoutOf(line).parsed());
    }

    protected boolean isInsideContent(double localY) {
        return localY >= textTop() && localY < textTop() + contentHeight;
    }

    protected boolean onMousePressed(int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || hoveredLink == null) {
            return false;
        }
        invokeEvent(this, new LinkClickedEvent(hoveredLink), true);
        return true;
    }

    public String hoveredLink() {
        return hoveredLink;
    }
}
