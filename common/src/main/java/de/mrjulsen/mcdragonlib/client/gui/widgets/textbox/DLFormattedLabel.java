package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextDocument;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextRange;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LayoutEngine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LineLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextActionTarget;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.TextBoxClip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.TextBoxRenderContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.TextBoxRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.EVerticalAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

@SupportsEvents({
        DLFormattedLabel.LinkClickedEvent.class
})
public class DLFormattedLabel extends DLGuiComponent implements ITextActionTarget {

    public record LinkClickedEvent(String target) implements IEvent {}

    public static final String DEFAULT_ELLIPSIS = "...";
    public static final int UNLIMITED = 0;

    public static final String MENU_TRANSLATION_PREFIX = "gui." + DragonLib.MODID + ".textbox.";

    private static final int MEASURE_LINE_LIMIT = 4096;

    private final TextDocument document = new TextDocument();
    private final LayoutEngine layoutEngine;
    private final TextBoxRenderer renderer;

    private final List<IContextMenuContributor<DLFormattedLabel>> contextMenuContributors = new ArrayList<>();
    private DLContextMenu contextMenu;
    private TextContext menuContext = TextContext.NONE;

    private String hoveredLink;
    private float contentHeight;
    private int visibleRows = UNLIMITED;
    private boolean truncated;

    public final Property<String> text = new Property<>("");
    public final BooleanProperty lineWrap = new BooleanProperty(true);
    public final BooleanProperty autoHeight = new BooleanProperty(false);
    public final Property<Padding> padding = new Property<>(Padding.ZERO);
    public final Property<ETextAlignment> align = new Property<>(ETextAlignment.LEFT);
    public final Property<EVerticalAlignment> verticalAlign = new Property<>(EVerticalAlignment.TOP);
    public final NumberProperty<Float> lineHeight = new NumberProperty<>(LayoutEngine.DEFAULT_LINE_HEIGHT, LayoutEngine.MIN_LINE_HEIGHT, LayoutEngine.MAX_LINE_HEIGHT);
    public final NumberProperty<Float> lineSpacing = new NumberProperty<>(0.0F, 0.0F, 256.0F);
    public final NumberProperty<Integer> maxLines = new NumberProperty<>(UNLIMITED, UNLIMITED, Integer.MAX_VALUE);
    public final BooleanProperty limitToHeight = new BooleanProperty(true);
    public final Property<String> ellipsis = new Property<>(DEFAULT_ELLIPSIS);
    public final Property<TextStyle> baseStyle = new Property<>(TextStyle.DEFAULT);
    public final Property<TextBoxStyle> componentRenderer = new Property<>(TextBoxStyle.VANILLA);
    public final BooleanProperty drawBackground = new BooleanProperty(false);
    public final NumberProperty<Integer> indentWidth = new NumberProperty<>(12, 0, 256);
    public final Property<TextFormat> format = new Property<>(TextFormat.MARKDOWN_EXTENDED);
    public final BooleanProperty contextMenuEnabled = new BooleanProperty(true);
    public final BooleanProperty copyEnabled = new BooleanProperty(false);
    public final Property<ITextTooltipProvider> tooltipProvider = new Property<>(ITextTooltipProvider.DEFAULT);

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

        text.withAfterPropertyChangedCallback((o, v) -> setSource(v));
        lineWrap.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        autoHeight.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        padding.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
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
        componentRenderer.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setStyle(v);
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

