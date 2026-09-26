package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.autocomplete.DLAutocompleteWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.autocomplete.IAutocompletionManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.DocumentEdit;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextDocument;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextRange;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.Caret;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.KeyEvent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.KeyMap;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.KeyStrokes;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.HeightIndex;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LayoutEngine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LineLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.MaskTextTransform;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextGeometry;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextFormatParser;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ITextActionTarget;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupSnippet;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.TextBoxClip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.TextBoxRenderContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.TextBoxRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search.ISearchStrategy;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search.SearchHit;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search.SearchQuery;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.search.SearchSession;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

@SupportsEvents({
        DLTextBox.TextChangedEvent.class,
        DLTextBox.CaretMovedEvent.class,
        DLTextBox.SelectionChangedEvent.class,
        DLTextBox.LinkClickedEvent.class,
        DLTextBox.SearchRequestedEvent.class,
        DLTextBox.SearchResultEvent.class,
        DLTextBox.TextAcceptKeyPressedEvent.class,
        DLTextBox.TextCancelKeyPressedEvent.class
})
public class DLTextBox extends DLGuiComponent implements ITextActionTarget {

    public static enum TextBoxState {
        NORMAL,
        SELECTED,
        FOCUSED,
        DISABLED;
    }

    public record TextChangedEvent(TextDocument document, DocumentEdit edit) implements IEvent {}
    public record CaretMovedEvent(int offset, int line, int column) implements IEvent {}
    public record SelectionChangedEvent(TextRange selection) implements IEvent {}
    public record LinkClickedEvent(String target) implements IEvent {}
    public record SearchRequestedEvent(SearchQuery query) implements IEvent {}
    public record SearchResultEvent(SearchHit hit, int index, int total) implements IEvent {}

    public record TextAcceptKeyPressedEvent() implements IEvent {}

    public record TextCancelKeyPressedEvent() implements IEvent {}

    public static final double MULTI_CLICK_DISTANCE = 4.0D;
    protected static final char DELETE_CHARACTER = 127;
    protected static final double MIN_SCALE = 0.01D;
    protected static final double SCROLL_EPSILON = 0.001D;

    public static final String MENU_TRANSLATION_PREFIX = "gui." + DragonLib.MODID + ".textbox.";

    private final TextDocument document;
    private final LayoutEngine layoutEngine;
    private final TextBoxRenderer renderer;
    private final SearchSession searchSession;
    private final Caret caret = new Caret();
    private final KeyMap<DLTextBox> keyMap = new KeyMap<>();

    private final List<IContextMenuContributor<DLTextBox>> contextMenuContributors = new ArrayList<>();
    private DLContextMenu contextMenu;
    private DLAutocompleteWindow<?> autocompleteWindow;
    private TextContext menuContext = TextContext.NONE;
    private boolean dragging;

    private final DLScrollBar verticalScrollBar;
    private final DLScrollBar horizontalScrollBar;
    private boolean updatingScrollBars;

    private double scrollX;
    private double scrollY;
    private long lastCaretResetMs = System.currentTimeMillis();
    private String hoveredLink;
    private IntPredicate lineHighlight;
    private double lastClickX = Double.NaN;
    private double lastClickY = Double.NaN;
    private double previousClickX = Double.NaN;
    private double previousClickY = Double.NaN;