        addEventListener(DLGuiStandardEvents.MouseMoveEvent.class, (src, e) -> onMouseMoved(e.mouseX(), e.mouseY()));
        addEventListener(DLGuiStandardEvents.MouseDownEvent.class, (src, e) -> onMousePressed(e.button()));
        addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, (src, e) -> {
            refreshLayout();
            return false;
        });
        addEventListener(DLGuiStandardEvents.MouseLeaveEvent.class, (src, e) -> {
            clearHover();
            return false;
        });
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (src, e) -> {
            clearHover();
            return false;
        });
        addEventListener(DLGuiStandardEvents.RightClickEvent.class, (src, e) -> {
            openContextMenu(contextAt(e.mouseX(), e.mouseY()));
            return false;
        });

        addContextMenuContributor(DLFormattedLabel::addDefaultContextMenuEntries);
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

    public int textLeft() {
        return padding.get().left();
    }

    public int textTop() {
        return padding.get().top() + Math.round(verticalOffset());
    }

    public int textWidth() {
        return Math.max(0, width() - padding.get().left() - padding.get().right());
    }

    public int textHeight() {
        return Math.max(0, height() - padding.get().top() - padding.get().bottom());
    }

    public float contentHeight() {
        return contentHeight;
    }

    public float contentWidth() {
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

    protected void refreshLayout() {
        layoutEngine.setWrapWidth(lineWrap.get() ? Math.max(1, textWidth()) : LayoutEngine.NO_WRAP);
        layoutEngine.setViewportWidth(textWidth());
        measure();

        if (autoHeight.get()) {
            int measured = Math.round(contentHeight) + padding.get().top() + padding.get().bottom();
            if (measured != height()) {
                setHeight(measured);
            }
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
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (drawBackground.get()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, enabled.get() ? DLTextBox.TextBoxState.NORMAL : DLTextBox.TextBoxState.DISABLED);
        }

        Rectangle outer = TextBoxClip.clampToScreen(renderBounds);
        Rectangle inner = Rectangle.intersection(outer, textAreaOnScreen());
        if (TextBoxClip.isEmpty(inner)) {
            return;
        }

        GuiUtils.enableScissor(graphics, inner);
        renderer.render(graphics, createRenderContext(inner, inner));
        GuiUtils.enableScissor(graphics, outer);
    }

    protected Rectangle textAreaOnScreen() {
        double scale = getGlobalScale();
        return Rectangle.withSize(
                getXOnScreen() + textLeft() * scale,
                getYOnScreen() + padding.get().top() * scale,
                Math.max(0, textWidth() * scale),
                Math.max(0, textHeight() * scale)
        );
    }

    protected TextBoxRenderContext createRenderContext() {
        return createRenderContext(null, null);
    }

    protected TextBoxRenderContext createRenderContext(Rectangle clip, Rectangle textClip) {
        return new TextBoxRenderContext(
                textLeft(), textTop(), textWidth(), textHeight(),
                0.0D, 0.0D,
                -1, TextRange.EMPTY,
                false, enabled.get(),
                false, false,
                hoveredLink, null, null, null,
                clip, textClip,
                visibleRows, truncated ? ellipsis.get() : null
        );
    }

    protected boolean onMouseMoved(double localX, double localY) {
        TextContext context = contextAt(localX, localY);
        StyledSpan span = context.span();
        if (span == null) {
            clearHover();
            return false;
        }

        hoveredLink = span.style().hasLink() ? span.style().link() : null;
        ITextTooltipProvider provider = tooltipProvider.get();
        DLTooltip hint = provider == null
                ? DLTooltip.EMPTY
                : provider.tooltipFor(context, parser(), componentRenderer.get());
        tooltip.set(hint == null ? DLTooltip.EMPTY : hint);
        cursor.set(hoveredLink != null ? CursorType.HAND : null);
        return false;
    }

    public void clearHover() {
        hoveredLink = null;
        tooltip.set(DLTooltip.EMPTY);
        cursor.set(null);
    }

    public void addContextMenuContributor(IContextMenuContributor<DLFormattedLabel> contributor) {
        contextMenuContributors.add(contributor);
    }

    public boolean removeContextMenuContributor(IContextMenuContributor<DLFormattedLabel> contributor) {
        return contextMenuContributors.remove(contributor);
    }

    public TextContext contextAt(double localX, double localY) {
        if (!isInsideContent(localY)) {
            return TextContext.NONE;
        }
        int line = layoutEngine.heights().lineAtOffset((float) (localY - textTop()));
        return new TextContext(localX, localY, line, document.lineStart(line), document.getLine(line), renderer.spanAt(createRenderContext(), localX, localY), layoutEngine.layoutOf(line).parsed());
    }

    public void openContextMenu() {
        openContextMenu(TextContext.NONE);
    }

    public void openContextMenu(TextContext context) {
        if (!contextMenuEnabled.get() || getWindowManager() == null) {
            return;
        }
        this.menuContext = context == null ? TextContext.NONE : context;
        clearHover();
        if (!hasContextMenuEntries(buildContextMenu(0, 0))) {
            return;
        }
        if (contextMenu == null) {
            contextMenu = new DLContextMenu(this::buildContextMenu);
        }
        contextMenu.open(getWindowManager(), (int) getWindowManager().mouseXOnScreen(), (int) getWindowManager().mouseYOnScreen());
    }

    public TextContext menuContext() {
        return menuContext;
    }

    protected static boolean hasContextMenuEntries(List<DLContextMenu.ItemEntry> entries) {
        for (DLContextMenu.ItemEntry entry : entries) {
            if (entry != DLContextMenu.ItemEntry.SEPARATOR) {
                return true;
            }
        }
        return false;
    }

    protected List<DLContextMenu.ItemEntry> buildContextMenu(int x, int y) {
        List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
        for (IContextMenuContributor<DLFormattedLabel> contributor : contextMenuContributors) {
            contributor.contribute(this, menuContext, entries);
        }
        return entries;
    }

    protected void addDefaultContextMenuEntries(TextContext context, List<DLContextMenu.ItemEntry> entries) {
        List<TextAction> contextActions = parser().contextActions(context);
        for (TextAction action : contextActions) {
            entries.add(new DLContextMenu.ItemEntry(action.label(), DLSprite.empty(), true, () -> action.action().accept(this), null));
        }
        if (!copyEnabled.get()) {
            return;
        }
        if (!contextActions.isEmpty()) {
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
        }
        entries.add(menuEntry("copy", !document.isEmpty(), this::copyPlainText));
        entries.add(menuEntry("copy_source", !document.isEmpty(), this::copyText));
    }

    protected DLContextMenu.ItemEntry menuEntry(String key, boolean enabled, Runnable action) {
        return new DLContextMenu.ItemEntry(TextUtils.translate(MENU_TRANSLATION_PREFIX + key), DLSprite.empty(), enabled, action, null);
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

    @Override
    public void updateScreenLayout() {
        layoutEngine.setMeasurer(TextMeasurer.ofDefaultFont());
        refreshLayout();
    }
}