    public final Property<Component> placeholderText = new Property<>(TextUtils.empty());
    public final Property<Padding> padding = new Property<>(new Padding(4));
    public final BooleanProperty acceptAndCancelKeysEnabled = new BooleanProperty(false);
    public final Property<IAutocompletionManager<?>> autocompleteManager = new Property<IAutocompletionManager<?>>(null)
            .withAfterPropertyChangedCallback((o, v) -> closeAutocompleteWindow());
    public final BooleanProperty hideSelection = new BooleanProperty(true);
    public final BooleanProperty multiline = new BooleanProperty(false);
    public final BooleanProperty lineWrap = new BooleanProperty(false);
    public final BooleanProperty editable = new BooleanProperty(true);
    public final BooleanProperty showLineNumbers = new BooleanProperty(false);
    public final BooleanProperty drawBackground = new BooleanProperty(true);
    public final BooleanProperty showMarkup = new BooleanProperty(true);
    public final BooleanProperty showVerticalScrollBar = new BooleanProperty(false);
    public final BooleanProperty showHorizontalScrollBar = new BooleanProperty(false);
    public final BooleanProperty autoHideScrollBars = new BooleanProperty(true);
    public final BooleanProperty highlightSearchResults = new BooleanProperty(true);
    public final BooleanProperty highlightCurrentLine = new BooleanProperty(false);
    public final BooleanProperty password = new BooleanProperty(false);
    public final Property<Character> passwordCharacter = new Property<>(MaskTextTransform.BULLET);
    public final BooleanProperty contextMenuEnabled = new BooleanProperty(true);
    public final Property<ITextTooltipProvider> tooltipProvider = new Property<>(ITextTooltipProvider.DEFAULT);
    public final NumberProperty<Float> lineHeight = new NumberProperty<>(LayoutEngine.DEFAULT_LINE_HEIGHT, LayoutEngine.MIN_LINE_HEIGHT, LayoutEngine.MAX_LINE_HEIGHT);
    public final NumberProperty<Float> lineSpacing = new NumberProperty<>(0.0F, 0.0F, 256.0F);
    public final NumberProperty<Integer> scrollRowsPerNotch = new NumberProperty<>(3, 1, Integer.MAX_VALUE);
    public final NumberProperty<Integer> maxLength = new NumberProperty<>(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    public final Property<TextStyle> baseStyle = new Property<>(TextStyle.DEFAULT);
    public final Property<TextBoxStyle> componentRenderer = new Property<>(TextBoxStyle.VANILLA);
    public final NumberProperty<Integer> indentWidth = new NumberProperty<>(12, 0, 256);
    public final NumberProperty<Float> autoScrollSpeed = new NumberProperty<>(0.6F, 0.0F, 64.0F);
    public final NumberProperty<Float> autoScrollMaxStep = new NumberProperty<>(16.0F, 1.0F, 256.0F);
    public final NumberProperty<Integer> horizontalScrollCharacters = new NumberProperty<>(2, 1, 64);
    public final Property<TextFormat> format = new Property<>(TextFormat.PLAIN);

    public DLTextBox(int x, int y, int width, int height) {
        this(x, y, width, height, new TextDocument());
    }

    public DLTextBox(int x, int y, int width, int height, TextDocument document) {
        super(x, y, width, height);
        this.document = document;
        this.layoutEngine = new LayoutEngine(document, TextMeasurer.ofDefaultFont(),
                TextFormat.PLAIN.parser(), TextBoxStyle.VANILLA);
        this.renderer = createRenderer(document, layoutEngine);
        this.searchSession = new SearchSession(document);

        this.verticalScrollBar = createScrollBar(Orientation.VERTICAL);
        this.horizontalScrollBar = createScrollBar(Orientation.HORIZONTAL);

        document.addListener((doc, edit, firstLine, removed, inserted) -> {
            layoutEngine.onLinesChanged(firstLine, removed, inserted);
            searchSession.invalidate();
        });

        multiline.withAfterPropertyChangedCallback((o, v) -> {
            if (!v && document.lineCount() > 1) {
                setText(document.getText());
            }
            refreshLayout();
        });
        indentWidth.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setIndentWidth(v);
            refreshLayout();
        });
        baseStyle.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setBaseStyle(v);
            refreshLayout();
        });
        lineWrap.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        padding.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        showLineNumbers.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        editable.withAfterPropertyChangedCallback((o, v) -> document.setReadOnly(!v));
        showMarkup.withAfterPropertyChangedCallback((o, v) -> layoutEngine.setShowMarkup(v));
        lineHeight.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setLineHeight(v);
            refreshLayout();
        });
        lineSpacing.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setLineSpacing(v);
            refreshLayout();
        });
        componentRenderer.withAfterPropertyChangedCallback((o, v) -> {
            layoutEngine.setStyle(v);
            refreshLayout();
        });
        format.withAfterPropertyChangedCallback((o, v) ->
                layoutEngine.setParser((v == null ? TextFormat.PLAIN : v).parser()));
        password.withAfterPropertyChangedCallback((o, v) -> updateTextMask());
        passwordCharacter.withAfterPropertyChangedCallback((o, v) -> updateTextMask());
        showVerticalScrollBar.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        showHorizontalScrollBar.withAfterPropertyChangedCallback((o, v) -> refreshLayout());
        autoHideScrollBars.withAfterPropertyChangedCallback((o, v) -> refreshLayout());

        addEventListener(DLGuiStandardEvents.KeyPressEvent.class,
                (src, e) -> onKeyPressed(e.keyCode(), e.scanCode()));
        addEventListener(DLGuiStandardEvents.CharTypeEvent.class,
                (src, e) -> onCharTyped(e.codePoint()));
        addEventListener(DLGuiStandardEvents.MouseDownEvent.class,
                (src, e) -> onMousePressed(e.mouseX(), e.mouseY(), e.button()));
        addEventListener(DLGuiStandardEvents.MultiClickEvent.class,
                (src, e) -> onMultiClick(e.mouseX(), e.mouseY(), e.clickCount()));
        addEventListener(DLGuiStandardEvents.DragEvent.class,
                (src, e) -> onDragged(e.mouseX(), e.mouseY()));
        addEventListener(DLGuiStandardEvents.ScrollEvent.class,
                (src, e) -> onScrolled(e.deltaY()));
        addEventListener(DLGuiStandardEvents.MouseMoveEvent.class,
                (src, e) -> onMouseMoved(e.mouseX(), e.mouseY()));
        addEventListener(DLGuiStandardEvents.MouseLeaveEvent.class, (src, e) -> {
            clearHover();
            return false;
        });
        addEventListener(DLGuiStandardEvents.DragBeginEvent.class, (src, e) -> {
            dragging = e.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
            clearHover();
            return false;
        });
        addEventListener(DLGuiStandardEvents.DragEndEvent.class, (src, e) -> {
            dragging = false;
            return false;
        });
        addEventListener(DLGuiStandardEvents.RightClickEvent.class, (src, e) -> {
            openContextMenu(contextAt(e.mouseX(), e.mouseY()));
            return false;
        });
        addEventListener(TextChangedEvent.class, (src, e) -> {
            clearHover();
            return false;
        });
        addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, (src, e) -> {
            refreshLayout();
            return false;
        });

        this.multiClickable.set((byte) 3);
        this.cursor.set(CursorType.IBEAM);
        this.inputConsumptionPolicy.set(type ->
                type != ConsumptionType.SCROLL || (multiline.get() && contentHeight() > textHeight()));

        setupAutocomplete();
        registerDefaultKeyBindings();
        addContextMenuContributor(DLTextBox::addDefaultContextMenuEntries);
        refreshLayout();
    }

    @SuppressWarnings("unchecked")
    protected void setupAutocomplete() {
        addEventListener(TextChangedEvent.class, (src, e) -> {
            if (autocompleteWindow != null && autocompleteWindow.supressTextUpdate()) {
                return false;
            }
            if (document.isEmpty() || autocompleteManager.get() == null) {
                closeAutocompleteWindow();
            } else if (autocompleteWindow == null) {
                openAutocompleteWindow();
            }
            if (autocompleteWindow != null) {
                ((IAutocompletionManager<Object>) autocompleteManager.get())
                        .configureWindow((DLAutocompleteWindow<Object>) autocompleteWindow, this);
            }
            return false;
        });

        addEventListener(DLGuiStandardEvents.FocusChangedEvent.class, (src, e) -> {
            if (autocompleteManager.get() != null && e.focus() && autocompleteWindow == null) {
                openAutocompleteWindow();
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    protected void openAutocompleteWindow() {
        if (autocompleteManager.get() == null || getWindowManager() == null) {
            return;
        }
        IAutocompletionManager<Object> manager = (IAutocompletionManager<Object>) autocompleteManager.get();
        getWindowManager().createWindow(mgr -> {
            autocompleteWindow = manager.createWindow(mgr, this);
            autocompleteWindow.addEventListener(DLGuiStandardEvents.CloseEvent.class, (src, e) -> {
                autocompleteWindow = null;
                return false;
            });
            manager.configureWindow((DLAutocompleteWindow<Object>) autocompleteWindow, this);
            return autocompleteWindow;
        });
    }

    @SuppressWarnings("unchecked")
    protected void closeAutocompleteWindow() {
        if (autocompleteWindow == null) {
            return;
        }
        if (autocompleteManager.get() != null) {
            ((IAutocompletionManager<Object>) autocompleteManager.get())
                    .closeWindow((DLAutocompleteWindow<Object>) autocompleteWindow, this);
        }
        getWindowManager().closeWindow(autocompleteWindow);
    }

    public DLAutocompleteWindow<?> autocompleteWindow() {
        return autocompleteWindow;
    }

    protected TextBoxRenderer createRenderer(TextDocument document, LayoutEngine layout) {
        return new TextBoxRenderer(document, layout);
    }

    protected DLScrollBar createScrollBar(Orientation orientation) {
        DLScrollBar bar = new DLScrollBar(0, 0, 1, 1, orientation);
        bar.componentRenderer.set(VanillaListScrollBarRenderer.VANILLA_SCROLLBAR);
        bar.inputConsumptionPolicy.set(type -> true);
        bar.visible.set(false);
        bar.addEventListener(DLScrollBar.ValueChangedEvent.class, (src, e) -> {
            if (!updatingScrollBars) {
                if (orientation == Orientation.VERTICAL) {
                    setScrollY(e.value());
                } else {
                    setScrollX(e.value());
                }
            }
            return false;
        });
        addComponent(bar);
        return bar;
    }

    protected void registerDefaultKeyBindings() {
        keyMap.bind(event -> Screen.isSelectAll(event.keyCode()), (box, event) -> {
            box.selectAll();
            return true;
        });
        keyMap.bind(event -> Screen.isCopy(event.keyCode()), (box, event) -> {
            box.copySelection();
            return true;
        });
        keyMap.bind(event -> Screen.isCut(event.keyCode()), (box, event) -> {
            box.cutSelection();
            return true;
        });
        keyMap.bind(event -> Screen.isPaste(event.keyCode()), (box, event) -> {
            box.pasteFromClipboard();
            return true;
        });
        keyMap.bind(KeyStrokes.controlShift(GLFW.GLFW_KEY_V), (box, event) -> {
            box.pastePlainFromClipboard();
            return true;
        });

        keyMap.bind(KeyStrokes.controlLabelled('z', GLFW.GLFW_KEY_Z), (box, event) -> {
            box.undo();
            return true;
        });
        keyMap.bind(KeyStrokes.controlShiftLabelled('z', GLFW.GLFW_KEY_Z), (box, event) -> {
            box.redo();
            return true;
        });
        keyMap.bind(KeyStrokes.controlLabelled('y', GLFW.GLFW_KEY_Y), (box, event) -> {
            box.redo();
            return true;
        });

        keyMap.bind(KeyStrokes.control(GLFW.GLFW_KEY_F), (box, event) -> {
            box.invokeEvent(box, new SearchRequestedEvent(box.searchSession.query()), true);
            return true;
        });
        keyMap.bind(KeyStrokes.plain(GLFW.GLFW_KEY_F3), (box, event) -> box.findNext() != null);
        keyMap.bind(KeyStrokes.shift(GLFW.GLFW_KEY_F3), (box, event) -> box.findPrevious() != null);

        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_LEFT), (box, event) -> {
            int target = event.control()
                    ? document.previousWordBoundary(caret.offset())
                    : document.offsetBefore(caret.offset());
            box.moveCaretTo(box.collapseOrMove(target, event.shift(), true), event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_RIGHT), (box, event) -> {
            int target = event.control()
                    ? document.nextWordBoundary(caret.offset())
                    : document.offsetAfter(caret.offset());
            box.moveCaretTo(box.collapseOrMove(target, event.shift(), false), event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_UP), (box, event) -> {
            if (!box.multiline.get()) {
                return false;
            }
            box.moveCaretVertically(-1, event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_DOWN), (box, event) -> {
            if (!box.multiline.get()) {
                return false;
            }
            box.moveCaretVertically(1, event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_HOME), (box, event) -> {
            box.moveCaretTo(event.control() ? 0 : box.lineStartOfCaret(), event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_END), (box, event) -> {
            box.moveCaretTo(event.control() ? document.length() : box.lineEndOfCaret(), event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_PAGE_UP), (box, event) -> {
            if (!box.multiline.get()) {
                return false;
            }
            box.pageScroll(-1, event.shift());
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_PAGE_DOWN), (box, event) -> {
            if (!box.multiline.get()) {
                return false;
            }
            box.pageScroll(1, event.shift());
            return true;
        });

        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_BACKSPACE), (box, event) -> {
            if (!box.deleteSelection()) {
                int from = event.control()
                        ? document.previousWordBoundary(caret.offset())
                        : document.offsetBefore(caret.offset());
                box.deleteRange(from, caret.offset());
            }
            return true;
        });
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_DELETE), (box, event) -> {
            if (!box.deleteSelection()) {
                int to = event.control()
                        ? document.nextWordBoundary(caret.offset())
                        : document.offsetAfter(caret.offset());
                box.deleteRange(caret.offset(), to);
            }
            return true;
        });

        KeyMap.ICommand<DLTextBox> newLine = (box, event) -> {
            if (box.acceptAndCancelKeysEnabled.get() && box.isEditable() && !event.control() && !event.shift()) {
                box.invokeEvent(box, new TextAcceptKeyPressedEvent(), true);
                return true;
            }
            if (!box.multiline.get()) {
                return false;
            }
            if (box.clearEmptyBlock()) {
                return true;
            }
            box.insertText("\n" + box.continuationPrefix());
            return true;
        };
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_ENTER), newLine);
        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_KP_ENTER), newLine);

        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_ESCAPE), (box, event) -> {
            if (!box.acceptAndCancelKeysEnabled.get() || !box.isEditable()) {
                return false;
            }
            box.invokeEvent(box, new TextCancelKeyPressedEvent(), true);
            return true;
        });

        keyMap.bind(KeyStrokes.of(GLFW.GLFW_KEY_TAB), (box, event) -> {
            if (!box.multiline.get()) {
                return false;
            }
            box.handleTab(event.shift());
            return true;
        });
    }

    public KeyMap<DLTextBox> keyMap() {
        return keyMap;
    }

    public TextDocument document() {
        return document;
    }

    public LayoutEngine layout() {
        return layoutEngine;
    }

    public TextBoxRenderer renderer() {
        return renderer;
    }

    public Caret caret() {
        return caret;
    }

    public ITextFormatParser parser() {
        return layoutEngine.parser();
    }

    public DLScrollBar verticalScrollBar() {
        return verticalScrollBar;
    }

    public DLScrollBar horizontalScrollBar() {
        return horizontalScrollBar;
    }

    public String getText() {
        return document.getText();
    }

    public void setText(String text) {
        document.setText(sanitizeInput(text));
        caret.moveTo(0, false);
        scrollX = 0;
        scrollY = 0;
        layoutEngine.invalidateAll();
        searchSession.invalidate();
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

    public String getSelectedText() {
        return document.getText(document.snapToCodePoints(caret.selection()));
    }

    protected String sanitizeInput(String text) {
        if (text == null) {
            return "";
        }
        return multiline.get() ? text : toSingleLine(text);
    }

    public static String toSingleLine(String text) {
        return text.replace("\r\n", " ").replace('\r', ' ').replace('\n', ' ');
    }

    public String getPlainText() {
        return getPlainText(new TextRange(0, document.length()));
    }

    public String getPlainText(TextRange range) {
        TextRange clamped = document.snapToCodePoints(range);
        return layoutEngine.plainText(clamped.start(), clamped.end());
    }

    public void copySelection() {
        if (!isMasked() && caret.hasSelection()) {
            setClipboard(getSelectedText());
        }
    }

    public void copySelectionAsPlainText() {
        if (!isMasked() && caret.hasSelection()) {
            setClipboard(getPlainText(caret.selection()));
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
    public void replaceRange(int from, int to, String text) {
        if (!isEditable()) {
            return;
        }
        TextRange range = document.snapToCodePoints(TextRange.of(from, to));
        int caretBefore = caret.offset();
        int caretAfter = range.start() + text.length();

        DocumentEdit edit = document.replace(range.start(), range.end(), text, caretBefore, caretAfter);
        if (edit == null) {
            return;
        }
        if (caretBefore >= range.end()) {
            caret.moveTo(document.clampOffset(caretBefore + edit.lengthDelta()), false);
        } else if (caretBefore > range.start()) {
            caret.moveTo(document.clampOffset(caretAfter), false);
        }
        finishEdit(edit, false);
    }

    protected static void setClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(text);
        }
    }

    public void cutSelection() {
        copySelection();
        deleteSelection();
    }

    public void pasteFromClipboard() {
        insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
    }

    public void pastePlainFromClipboard() {
        insertText(parser().escapeLiteral(Minecraft.getInstance().keyboardHandler.getClipboard()));
    }

    public boolean isMasked() {
        return layoutEngine.textTransform() != null;
    }

    protected void updateTextMask() {
        Character mask = passwordCharacter.get();
        layoutEngine.setTextTransform(password.get() && mask != null ? new MaskTextTransform(mask) : null);
        refreshLayout();
    }

    public void setParser(ITextFormatParser parser) {
        layoutEngine.setParser(parser);
    }

    public void setLineHighlight(IntPredicate predicate) {
        this.lineHighlight = predicate;
    }

    public SearchSession search() {
        return searchSession;
    }

    public void setSearchQuery(SearchQuery query) {
        searchSession.setQuery(query);
        invokeEvent(this, new SearchResultEvent(searchSession.activeHit(), searchSession.activeIndex(),
                searchSession.count()), true);
    }

    public void setSearchPattern(String pattern) {
        setSearchQuery(searchSession.query().withPattern(pattern));
    }

    public void clearSearch() {
        searchSession.clear();
        invokeEvent(this, new SearchResultEvent(null, -1, 0), true);
    }

    public SearchHit findNext() {
        return selectHit(searchSession.findNext(Math.max(caret.offset(), caret.selection().end())));
    }

    public SearchHit findPrevious() {
        return selectHit(searchSession.findPrevious(Math.min(caret.offset(), caret.selection().start())));
    }

    protected SearchHit selectHit(SearchHit hit) {
        if (hit != null) {
            caret.select(hit.range());
            lastCaretResetMs = System.currentTimeMillis();
            scrollCaretIntoView();
            invokeEvent(this, new SelectionChangedEvent(caret.selection()), true);
        }
        invokeEvent(this, new SearchResultEvent(hit, searchSession.activeIndex(), searchSession.count()), true);
        return hit;
    }

    public boolean replaceCurrentMatch(String replacement) {
        if (!isEditable()) {
            return false;
        }
        SearchHit hit = searchSession.activeHit();
        if (hit == null) {
            hit = searchSession.findNext(caret.offset());
        }
        if (hit == null) {
            return false;
        }

        String text = expand(hit, replacement);
        int caretAfter = hit.start() + text.length();
        document.replace(hit.start(), hit.end(), text, caret.offset(), caretAfter);
        caret.moveTo(caretAfter, false);
        finishEdit(null);
        selectHit(searchSession.findNext(caretAfter));
        return true;
    }

    public int replaceAllMatches(String replacement) {
        if (!isEditable()) {
            return 0;
        }
        List<SearchHit> hits = searchSession.hits();
        if (hits.isEmpty()) {
            return 0;
        }

        int from = hits.get(0).start();
        int to = hits.get(hits.size() - 1).end();
        StringBuilder builder = new StringBuilder(to - from);
        int cursor = from;
        for (SearchHit hit : hits) {
            builder.append(document.getText(new TextRange(cursor, hit.start())));
            builder.append(expand(hit, replacement));
            cursor = hit.end();
        }

        int replaced = hits.size();
        document.replace(from, to, builder.toString(), caret.offset(), from + builder.length());
        caret.moveTo(document.clampOffset(from + builder.length()), false);
        finishEdit(null);
        invokeEvent(this, new SearchResultEvent(null, -1, searchSession.count()), true);
        return replaced;
    }

    protected String expand(SearchHit hit, String replacement) {
        ISearchStrategy strategy = searchSession.strategy();
        return strategy == null ? replacement : strategy.expand(hit, replacement);
    }

    protected Padding padding() {
        Padding value = padding.get();
        return value == null ? Padding.ZERO : value;
    }

    protected int gutterWidth() {
        if (!showLineNumbers.get() || !multiline.get()) {
            return 0;
        }
        int digits = Integer.toString(Math.max(document.lineCount(), 1)).length();
        return layoutEngine.measurer().font().width("0") * digits + componentRenderer.get().gutterPadding * 2;
    }

    protected int textLeft() {
        return padding().left() + gutterWidth();
    }

    protected int textTop() {
        if (multiline.get()) {
            return padding().top();
        }
        return Math.max(padding().top(), Math.round((height() - layoutEngine.heights().defaultHeight()) / 2.0F));
    }

    protected int innerWidth() {
        return Math.max(0, width() - textLeft() - padding().right());
    }

    protected int innerHeight() {
        return Math.max(0, height() - padding().top() - padding().bottom());
    }

    public boolean isVerticalScrollBarVisible() {
        return multiline.get() && showVerticalScrollBar.get()
                && (!autoHideScrollBars.get() || contentHeight() > innerHeight());
    }

    public boolean isHorizontalScrollBarVisible() {
        return multiline.get() && showHorizontalScrollBar.get() && !isWrapping()
                && (!autoHideScrollBars.get() || contentWidth() > innerWidth());
    }

    public boolean isWrapping() {
        return multiline.get() && lineWrap.get();
    }

    protected int scrollBarSize() {
        return componentRenderer.get().scrollBarSize;
    }

    protected int textWidth() {
        return Math.max(0, innerWidth() - (isVerticalScrollBarVisible() ? scrollBarSize() : 0));
    }

    protected int textHeight() {
        return Math.max(0, innerHeight() - (isHorizontalScrollBarVisible() ? scrollBarSize() : 0));
    }

    protected void refreshLayout() {
        layoutEngine.setWrapWidth(isWrapping() ? textWidth() : LayoutEngine.NO_WRAP);
        layoutEngine.setViewportWidth(textWidth());
        clampScroll();
        updateScrollBars();
    }

    public float contentHeight() {
        return layoutEngine.heights().totalHeight();
    }

    public float contentWidth() {
        return layoutEngine.widestLine();
    }

    public double scrollX() {
        return scrollX;
    }

    public double scrollY() {
        return scrollY;
    }

    public void setScrollY(double value) {
        this.scrollY = value;
        clampScroll();
    }

    public void setScrollX(double value) {
        this.scrollX = value;
        clampScroll();
    }

    public int maxScrollY() {
        if (!multiline.get()) {
            return 0;
        }
        return Math.max(0, Mth.ceil(contentHeight()) - textHeight());
    }

    public int maxScrollX() {
        if (isWrapping()) {
            return 0;
        }
        return Math.max(0, Mth.ceil(contentWidth()) - textWidth());
    }

    protected void clampScroll() {
        scrollY = MathUtils.clamp(scrollY, 0.0D, maxScrollY());
        scrollX = MathUtils.clamp(scrollX, 0.0D, maxScrollX());
    }

    protected void updateScrollBars() {
        if (updatingScrollBars) {
            return;
        }
        updatingScrollBars = true;
        try {
            int size = scrollBarSize();
            boolean vertical = isVerticalScrollBarVisible();
            boolean horizontal = isHorizontalScrollBarVisible();
            int inset = drawBackground.get() ? 1 : 0;

            verticalScrollBar.visible.set(vertical);
            if (vertical) {
                int screen = Math.max(1, textHeight());
                place(verticalScrollBar, width() - inset - size, inset,
                        size, Math.max(1, height() - inset * 2 - (horizontal ? size : 0)));
                apply(verticalScrollBar.screenSize, screen);
                apply(verticalScrollBar.max, screen + maxScrollY());
                apply(verticalScrollBar.scrollSteps, Math.max(1, Math.round(layoutEngine.heights().defaultHeight()
                        * scrollRowsPerNotch.get())));
                apply(verticalScrollBar.value, scrollY);
            }

            horizontalScrollBar.visible.set(horizontal);
            if (horizontal) {
                int screen = Math.max(1, textWidth());
                place(horizontalScrollBar, inset, height() - inset - size,
                        Math.max(1, width() - inset * 2 - (vertical ? size : 0)), size);
                apply(horizontalScrollBar.screenSize, screen);
                apply(horizontalScrollBar.max, screen + maxScrollX());
                apply(horizontalScrollBar.scrollSteps, Math.max(1, layoutEngine.measurer().font().width("0")
                        * horizontalScrollCharacters.get()));
                apply(horizontalScrollBar.value, scrollX);
            }
        } finally {
            updatingScrollBars = false;
        }
    }

    private static void place(DLScrollBar bar, int x, int y, int width, int height) {
        if (bar.x() != x || bar.y() != y) {
            bar.setPosition(x, y);
        }
        if (bar.width() != width || bar.height() != height) {
            bar.setSize(width, height);
        }
    }

    private static void apply(NumberProperty<Integer> property, int value) {
        if (property.get() != value) {
            property.set(value);
        }
    }

    private static void apply(NumberProperty<Double> property, double value) {
        if (Math.abs(property.get() - value) > SCROLL_EPSILON) {
            property.set(value);
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateScrollBars();
        if (dragging && !isDragged()) {
            dragging = false;
        }
        if (dragging) {
            autoScroll();
        }
    }

    protected void autoScroll() {
        double scale = Math.max(getGlobalScale(), MIN_SCALE);
        double localX = getLocalMouseX() / scale;
        double localY = getLocalMouseY() / scale;
        double overflowX = overflowOf(localX, textLeft(), textLeft() + textWidth());
        double overflowY = overflowOf(localY, textTop(), textTop() + textHeight());
        if (overflowX == 0.0D && overflowY == 0.0D) {
            return;
        }

        scrollX += autoScrollStep(overflowX);
        scrollY += autoScrollStep(overflowY);
        clampScroll();
        updateScrollBars();

        double caretX = MathUtils.clamp(localX, textLeft(), textLeft() + Math.max(0, textWidth() - 1));
        double caretY = MathUtils.clamp(localY, textTop(), textTop() + Math.max(0, textHeight() - 1));
        moveCaretTo(offsetAt(caretX, caretY), true);
    }

    protected double autoScrollStep(double overflow) {
        if (overflow == 0.0D) {
            return 0.0D;
        }
        double step = Math.min(Math.abs(overflow) * autoScrollSpeed.get(), autoScrollMaxStep.get());
        return Math.signum(overflow) * Math.max(1.0D, step);
    }

    protected static double overflowOf(double value, double min, double max) {
        if (value < min) {
            return value - min;
        }
        return value > max ? value - max : 0.0D;
    }

    public void addContextMenuContributor(IContextMenuContributor<DLTextBox> contributor) {
        contextMenuContributors.add(contributor);
    }

    public boolean removeContextMenuContributor(IContextMenuContributor<DLTextBox> contributor) {
        return contextMenuContributors.remove(contributor);
    }

    public List<IContextMenuContributor<DLTextBox>> contextMenuContributors() {
        return contextMenuContributors;
    }

    public TextContext contextAt(double localX, double localY) {
        int line = layoutEngine.heights().lineAtOffset((float) (localY - textTop() + scrollY));
        return new TextContext(localX, localY, line, document.lineStart(line), document.getLine(line),
                renderer.spanAt(createRenderContext(), localX, localY), layoutEngine.layoutOf(line).parsed());
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
        contextMenu.open(getWindowManager(), (int) getWindowManager().mouseXOnScreen(),
                (int) getWindowManager().mouseYOnScreen());
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
        for (IContextMenuContributor<DLTextBox> contributor : contextMenuContributors) {
            contributor.contribute(this, menuContext, entries);
        }
        return entries;
    }

    protected void addDefaultContextMenuEntries(TextContext context, List<DLContextMenu.ItemEntry> entries) {
        boolean editable = isEditable();
        boolean selection = caret.hasSelection();
        boolean readable = selection && !isMasked();

        List<TextAction> contextActions = parser().contextActions(context);
        if (!contextActions.isEmpty()) {
            entries.addAll(textActionEntries(contextActions));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
        }

        entries.add(menuEntry("undo", editable && document.undo().canUndo(), this::undo));
        entries.add(menuEntry("redo", editable && document.undo().canRedo(), this::redo));
        entries.add(DLContextMenu.ItemEntry.SEPARATOR);
        entries.add(menuEntry("cut", editable && readable, this::cutSelection));
        entries.add(menuEntry("copy", readable, this::copySelection));
        entries.add(menuEntry("copy_plain", readable, this::copySelectionAsPlainText));
        entries.add(menuEntry("paste", editable, this::pasteFromClipboard));
        entries.add(menuEntry("paste_plain", editable, this::pastePlainFromClipboard));
        entries.add(DLContextMenu.ItemEntry.SEPARATOR);
        entries.add(menuEntry("select_all", !document.isEmpty(), this::selectAll));

        List<TextAction> actions = parser().formatActions();
        if (editable && !actions.isEmpty()) {
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate(MENU_TRANSLATION_PREFIX + "format"), DLSprite.empty(), true, () -> {}, (menuX, menuY) -> textActionEntries(actions)));
        }
    }

    protected List<DLContextMenu.ItemEntry> textActionEntries(List<TextAction> actions) {
        List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
        for (TextAction action : actions) {
            if (action.isSeparator()) {
                entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            } else if (action.hasChildren()) {
                entries.add(new DLContextMenu.ItemEntry(action.label(), DLSprite.empty(), true, () -> {},
                        (menuX, menuY) -> textActionEntries(action.children())));
            } else {
                entries.add(new DLContextMenu.ItemEntry(action.label(), DLSprite.empty(), true,
                        () -> action.action().accept(this), null));
            }
        }
        return entries;
    }

    protected DLContextMenu.ItemEntry menuEntry(String key, boolean enabled, Runnable action) {
        return new DLContextMenu.ItemEntry(TextUtils.translate(MENU_TRANSLATION_PREFIX + key),
                DLSprite.empty(), enabled, action, null);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        TextBoxStyle boxStyle = componentRenderer.get();
        if (drawBackground.get()) {
            boxStyle.renderSprite(graphics, 0, 0, width(), height(), this, currentState());
        }

        if (document.isEmpty() && !isFocused() && !placeholderText.get().getString().isEmpty()) {
            graphics.graphics().drawString(layoutEngine.measurer().font(), placeholderText.get(),
                    textLeft(), textTop(), boxStyle.placeholder, false);
            return;
        }

        Rectangle outer = clampToScreen(renderBounds);
        Rectangle inner = Rectangle.intersection(outer, contentClipOnScreen());
        if (inner.width() <= 0 || inner.height() <= 0) {
            return;
        }

        GuiUtils.enableScissor(graphics, inner);
        renderer.render(graphics, createRenderContext(inner, Rectangle.intersection(inner, textAreaOnScreen())));
        GuiUtils.enableScissor(graphics, outer);
    }

    public TextBoxState currentState() {
        if (!enabled.get()) {
            return TextBoxState.DISABLED;
        }
        if (isFocused()) {
            return TextBoxState.FOCUSED;
        }
        return isSelected() ? TextBoxState.SELECTED : TextBoxState.NORMAL;
    }

    protected Rectangle textAreaOnScreen() {
        double scale = getGlobalScale();
        double inset = drawBackground.get() ? componentRenderer.get().borderWidth * scale : 0.0D;
        return Rectangle.withSize(
                getXOnScreen() + textLeft() * scale,
                getYOnScreen() + inset,
                Math.max(0, textWidth() * scale),
                Math.max(0, height() * scale - inset * 2)
        );
    }

    protected Rectangle contentClipOnScreen() {
        double scale = getGlobalScale();
        double inset = drawBackground.get() ? componentRenderer.get().borderWidth * scale : 0.0D;
        return Rectangle.withSize(
                getXOnScreen() + inset,
                getYOnScreen() + inset,
                Math.max(0, width() * scale - inset * 2),
                Math.max(0, height() * scale - inset * 2)
        );
    }

    protected static Rectangle clampToScreen(Rectangle bounds) {
        return TextBoxClip.clampToScreen(bounds);
    }

    protected TextBoxRenderContext createRenderContext() {
        return createRenderContext(null, null);
    }

    protected TextBoxRenderContext createRenderContext(Rectangle clip, Rectangle textClip) {
        return new TextBoxRenderContext(
                textLeft(), textTop(), textWidth(), textHeight(),
                scrollX, scrollY,
                caret.offset(), isFocused() || !hideSelection.get() ? caret.selection() : TextRange.EMPTY,
                isFocused(), enabled.get() && editable.get(),
                isCaretBlinkVisible(), multiline.get() && highlightCurrentLine.get(),
                hoveredLink, multiline.get() ? lineHighlight : null, gutterArea(),
                highlightSearchResults.get() && searchSession.isActive() ? searchSession : null,
                clip, textClip
        );
    }

    protected Rectangle gutterArea() {
        int gutter = gutterWidth();
        if (gutter <= 0) {
            return null;
        }
        int inset = drawBackground.get() ? 1 : 0;
        return Rectangle.withSize(inset, inset, textLeft() - inset, Math.max(0, height() - inset * 2));
    }

    protected boolean isCaretBlinkVisible() {
        int interval = Math.max(1, componentRenderer.get().caretBlinkIntervalMs);
        return (System.currentTimeMillis() - lastCaretResetMs) % interval < interval / 2;
    }

    public void moveCaretTo(int offset, boolean extend) {
        int clamped = document.startOfCodePoint(offset);
        boolean hadSelection = caret.hasSelection();
        caret.moveTo(clamped, extend);
        onCaretChanged(hadSelection);
    }

    private void onCaretChanged(boolean hadSelection) {
        lastCaretResetMs = System.currentTimeMillis();
        document.undo().breakCoalescing();
        scrollCaretIntoView();

        int line = document.lineOfOffset(caret.offset());
        invokeEvent(this, new CaretMovedEvent(caret.offset(), line, caret.offset() - document.lineStart(line)), true);
        if (hadSelection || caret.hasSelection()) {
            invokeEvent(this, new SelectionChangedEvent(caret.selection()), true);
        }
    }

    protected void moveCaretVertically(int direction, boolean extend) {
        int line = document.lineOfOffset(caret.offset());
        LineLayout lineLayout = layoutEngine.layoutOf(line);
        String lineText = layoutEngine.textOf(line);
        int column = caret.offset() - document.lineStart(line);
        int rowIndex = lineLayout.rowOfColumn(column);

        float goalX = caret.goalX();
        if (goalX < 0.0F) {
            goalX = TextGeometry.xOfColumn(layoutEngine.measurer(), lineLayout.row(rowIndex), lineText, column);
        }

        int targetRow = rowIndex + direction;
        int targetLine = line;
        if (targetRow < 0) {
            targetLine = line - 1;
            if (targetLine < 0) {
                caret.moveVertically(0, extend, goalX);
                onCaretChanged(true);
                return;
            }
            targetRow = layoutEngine.layoutOf(targetLine).rowCount() - 1;
        } else if (targetRow >= lineLayout.rowCount()) {
            targetLine = line + 1;
            if (targetLine >= document.lineCount()) {
                caret.moveVertically(document.length(), extend, goalX);
                onCaretChanged(true);
                return;
            }
            targetRow = 0;
        }

        LineLayout targetLayout = layoutEngine.layoutOf(targetLine);
        String targetText = layoutEngine.textOf(targetLine);
        VisualRow row = targetLayout.row(targetRow);
        int targetColumn = TextGeometry.columnAtX(layoutEngine.measurer(), row, targetText, goalX);

        caret.moveVertically(document.lineStart(targetLine) + targetColumn, extend, goalX);
        lastCaretResetMs = System.currentTimeMillis();
        scrollCaretIntoView();
        invokeEvent(this, new CaretMovedEvent(caret.offset(), targetLine, targetColumn), true);
    }

    protected boolean isCaretLineFullyVisible(int line) {
        if (textHeight() <= 0) {
            return true;
        }
        HeightIndex heights = layoutEngine.heights();
        int first = heights.lineAtOffset((float) scrollY);
        int last = heights.lineAtOffset((float) (scrollY + textHeight() - 1));
        return line > first && line < last;
    }

    public void scrollCaretIntoView() {
        int line = document.lineOfOffset(caret.offset());
        LineLayout lineLayout = layoutEngine.layoutOf(line);
        String lineText = layoutEngine.textOf(line);
        int column = caret.offset() - document.lineStart(line);
        int rowIndex = lineLayout.rowOfColumn(column);
        VisualRow row = lineLayout.row(rowIndex);

        if (!isCaretLineFullyVisible(line)) {
            float top = layoutEngine.heights().offsetOf(line) + lineLayout.rowTop(rowIndex);
            float bottom = top + row.height();
            boolean lastRow = line >= document.lineCount() - 1 && rowIndex >= lineLayout.rowCount() - 1;

            if (top < scrollY) {
                scrollY = lastRow ? Math.min(top, maxScrollY()) : top;
            } else if (bottom > scrollY + textHeight()) {
                scrollY = lastRow ? maxScrollY() : bottom - textHeight();
            }
        }

        if (!isWrapping()) {
            int margin = componentRenderer.get().caretMargin;
            float x = TextGeometry.xOfColumn(layoutEngine.measurer(), row, lineText, column);
            if (x < scrollX) {
                scrollX = x;
            } else if (x > scrollX + textWidth() - margin) {
                scrollX = x - textWidth() + margin;
            }
        }
        clampScroll();
        updateScrollBars();
    }

    public void insertText(String text) {
        if (!isEditable()) {
            return;
        }
        TextRange selection = document.snapToCodePoints(caret.selection());
        String sanitized = sanitizeInput(text);

        int resultingLength = document.length() - selection.length() + sanitized.length();
        if (resultingLength > maxLength.get()) {
            int allowed = maxLength.get() - (document.length() - selection.length());
            if (allowed <= 0) {
                return;
            }
            int cut = Math.min(allowed, sanitized.length());
            if (cut > 0 && Character.isHighSurrogate(sanitized.charAt(cut - 1))) {
                cut--;
            }
            if (cut <= 0) {
                return;
            }
            sanitized = sanitized.substring(0, cut);
        }

        int caretAfter = selection.start() + sanitized.length();
        DocumentEdit edit = document.replace(selection.start(), selection.end(), sanitized,
                caret.offset(), caretAfter);
        caret.moveTo(caretAfter, false);
        if (edit != null) {
            finishEdit(edit);
        }
    }

    public void deleteRange(int from, int to) {
        if (!isEditable()) {
            return;
        }
        TextRange range = document.snapToCodePoints(TextRange.of(from, to));
        if (range.isEmpty()) {
            return;
        }
        DocumentEdit edit = document.replace(range.start(), range.end(), "", caret.offset(), range.start());
        caret.moveTo(range.start(), false);
        if (edit != null) {
            finishEdit(edit);
        }
    }

    protected boolean deleteSelection() {
        if (!caret.hasSelection()) {
            return false;
        }
        TextRange selection = caret.selection();
        deleteRange(selection.start(), selection.end());
        return true;
    }

    private void finishEdit(DocumentEdit edit) {
        finishEdit(edit, true);
    }

    private void finishEdit(DocumentEdit edit, boolean revealCaret) {
        caret.clamp(document.length());
        lastCaretResetMs = System.currentTimeMillis();
        clampScroll();
        if (revealCaret) {
            scrollCaretIntoView();
        } else {
            updateScrollBars();
        }
        invokeEvent(this, new TextChangedEvent(document, edit), true);
    }

    public boolean isEditable() {
        return enabled.get() && editable.get() && !document.isReadOnly();
    }

    public void selectAll() {
        caret.select(new TextRange(0, document.length()));
        invokeEvent(this, new SelectionChangedEvent(caret.selection()), true);
    }

    public void undo() {
        int offset = document.undoLastEdit();
        if (offset >= 0) {
            caret.moveTo(document.clampOffset(offset), false);
            scrollCaretIntoView();
            invokeEvent(this, new TextChangedEvent(document, null), true);
        }
    }

    public void redo() {
        int offset = document.redoLastEdit();
        if (offset >= 0) {
            caret.moveTo(document.clampOffset(offset), false);
            scrollCaretIntoView();
            invokeEvent(this, new TextChangedEvent(document, null), true);
        }
    }

    public TextStyle styleAt(int offset) {
        return layoutEngine.styleAt(offset);
    }

    public TextStyle styleAtCaret() {
        return layoutEngine.styleAt(caret.offset());
    }

    public boolean isStyleActive(StyleFlag flag) {
        TextRange selection = document.snapToCodePoints(caret.selection());
        if (selection.isEmpty()) {
            return flag.isSet(styleAtCaret().flags());
        }

        boolean any = false;
        int firstLine = document.lineOfOffset(selection.start());
        int lastLine = document.lineOfOffset(selection.end());
        for (int line = firstLine; line <= lastLine; line++) {
            int lineStart = document.lineStart(line);
            for (StyledSpan span : layoutEngine.layoutOf(line).parsed().spans()) {
                if (span.isMarkup() || span.isEmpty()) {
                    continue;
                }
                int start = Math.max(lineStart + span.start(), selection.start());
                int end = Math.min(lineStart + span.end(), selection.end());
                if (start >= end) {
                    continue;
                }
                any = true;
                if (!flag.isSet(layoutEngine.baseStyle().merge(span.style()).flags())) {
                    return false;
                }
            }
        }
        return any;
    }

    public boolean toggleInlineStyle(StyleFlag flag) {
        MarkupSnippet snippet = parser().inlineMarkup(flag, "");
        if (snippet == null || !isEditable()) {
            return false;
        }
        toggleInlineMarkup(snippet.text().substring(0, snippet.contentStart()),
                snippet.text().substring(snippet.contentStart()));
        return true;
    }

    public void toggleInlineMarkup(String delimiter) {
        toggleInlineMarkup(delimiter, delimiter);
    }

    public void toggleInlineMarkup(String prefix, String suffix) {
        if (!isEditable()) {
            return;
        }
        TextRange selection = document.snapToCodePoints(caret.selection());
        String selected = document.getText(selection);

        boolean alreadyWrapped = selected.length() >= prefix.length() + suffix.length()
                && selected.startsWith(prefix) && selected.endsWith(suffix);
        if (alreadyWrapped) {
            String stripped = selected.substring(prefix.length(), selected.length() - suffix.length());
            document.replace(selection.start(), selection.end(), stripped, caret.offset(), selection.start());
            caret.select(new TextRange(selection.start(), selection.start() + stripped.length()));
        } else {
            String wrapped = prefix + selected + suffix;
            document.replace(selection.start(), selection.end(), wrapped, caret.offset(), selection.start());
            caret.select(new TextRange(selection.start() + prefix.length(),
                    selection.start() + prefix.length() + selected.length()));
        }
        finishEdit(null);
    }

    public void toggleLinePrefix(String prefix) {
        if (!isEditable()) {
            return;
        }
        TextRange selection = caret.selection();
        int firstLine = document.lineOfOffset(selection.start());
        int lastLine = document.lineOfOffset(selection.end());

        StringBuilder builder = new StringBuilder();
        boolean removing = true;
        for (int line = firstLine; line <= lastLine; line++) {
            if (!document.getLine(line).startsWith(prefix)) {
                removing = false;
                break;
            }
        }

        for (int line = firstLine; line <= lastLine; line++) {
            String text = document.getLine(line);
            builder.append(removing ? text.substring(prefix.length()) : prefix + text);
            if (line < lastLine) {
                builder.append('\n');
            }
        }

        int from = document.lineStart(firstLine);
        int to = document.lineEnd(lastLine);
        document.replace(from, to, builder.toString(), caret.offset(), from + builder.length());
        caret.select(new TextRange(from, from + builder.length()));
        finishEdit(null);
    }

    public void applyStyleBlock(String directives) {
        if (!isEditable()) {
            return;
        }
        TextRange selection = caret.selection();
        String selected = document.getText(selection);
        MarkupSnippet snippet = parser().styleBlock(directives, selected);
        if (snippet == null) {
            return;
        }

        document.replace(selection.start(), selection.end(), snippet.text(), caret.offset(), selection.start());
        int contentStart = selection.start() + snippet.contentStart();
        caret.select(new TextRange(contentStart, contentStart + snippet.contentLength()));
        finishEdit(null);
    }

    public void applyColor(int argb) {
        applyStyleBlock(String.format("#%08X", argb));
    }

    public void applyScale(float scale) {
        applyStyleBlock(scale + "x");
    }

    protected boolean onKeyPressed(int keyCode, int scanCode) {
        if (!isFocused()) {
            return false;
        }
        KeyEvent event = new KeyEvent(keyCode, scanCode,
                Screen.hasControlDown(), Screen.hasShiftDown(), Screen.hasAltDown());
        return keyMap.handle(this, event) || runShortcut(parser().formatActions(), event);
    }

    protected boolean runShortcut(List<TextAction> actions, KeyEvent event) {
        if (!isEditable()) {
            return false;
        }
        for (TextAction action : actions) {
            if (action.hasChildren() && runShortcut(action.children(), event)) {
                return true;
            }
            if (action.hasShortcut() && action.shortcut().matches(event)) {
                action.action().accept(this);
                return true;
            }
        }
        return false;
    }

    private int collapseOrMove(int target, boolean shift, boolean toLeft) {
        if (shift || !caret.hasSelection()) {
            return target;
        }
        TextRange selection = caret.selection();
        return toLeft ? selection.start() : selection.end();
    }

    protected int lineStartOfCaret() {
        return document.lineStart(document.lineOfOffset(caret.offset()));
    }

    protected int lineEndOfCaret() {
        return document.lineEnd(document.lineOfOffset(caret.offset()));
    }

    protected void handleTab(boolean outdent) {
        if (!isEditable()) {
            return;
        }
        TextRange selection = caret.selection();
        int firstLine = document.lineOfOffset(selection.start());
        int lastLine = document.lineOfOffset(selection.end());

        if (outdent || firstLine != lastLine || isBlockIndentable(firstLine)) {
            indentLines(firstLine, lastLine, outdent);
        } else {
            insertText(parser().indentUnit());
        }
    }

    protected boolean isBlockIndentable(int line) {
        return parser().isMarkup() && layoutEngine.layoutOf(line).parsed().kind().isIndentable();
    }

    protected void indentLines(int firstLine, int lastLine, boolean outdent) {
        String unit = parser().indentUnit();
        StringBuilder builder = new StringBuilder();
        int caretLine = document.lineOfOffset(caret.offset());
        int caretDelta = 0;
        boolean changed = false;

        for (int line = firstLine; line <= lastLine; line++) {
            String text = document.getLine(line);
            int delta;
            if (outdent) {
                int removable = 0;
                while (removable < unit.length() && removable < text.length()
                        && text.charAt(removable) == unit.charAt(removable)) {
                    removable++;
                }
                builder.append(text, removable, text.length());
                delta = -removable;
            } else {
                builder.append(unit).append(text);
                delta = unit.length();
            }
            changed |= delta != 0;
            if (line == caretLine) {
                caretDelta = delta;
            }
            if (line < lastLine) {
                builder.append('\n');
            }
        }

        if (!changed) {
            return;
        }

        boolean hadSelection = caret.hasSelection();
        int caretBefore = caret.offset();
        int from = document.lineStart(firstLine);
        int to = document.lineEnd(lastLine);
        int caretAfter = Math.max(from, caretBefore + caretDelta);

        document.replace(from, to, builder.toString(), caretBefore, caretAfter);
        if (hadSelection) {
            caret.select(new TextRange(from, from + builder.length()));
        } else {
            caret.moveTo(document.clampOffset(caretAfter), false);
        }
        finishEdit(null);
    }

    protected boolean clearEmptyBlock() {
        if (!parser().isMarkup() || !isEditable() || caret.hasSelection()) {
            return false;
        }
        int line = document.lineOfOffset(caret.offset());
        ParsedLine parsed = layoutEngine.layoutOf(line).parsed();
        if (parser().continuationPrefix(parsed).isEmpty() || !parsed.isMarkupOnly()) {
            return false;
        }

        int from = document.lineStart(line);
        int to = document.lineEnd(line);
        document.replace(from, to, "", caret.offset(), from);
        caret.moveTo(from, false);
        finishEdit(null);
        return true;
    }

    protected String continuationPrefix() {
        if (!parser().isMarkup()) {
            return "";
        }
        int line = document.lineOfOffset(caret.offset());
        return parser().continuationPrefix(layoutEngine.layoutOf(line).parsed());
    }

    protected void pageScroll(int direction, boolean extend) {
        int rows = Math.max(1, (int) (textHeight() / layoutEngine.heights().defaultHeight()));
        for (int i = 0; i < rows; i++) {
            moveCaretVertically(direction, extend);
        }
    }

    protected boolean onCharTyped(char codePoint) {
        if (!isFocused() || !isEditable() || !isAcceptableCharacter(codePoint)) {
            return false;
        }
        insertText(String.valueOf(codePoint));
        return true;
    }

    protected boolean isAcceptableCharacter(char codePoint) {
        return codePoint >= ' ' && codePoint != DELETE_CHARACTER || Character.isLetterOrDigit(codePoint);
    }

    public int offsetAt(double localX, double localY) {
        float documentY = (float) (localY - textTop() + scrollY);
        int line = layoutEngine.heights().lineAtOffset(documentY);
        LineLayout lineLayout = layoutEngine.layoutOf(line);
        String lineText = layoutEngine.textOf(line);

        float rowTop = layoutEngine.heights().offsetOf(line);
        VisualRow target = lineLayout.row(lineLayout.rowCount() - 1);
        for (VisualRow row : lineLayout.rows()) {
            if (documentY < rowTop + row.height()) {
                target = row;
                break;
            }
            rowTop += row.height();
        }

        float x = (float) (localX - textLeft() + scrollX);
        int column = TextGeometry.columnAtX(layoutEngine.measurer(), target, lineText, x);
        return document.lineStart(line) + column;
    }

    protected boolean onMousePressed(double localX, double localY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        String link = hoveredLink;
        if (link != null && Screen.hasControlDown()) {
            invokeEvent(this, new LinkClickedEvent(link), true);
            return true;
        }
        moveCaretTo(offsetAt(localX, localY), Screen.hasShiftDown());
        this.previousClickX = lastClickX;
        this.previousClickY = lastClickY;
        this.lastClickX = localX;
        this.lastClickY = localY;
        return true;
    }

    protected boolean isWithinMultiClickDistance(double localX, double localY) {
        if (Double.isNaN(previousClickX)) {
            return false;
        }
        double dx = localX - previousClickX;
        double dy = localY - previousClickY;
        return dx * dx + dy * dy <= MULTI_CLICK_DISTANCE * MULTI_CLICK_DISTANCE;
    }

    protected boolean onMultiClick(double localX, double localY, byte clickCount) {
        if (!isWithinMultiClickDistance(localX, localY)) {
            return false;
        }
        int offset = offsetAt(localX, localY);
        if (clickCount == 2) {
            caret.select(document.wordAt(offset));
        } else if (clickCount >= 3) {
            caret.select(document.lineRangeWithBreak(document.lineOfOffset(offset)));
        } else {
            return false;
        }
        invokeEvent(this, new SelectionChangedEvent(caret.selection()), true);
        return true;
    }

    protected boolean onDragged(double localX, double localY) {
        moveCaretTo(offsetAt(localX, localY), true);
        return true;
    }

    protected boolean onMouseMoved(double localX, double localY) {
        updateHover(localX, localY);
        return false;
    }

    protected void updateHover(double localX, double localY) {
        TextContext context = dragging ? TextContext.NONE : contextAt(localX, localY);
        StyledSpan span = context.span();
        if (span == null) {
            clearHover();
            return;
        }

        hoveredLink = span.style().hasLink() ? span.style().link() : null;
        ITextTooltipProvider provider = tooltipProvider.get();
        DLTooltip hint = provider == null
                ? DLTooltip.EMPTY
                : provider.tooltipFor(context, parser(), componentRenderer.get());
        tooltip.set(hint == null ? DLTooltip.EMPTY : hint);
        cursor.set(hoveredLink != null ? CursorType.HAND : CursorType.IBEAM);
    }

    public void clearHover() {
        hoveredLink = null;
        tooltip.set(DLTooltip.EMPTY);
        cursor.set(CursorType.IBEAM);
    }

    protected boolean onScrolled(double deltaY) {
        clearHover();
        if (!multiline.get() || contentHeight() <= textHeight()) {
            return false;
        }
        scrollY += deltaY * layoutEngine.heights().defaultHeight() * scrollRowsPerNotch.get();
        clampScroll();
        updateScrollBars();
        return true;
    }

    @Override
    public void updateScreenLayout() {
        layoutEngine.setMeasurer(TextMeasurer.ofDefaultFont());
        refreshLayout();
    }

    public int visibleRowCount() {
        return Mth.ceil(textHeight() / Math.max(1.0F, layoutEngine.heights().defaultHeight()));
    }
}
