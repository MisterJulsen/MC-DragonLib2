package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu.ItemEntry;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.EffectBatch;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.LineMarker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.RichTextComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.TextSegment;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.action.InteractiveElement;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.Align;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.TextCursorPosition;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.mixin.FontAccessor;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.Holder.MutableHolder;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.regex.Pattern;

@SupportsEvents({
    DLRichTextLabel.TextChangedEvent.class,
    DLRichTextLabel.TextLineSpacingChangedEvent.class,
    DLRichTextLabel.TextLineWrapChangedEvent.class,
    DLRichTextLabel.TextMaxCharactersChangedEvent.class,
    DLRichTextLabel.TextMultilinedChanged.class,
    DLRichTextLabel.TextFilterRegexChangedEvent.class,
    DLRichTextLabel.TextInteractiveElementClicked.class,
    DLRichTextLabel.TextTextValidationEvent.class
})
public class DLRichTextLabel extends DLGuiComponent implements DLContextMenu.MenuBuilder {
    
    public record TextChangedEvent(RichTextComponent text) implements IEvent {}
    public record TextMultilinedChanged(boolean multiline) implements IEvent {}
    public record TextLineWrapChangedEvent(boolean lineWrap) implements IEvent {}
    public record TextMaxCharactersChangedEvent(int maxCharacters) implements IEvent {}
    public record TextLineSpacingChangedEvent(int lineSpacing) implements IEvent {}
    public record TextFilterRegexChangedEvent(MutableHolder<String> regex) implements IEvent {}
    public record TextInteractiveElementClicked(InteractiveElement.ClickAction action) implements IEvent {}
    public record TextTextValidationEvent(String currentText, String futureText, MutableHolder<String> input) implements IEvent {}
    
    public final Property<RichTextComponent> text = new Property<RichTextComponent>(new RichTextComponent())
        .withAfterPropertyChangedCallback((o, x) -> {
            invokeEvent(this, new DLRichTextLabel.TextChangedEvent(x));
            setupRichTextComponent(x);
            refresh();
        });
    public final BooleanProperty lineWrap = new BooleanProperty(false)
        .withAfterPropertyChangedCallback((o, x) -> {
            invokeEvent(this, new DLRichTextLabel.TextLineWrapChangedEvent(x));
            refresh();
        });
    public final BooleanProperty multiline = new BooleanProperty(false)
        .withAfterPropertyChangedCallback((o, x) -> {
            if (!x) {
                text.get().replace("\n", "").replace("\r", "");
                lineWrap.set(false);
            }
            text.get().setMultiline(x);
            invokeEvent(this, new DLRichTextLabel.TextMultilinedChanged(x));
            refresh();
        });
    public final NumberProperty<Integer> maxCharacters = new NumberProperty<Integer>(1000000, () -> 0, () -> 2000000)
        .withAfterPropertyChangedCallback((o, x) -> {
            text.get().setMaxCharacters(x);
            invokeEvent(this, new DLRichTextLabel.TextMaxCharactersChangedEvent(x));
            refresh();
        });
    public final NumberProperty<Integer> lineSpacing = new NumberProperty<Integer>(2)
        .withAfterPropertyChangedCallback((o, x) -> {
            invokeEvent(this, new DLRichTextLabel.TextLineSpacingChangedEvent(x));
            refresh();
        });        
    public final Property<String> filterRegex = new Property<>("(?s).*")
        .withModificationCallback((o, n) -> {
            MutableHolder<String> regex = new MutableHolder<>(n);
            invokeEvent(this, new DLRichTextLabel.TextFilterRegexChangedEvent(regex));
            try {
                String rx = regex.get();
                Pattern.compile(rx);
                return rx;
            } catch (Exception e) {
                return o;
            }
        })
        .withAfterPropertyChangedCallback((o, x) -> {
            text.get().setFilterRegex(x);
            refresh();
        });

    public final ColorProperty clickAreaColor = new ColorProperty(DLColor.fromInt(0x403399FF), DLColor.TRANSPARENT);
    public final Property<Padding> contentPadding = new Property<>(new Padding(0, 3, 0, 3));

    private final List<EffectBatch> effects = Lists.newLinkedList();
    protected InteractiveElement hoveredElement = null;
    private int renderCharacterIndex;
    private float renderOffsetX;
    private float renderOffsetY;
    private LineMarker currentMarker;
    private TreeMap<Integer, LineMarker> lineMarkers;
    private TreeMap<Integer, LineMarker> lineMarkersByY;
    private List<LineMarker> linesByIndex;
    private double lastMouseX = -1, lastMouseY = -1;

    private float currentLayoutContentX;
    private float currentLayoutContentY;
    private float currentLayoutContentWidth;
    private float currentLayoutContentHeight;

    public final EventListenerId textElementClickEventId;
    public final EventListenerId interactiveTextElementClickEventId;
    public final EventListenerId hoverInteractableElementsEventId;
    public final EventListenerId unhoverInteractableElementsEventId;

    public DLRichTextLabel(int x, int y, int w, int h) {
        super(x, y, w, h);
        text.set(new RichTextComponent());

        DLContextMenu contextMenu = new DLContextMenu(this::buildContextMenuContents);

        textElementClickEventId = addEventListener(DLGuiStandardEvents.MouseDownEvent.class, this::onTextElementClicked);
        interactiveTextElementClickEventId = addEventListener(DLRichTextLabel.TextInteractiveElementClicked.class, this::onInteractiveElementClicked);
        hoverInteractableElementsEventId = addEventListener(DLGuiStandardEvents.MouseMoveEvent.class, (src, e) -> onHoverInteractiveElements(e.mouseX(), e.mouseY()));
        unhoverInteractableElementsEventId = addEventListener(DLGuiStandardEvents.MouseLeaveEvent.class, (src, e) -> onUnhover());

        addEventListener(DLGuiStandardEvents.RightClickEvent.class, (src, event) -> {
            contextMenu.open(getWindowManager(), (int)getWindowManager().mouseXOnScreen(), (int)getWindowManager().mouseYOnScreen());
            return false;
        });

        /*
        text.get().append("Dies", new TextStyle.Builder().bold(true).italic(true).color(0xFFFF0000).underlined(true).shadow(true).build());
        text.get().append(" ist", new TextStyle.Builder().color(0xFFFF0000).size(20).underlined(true).shadow(false).highlightColor(0xFFFFFF00).build());
        text.get().append(" ein", new TextStyle.Builder().italic(true).underlined(true).color(0xFF00FF00).build());
        text.get().append(" Test mit Linksbündigkeit.\n", new TextStyle.Builder().shadow(true).underlined(true).build());
        int indexOfTest = text.get().getPlainText().indexOf("Test mit Linksbündigkeit");
        if (indexOfTest != -1) text.get().setParagraphAlignment(text.get().getPlainText().codePointCount(0, indexOfTest), EAlignment.LEFT);
        text.get().append("Klicke ", null);
        int linkStart = text.get().length();
        text.get().append("hier für Google!", null);
        int linkEnd = text.get().length();
        text.get().append("\nUnd ", null);

        int consoleLinkStart = text.get().length();
        text.get().append("Hello World", new TextStyle.Builder().color(0xFF00AAAA).underlined(true).build());
        int consoleLinkEnd = text.get().length();
        text.get().append("Dieser Text ist zentriert.", new TextStyle.Builder().color(0xFF00FFFF).build());
        text.get().append("\nNoch eine zentrierte Zeile.\n", new TextStyle.Builder().color(0xFF00AAFF).build());
        int indexOfZentriert = text.get().getPlainText().indexOf("Dieser Text ist zentriert");
        if (indexOfZentriert != -1) text.get().setParagraphAlignment(text.get().getPlainText().codePointCount(0, indexOfZentriert), EAlignment.CENTER);

        text.get().append("Und dieser Text hier ist rechtsbündig ausgerichtet.", new TextStyle.Builder().color(0xFFFFAA00).build());
        text.get().append("\nEine weitere rechtsbündige Zeile.", new TextStyle.Builder().color(0xFFFF7700).build());
        int indexOfRechts = text.get().getPlainText().indexOf("Und dieser Text hier ist rechtsbündig");
        if (indexOfRechts != -1) text.get().setParagraphAlignment(text.get().getPlainText().codePointCount(0, indexOfRechts), EAlignment.RIGHT);
        int indexOfWeitereRechts = text.get().getPlainText().indexOf("weitere rechtsbündige");
        if (indexOfWeitereRechts != -1) text.get().setParagraphAlignment(text.get().getPlainText().codePointCount(0, indexOfWeitereRechts), EAlignment.RIGHT);

        text.get().append("\nStandardausrichtung (links) für diesen Text.", null);
        text.get().append("\nTest Test Test", null);


        text.get().createInteractiveElement(linkStart, linkEnd).ifPresent(a -> {
            a.withAction(new InteractiveElement.ClickAction(InteractiveElement.ClickAction.OPEN_URL, "https://www.google.com"))
             .withAction(new InteractiveElement.HoverAction(TextUtils.text("HELLO WORLD!")));
        });

        text.get().createInteractiveElement(linkStart - 20, linkEnd - 20).ifPresent(a -> {
            a.withAction(new InteractiveElement.ClickAction(InteractiveElement.ClickAction.COPY_TO_CLIPBAORD, "Here be Dragons!"));
        });
        */
    }

    @Override
    public List<ItemEntry> buildContextMenuContents(int x, int y) {
        List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
        entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".menu.copy"), DLSprite.empty(), true, () -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(text.get().getPlainText());
        }, null));
        return entries;
    }
    
    protected boolean onTextElementClicked(DLGuiComponent src, DLGuiStandardEvents.MouseDownEvent event) {
        if (text.get() != null && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (hoveredElement != null && hoveredElement.hasActionFromType(InteractiveElement.ClickAction.class)) {
                invokeEvent(this, new DLRichTextLabel.TextInteractiveElementClicked(hoveredElement.getActionFromType(InteractiveElement.ClickAction.class)));
            }
        }
        return false;
    }

    protected boolean onInteractiveElementClicked(DLGuiComponent src, DLRichTextLabel.TextInteractiveElementClicked event) {
        switch (event.action().actionName()) {
            case InteractiveElement.ClickAction.OPEN_URL: 
                Util.getPlatform().openUri(event.action().value());
                return false;
            case InteractiveElement.ClickAction.COPY_TO_CLIPBAORD: 
                Minecraft.getInstance().keyboardHandler.setClipboard(event.action().value());
                return false;
            default:
                return false;
        }
    }


    protected void setupRichTextComponent(RichTextComponent rtc) {
        if (rtc != null) {
            rtc.setTextChangedCallback(() -> {
                refresh();
                invokeEvent(this, new DLRichTextLabel.TextChangedEvent(rtc));
            });
            rtc.setTextValidator((current, future, input) -> {
                MutableHolder<String> ipt = new MutableHolder<>(input);
                invokeEvent(this, new DLRichTextLabel.TextTextValidationEvent(current, future, ipt));
                return ipt.get();
            });
        }
    }

    protected float getLayoutContentX() {
        return contentPadding.get().left();
    }

    protected float getLayoutContentY() {
        return contentPadding.get().top();
    }

    protected float getLayoutContentWidth() {
        return width() - contentPadding.get().left() -  contentPadding.get().right();
    }

    protected float getLayoutContentHeight() {
        return height() - contentPadding.get().top() -  contentPadding.get().bottom();
    }


    protected TreeMap<Integer, LineMarker> getLineMarkers() {
        if (lineMarkers == null) {
            recalcLines();
        }
        return lineMarkers;
    }

    protected TreeMap<Integer, LineMarker> getLineMarkersByY() {
        if (lineMarkersByY == null) {
            recalcLines();
        }
        return lineMarkersByY;
    }

    public List<LineMarker> getLinesOrdered() {
        if (linesByIndex == null) {
            recalcLines();
        }
        return linesByIndex;
    }

    public LineMarker getLineAt(int textIndex) throws IndexOutOfBoundsException {
        Map.Entry<Integer, LineMarker> marker = getLineMarkers().floorEntry(textIndex);
        if (marker == null) {
            throw new IllegalArgumentException(String.format("There is no line at index %d.", textIndex));
        }
        return marker.getValue();
    }


    public LineMarker getLineByLineIndex(int lineIndex) throws IndexOutOfBoundsException {
        if (lineIndex < 0 || lineIndex >= getLinesOrdered().size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds [0, %d]", lineIndex, getLinesOrdered().size() - 1));
        }
        if (getLinesOrdered().isEmpty()) {
            return new LineMarker(0, 0, 0, 1f, (float) Minecraft.getInstance().font.lineHeight, 0, text.get() != null ? text.get().getParagraphAlignment(0) : ETextAlignment.LEFT);
        }
        return getLinesOrdered().get(lineIndex);
    }

    public LineMarker getLineByYCoord(float y) {
        Map.Entry<Integer, LineMarker> marker = getLineMarkersByY().floorEntry((int)(y - getLayoutContentX()));
        if (marker == null) {
            return getLinesOrdered().get(0);
        }
        return marker.getValue();
    }


    public int getIndexByPos(float x, float y, boolean pickClosest) {
        if (text.get() == null) return 0;

        float textBlockRelativeX = (float)(x - getLayoutContentX() + getScrollOffsetX());
        LineMarker targetLine = getLineByYCoord(y + (float)lineSpacing.get() / 2f);

        if (targetLine == null) {
            if (pickClosest && !getLinesOrdered().isEmpty()) {
                targetLine = getLinesOrdered().get(0);
            } else if (pickClosest) {
                return 0;
            } else {
                return -1;
            }
        }

        float lineVisualStartXInTextBlock = calculateAlignedX(targetLine, getLayoutContentWidth());

        if (textBlockRelativeX < lineVisualStartXInTextBlock) {
            return pickClosest ? targetLine.startIndex() : -1;
        }
        if (textBlockRelativeX > lineVisualStartXInTextBlock + targetLine.lineWidth() + 2.0f) {
            if (pickClosest) return targetLine.endIndex();
            return -1;
        }
        float adjustedXInLineSpace = textBlockRelativeX - lineVisualStartXInTextBlock;
        return text.get().indexByWidth(adjustedXInLineSpace, targetLine, pickClosest);
    }


    public TextCursorPosition getPosByIndex(int index) {
        if (text.get() == null) {
            return new TextCursorPosition(0, 0, 0, 0, Minecraft.getInstance().font.lineHeight, ETextAlignment.LEFT);
        }

        index = MathUtils.clamp(index, 0, text.get().length());
        LineMarker marker = getLineAt(index);
        float widthAtIdx = text.get().width(marker.startIndex(), index);
        float lineX = calculateAlignedX(marker, getLayoutContentWidth());
        float x = lineX + widthAtIdx;

        return new TextCursorPosition(index, index - marker.startIndex(), x, marker.y(), marker.lineHeight(), marker.alignment());
    }


    public void refresh() {
        this.currentLayoutContentX = getLayoutContentX();
        this.currentLayoutContentY = getLayoutContentY();
        this.currentLayoutContentWidth = getLayoutContentWidth();
        this.currentLayoutContentHeight = getLayoutContentHeight();

        this.linesByIndex = null;
        this.lineMarkers = null;
        this.lineMarkersByY = null;
        this.hoveredElement = null;
    }

    private void recalcLines() {
        TreeMap<Integer, LineMarker> calculatedMarkers;
        this.lineMarkers = Maps.newTreeMap();
        this.lineMarkersByY = Maps.newTreeMap();
        if (text.get() == null) {
            calculatedMarkers = Maps.newTreeMap();
            calculatedMarkers.put(0, new LineMarker(0, 0, 0, 1f, (float) Minecraft.getInstance().font.lineHeight, 0, ETextAlignment.LEFT));
            this.lineMarkers.putAll(calculatedMarkers);
            this.lineMarkersByY.putAll(calculatedMarkers);
            this.linesByIndex = new ArrayList<>(calculatedMarkers.values());
            return;
        }

        float widthForSplitting = lineWrap.get() ? getLayoutContentWidth() : Integer.MAX_VALUE;
        if (widthForSplitting <= 0 && lineWrap.get()) widthForSplitting = 1;

        if (text.get().isMultiline()) {
            calculatedMarkers = text.get().splitLines((int) widthForSplitting);
        } else {
            float textWidth = text.get().width();
            float maxScale = 1f;
            float maxLineHeight = Minecraft.getInstance().font.lineHeight;
            if (text.get().getSegmentsRaw() != null && !text.get().getSegmentsRaw().isEmpty()) {
                maxScale = 0;
                maxLineHeight = 0;
                for (TextSegment segment : text.get().getSegmentsRaw()) {
                    maxScale = Math.max(maxScale, segment.style().scale());
                    maxLineHeight = Math.max(maxLineHeight, segment.style().scale() * segment.style().font().lineHeight);
                }
                if (maxLineHeight == 0) maxLineHeight = Minecraft.getInstance().font.lineHeight;
                if (maxScale == 0) maxScale = 1f;
            }
            calculatedMarkers = Maps.newTreeMap();
            calculatedMarkers.put(0, new LineMarker(0, text.get().length(), textWidth, maxScale, maxLineHeight, 0, text.get().getParagraphAlignment(0)));
        }

        if (calculatedMarkers.isEmpty() && (text.get().length() == 0 || (text.get().getPlainText() != null && text.get().getPlainText().equals("\n")))) {
            calculatedMarkers.put(0, new LineMarker(0, text.get().length(), 0, 1f, (float) Minecraft.getInstance().font.lineHeight, 0, text.get().getParagraphAlignment(0)));
        }

        float currentY = lineSpacing.get() / 2f;
        List<LineMarker> tempList = new ArrayList<>();
        for (Map.Entry<Integer, LineMarker> entry : calculatedMarkers.entrySet()) {
            LineMarker oldMarker = entry.getValue();
            LineMarker newMarkerWithY = new LineMarker(oldMarker.startIndex(), oldMarker.endIndex(), oldMarker.lineWidth(), oldMarker.scale(), oldMarker.lineHeight(), currentY, oldMarker.alignment());
            tempList.add(newMarkerWithY);
            currentY += oldMarker.lineHeight() + lineSpacing.get();
        }

        for (LineMarker lm : tempList) {
            lineMarkers.put(lm.startIndex(), lm);
            lineMarkersByY.put((int) lm.y(), lm);
        }
        this.linesByIndex = tempList;
    }

    private void setPose(PoseStack stack, float x, float y, float scale) {
        stack.pushPose();
        stack.translate(x, y, 0);
        stack.pushPose();
        stack.scale(scale, scale, 1);
    }

    private void clearPose(PoseStack stack) {
        stack.popPose();
        stack.popPose();
    }

    protected float calculateAlignedX(LineMarker marker, float layoutWidth) {
        if (marker == null || layoutWidth <= 0) return 0;
        float lineWidth = marker.lineWidth();
        float lineX = switch (marker.alignment()) {
            case CENTER -> (layoutWidth - lineWidth) / 2.0f;
            case RIGHT -> layoutWidth - lineWidth - 1;
            default -> 0;
        };
        return Math.max(0, lineX);
    }

    private void initRenderer() {
        effects.clear();
        renderCharacterIndex = -1;
        renderOffsetY = lineSpacing.get() / 2f;

        List<LineMarker> orderedLines = getLinesOrdered();
        if (!orderedLines.isEmpty()) {
            currentMarker = orderedLines.get(0);
            renderOffsetX = calculateAlignedX(currentMarker, this.currentLayoutContentWidth);
        } else {
            currentMarker = new LineMarker(0, 0, 0, 1f, Minecraft.getInstance().font.lineHeight, 0, text.get() != null ? text.get().getParagraphAlignment(0) : ETextAlignment.LEFT);
            renderOffsetX = 0;
        }
    }

    private void addEffect(EffectBatch batch) {
        if (!batch.isEmpty()) {
            this.effects.add(batch);
        }
    }

    public boolean renderSegment(de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics graphics, TextSegment segment, int skip) {
        float segmentRenderStartXVirtual = renderOffsetX;
        float segmentRenderStartYVirtual = renderOffsetY;

        float screenPoseX = (float)(this.currentLayoutContentX + segmentRenderStartXVirtual - getScrollOffsetX());
        float screenPoseY = (float)(this.currentLayoutContentY + segmentRenderStartYVirtual - getScrollOffsetY());

        if (segment.style().dropShadow()) {
            setPose(graphics.poseStack(), screenPoseX, screenPoseY, segment.style().scale());
            renderSegmentInternal(graphics, segment, true, segmentRenderStartXVirtual, skip);
            clearPose(graphics.poseStack());
        }
        setPose(graphics.poseStack(), screenPoseX, screenPoseY, segment.style().scale());
        boolean b = renderSegmentInternal(graphics, segment, false, segmentRenderStartXVirtual, skip);
        clearPose(graphics.poseStack());
        return b;
    }

    private boolean renderSegmentInternal(de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics graphics, TextSegment segment, boolean renderShadow, float segmentStartXLineUnscaledVirtual, int skip) {
        float localX = segmentStartXLineUnscaledVirtual;
        float localY = this.renderOffsetY;

        int charIdxInRichText = this.renderCharacterIndex + skip;
        LineMarker currentLineMarker = this.currentMarker;

        float dX = 0;
        float dYInScaledSpace;

        Matrix4f pose = graphics.poseStack().last().pose();
        BufferSource bufferSource = graphics.graphics().bufferSource();
        Font.DisplayMode mode = Font.DisplayMode.NORMAL;
        int packedLightCoords = LightTexture.FULL_BRIGHT;
        float dimFactor = renderShadow ? 0.25F : 1.0F;
        Font font = segment.style().font();
        FontAccessor accessor = (FontAccessor) font;
        FontSet fontSet = accessor.dragonlib$invokeGetFontSet(Style.DEFAULT_FONT);

        boolean bold = segment.style().bold();
        int textColor = segment.style().color();
        float alpha = (float) (textColor >> 24 & 255) / 255.0F;
        float red = (float) (textColor >> 16 & 255) / 255.0F * dimFactor;
        float green = (float) (textColor >> 8 & 255) / 255.0F * dimFactor;
        float blue = (float) (textColor & 255) / 255.0F * dimFactor;

        EffectBatch effectHolder = new EffectBatch(localX, localY, segment.style().scale());

        float viewMinY_tb = (float)(getScrollOffsetY());
        float viewMaxY_tb = (float)(getScrollOffsetY() + this.currentLayoutContentHeight);
        float viewMinX_tb = (float)(getScrollOffsetX());
        float viewMaxX_tb = (float)(getScrollOffsetX() + this.currentLayoutContentWidth);

        for (int offset = skip, c; offset < segment.length(); offset++) {
            charIdxInRichText++;
            c = segment.stringBuilder().codePointAt(segment.stringBuilder().offsetByCodePoints(0, offset));

            LineMarker oldMarker = currentLineMarker;
            currentLineMarker = getLineMarkers().floorEntry(charIdxInRichText).getValue();
            localY = currentLineMarker.y();

            if (oldMarker != currentLineMarker) {
                addEffect(effectHolder);
                localX = calculateAlignedX(currentLineMarker, this.currentLayoutContentWidth);
                dX = 0;
                if (!renderShadow) {
                    this.renderOffsetX = localX;
                    this.renderOffsetY = localY;
                    this.currentMarker = currentLineMarker;
                }

                clearPose(graphics.poseStack());
                float newScreenPoseX = (float)(this.currentLayoutContentX + localX - getScrollOffsetX());
                float newScreenPoseY = (float)(this.currentLayoutContentY + localY - getScrollOffsetY());
                setPose(graphics.poseStack(), newScreenPoseX, newScreenPoseY, segment.style().scale());
                pose = graphics.poseStack().last().pose();
                effectHolder = new EffectBatch(localX, localY, segment.style().scale());
            }

            if (charIdxInRichText >= currentLineMarker.endIndex() || charIdxInRichText < currentLineMarker.startIndex()) {
                continue;
            }

            float lineTop_tb = localY;
            float lineBottom_tb = localY + currentLineMarker.lineHeight();

            if (multiline.get() && lineTop_tb > viewMaxY_tb) {
                if (!renderShadow) {
                    this.renderCharacterIndex = charIdxInRichText - 1;
                }
                addEffect(effectHolder);
                return false;
            }


            dYInScaledSpace = Math.max(0, (currentLineMarker.lineHeight() - segment.style().font().lineHeight * segment.style().scale()) / segment.style().scale() - segment.style().scale());
            if (currentLineMarker.lineHeight() == segment.style().font().lineHeight * segment.style().scale()) {
                dYInScaledSpace = 0;
            }


            GlyphInfo glyphInfo = fontSet.getGlyphInfo(c, accessor.dragonlib$filterFishyGlyphs());
            BakedGlyph bakedGlyph = segment.style().obfuscated() && c != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(c);
            float charWidthInScaledSpace = glyphInfo.getAdvance(bold);

            if (!(multiline.get() && lineWrap.get())) {
                float charLeft_tb = localX + (dX * segment.style().scale());
                float charRight_tb = charLeft_tb + (charWidthInScaledSpace * segment.style().scale());
                if (charRight_tb < viewMinX_tb) {
                    dX += charWidthInScaledSpace;
                    continue;
                }
                if (charLeft_tb > viewMaxX_tb) {
                    dX += charWidthInScaledSpace;
                    continue;
                }
            }

            InteractiveElement currentElementForChar = hoveredElement != null && hoveredElement.contains(charIdxInRichText) ? hoveredElement : null;
            if (!renderShadow && currentElementForChar != null && currentElementForChar.shouldHighlightOnHover()) {
                int hoverColor = clickAreaColor.get().getAsARGB();
                float hr = (float) (hoverColor >> 16 & 255) / 255.0F;
                float hg = (float) (hoverColor >> 8 & 255) / 255.0F;
                float hb = (float) (hoverColor & 255) / 255.0F;
                float ha = (float) (hoverColor >> 24 & 255) / 255.0F;

                float hX1 = dX;
                float hY1 = -(float)lineSpacing.get() / 2f;
                float hX2 = dX + charWidthInScaledSpace;
                float hY2 = currentLineMarker.lineHeight() / segment.style().scale() + (float)lineSpacing.get() / 2f;


                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tesselator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();

                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.vertex(pose, hX1, hY2, 0.0F).color(hr, hg, hb, ha).endVertex();
                bufferBuilder.vertex(pose, hX2, hY2, 0.0F).color(hr, hg, hb, ha).endVertex();
                bufferBuilder.vertex(pose, hX2, hY1, 0.0F).color(hr, hg, hb, ha).endVertex();
                bufferBuilder.vertex(pose, hX1, hY1, 0.0F).color(hr, hg, hb, ha).endVertex();
                tesselator.end();

                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
            }


            if (!renderShadow && segment.style().highlightColor() != 0) {
                int highlightColorInt = segment.style().highlightColor();
                float hr = (float) (highlightColorInt >> 16 & 255) / 255.0F;
                float hg = (float) (highlightColorInt >> 8 & 255) / 255.0F;
                float hb = (float) (highlightColorInt & 255) / 255.0F;
                float ha = (float) (highlightColorInt >> 24 & 255) / 255.0F;

                float hX1 = dX;
                float hY1 = 0;
                float hX2 = dX + charWidthInScaledSpace;
                float hY2 = currentLineMarker.lineHeight() / segment.style().scale();

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tesselator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();

                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.vertex(pose, hX1, hY2, 0.0F).color(hr, hg, hb, ha).endVertex();
                bufferBuilder.vertex(pose, hX2, hY2, 0.0F).color(hr, hg, hb, ha).endVertex();
                bufferBuilder.vertex(pose, hX2, hY1, 0.0F).color(hr, hg, hb, ha).endVertex();
                bufferBuilder.vertex(pose, hX1, hY1, 0.0F).color(hr, hg, hb, ha).endVertex();
                tesselator.end();

                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
            }

            if (!(bakedGlyph instanceof EmptyGlyph)) {
                float boldOffset = bold ? glyphInfo.getBoldOffset() : 0.0F;
                float shadowOffset = renderShadow ? glyphInfo.getShadowOffset() : 0.0F;
                VertexConsumer vertexConsumer = bufferSource.getBuffer(bakedGlyph.renderType(mode));
                accessor.dragonlib$renderChar(bakedGlyph, bold, segment.style().italic(), boldOffset, dX + shadowOffset, dYInScaledSpace + shadowOffset, pose, vertexConsumer, red, green, blue, alpha, packedLightCoords);
            }

            float glyphAdvanceRenderShadowOffset = renderShadow ? 1.0F : 0.0F;
            if (segment.style().strikethrough()) {
                effectHolder.addEffectToBatch(new BakedGlyph.Effect(dX + glyphAdvanceRenderShadowOffset - 1.0F, dYInScaledSpace + glyphAdvanceRenderShadowOffset + 4.5F, dX + glyphAdvanceRenderShadowOffset + charWidthInScaledSpace, dYInScaledSpace + glyphAdvanceRenderShadowOffset + 4.5F - 1.0F, 0.01F, red, green, blue, alpha));
            }

            if (segment.style().underlined()) {
                effectHolder.addEffectToBatch(new BakedGlyph.Effect(dX + glyphAdvanceRenderShadowOffset - 1.0F, dYInScaledSpace + glyphAdvanceRenderShadowOffset + 9.0F, dX + glyphAdvanceRenderShadowOffset + charWidthInScaledSpace, dYInScaledSpace + glyphAdvanceRenderShadowOffset + 9.0F - 1.0F, 0.01F, red, green, blue, alpha));
            }
            dX += charWidthInScaledSpace;
        }
        addEffect(effectHolder);

        if (!renderShadow) {
            this.renderOffsetX = localX + (dX * segment.style().scale());
            this.renderCharacterIndex += segment.length();
        }
        return true;
    }

    public void postRender(de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics graphics, EffectBatch effectsBatch) {
        if (effectsBatch.isEmpty()) {
            return;
        }

        float effectVirtualX_tb = effectsBatch.x();
        float effectVirtualY_tb = effectsBatch.y();
        float effectScale = effectsBatch.scale();
        float approxEffectHeight = 10 * effectScale;

        double viewMinY_tb = getScrollOffsetY();
        double viewMaxY_tb = getScrollOffsetY() + this.currentLayoutContentHeight;

        if (multiline.get() && effectVirtualY_tb + approxEffectHeight < viewMinY_tb) return;
        if (multiline.get() && effectVirtualY_tb > viewMaxY_tb) return;

        double screenPoseX = this.currentLayoutContentX + effectVirtualX_tb - getScrollOffsetX();
        double screenPoseY = this.currentLayoutContentY + effectVirtualY_tb - getScrollOffsetY();

        setPose(graphics.poseStack(), (float)screenPoseX, (float)screenPoseY, effectScale);
        Matrix4f pose = graphics.poseStack().last().pose();
        MultiBufferSource.BufferSource bufferSource = graphics.graphics().bufferSource();
        Font.DisplayMode mode = Font.DisplayMode.NORMAL;
        int packedLightCoords = LightTexture.FULL_BRIGHT;

        Font font = Minecraft.getInstance().font;
        FontAccessor accessor = (FontAccessor) font;
        BakedGlyph whiteGlyph = accessor.dragonlib$invokeGetFontSet(Style.DEFAULT_FONT).whiteGlyph();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(whiteGlyph.renderType(mode));

        for (BakedGlyph.Effect effect : effectsBatch.getEffects()) {
            whiteGlyph.renderEffect(effect, pose, vertexConsumer, packedLightCoords);
        }
        clearPose(graphics.poseStack());
    }

    @Override
    public CursorType getCursor() {
        if (getResizeArea() == Align.CENTER && hoveredElement != null) {
            return CursorType.HAND;
        }
        return super.getCursor();
    }

    protected boolean onUnhover() {
        hoveredElement = null;
        return false;
    }

    protected boolean onHoverInteractiveElements(double mouseX, double mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        hoveredElement = null;
        if (true) {
            if (mouseX >= this.currentLayoutContentX &&
                mouseX <= this.currentLayoutContentX + this.currentLayoutContentWidth &&
                mouseY >= this.currentLayoutContentY &&
                mouseY <= this.currentLayoutContentY + this.currentLayoutContentHeight) {

                float scrolledMouseY = (float)(mouseY + getScrollOffsetY() - getLayoutContentY() - lineSpacing.get() / 2f);
                LineMarker selLine = getLineByYCoord((float)mouseY);
                if (selLine != null && scrolledMouseY >= selLine.y() - lineSpacing.get().floatValue() / 2f && scrolledMouseY <= selLine.y() + selLine.lineHeight() + lineSpacing.get().floatValue() / 2f) {
                    int charIdx = getIndexByPos((float)mouseX, (float)mouseY, false);
                    if (charIdx != -1) {
                        hoveredElement = text.get().getInteractiveElementAt(charIdx);
                        return false;
                    }
                }
            }
        }
        hoveredElement = null;
        return false;
    }


    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (text.get() == null) return;

        this.currentLayoutContentX = getLayoutContentX();
        this.currentLayoutContentY = getLayoutContentY();
        this.currentLayoutContentWidth = getLayoutContentWidth();
        this.currentLayoutContentHeight = getLayoutContentHeight();

        GuiUtils.enableScissor(graphics, (int)(renderBounds.x() + getLayoutContentX()), (int)(renderBounds.y() + getLayoutContentY()), (int)getLayoutContentWidth(), (int)getLayoutContentHeight());
        initRenderer();

        render: if (text.get().getSegmentsRaw() != null) {
            float estimatedMaxLineHeight = Minecraft.getInstance().font.lineHeight * 2;
            Map.Entry<Integer, LineMarker> firstMarkerEntry = getLineMarkersByY().floorEntry((int)getScrollOffsetY());
            if (firstMarkerEntry == null) {
                firstMarkerEntry = getLineMarkersByY().ceilingEntry((int)getScrollOffsetY());
                if (firstMarkerEntry == null) {
                    break render;
                }
            }

            graphics.poseStack().pushPose();
            for (TextSegment segment : text.get().getSegmentsRaw()) {
                final int segmentStartIndex = renderCharacterIndex;
                int skip = 0;
                if (multiline.get()) {
                    if (renderCharacterIndex + segment.length() < firstMarkerEntry.getValue().startIndex()) {
                        renderCharacterIndex += segment.length();
                        continue;
                    } else if (renderCharacterIndex < firstMarkerEntry.getValue().startIndex()) {
                        skip = Math.max(0, firstMarkerEntry.getValue().startIndex() - segmentStartIndex - 1);
                    }
                    if (currentMarker.y() + currentMarker.lineHeight() > getScrollOffsetY() + currentLayoutContentHeight + estimatedMaxLineHeight) {
                        break;
                    }
                }

                if (!renderSegment(graphics, segment, skip)) {
                    break;
                }
            }
            
            for (EffectBatch batch : effects) {
                postRender(graphics, batch);
            }
            graphics.poseStack().popPose();
        }

        graphics.graphics().bufferSource().endBatch();
        GuiUtils.enableScissor(graphics, (int)renderBounds.x(), (int)renderBounds.y(), (int)renderBounds.width(), (int)renderBounds.height());
    }

    @Override
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (hoveredElement != null) {
            renderInteractionElementOverlay(graphics, (int)(mouseX - getScrollOffsetX()), (int)(mouseY - getScrollOffsetY()), hoveredElement);
        }
    }

    protected void renderInteractionElementOverlay(DLGuiGraphics graphics, int mouseX, int mouseY, InteractiveElement element) {
        List<Component> lines = new ArrayList<>();
        if (element.hasActionFromType(InteractiveElement.HoverAction.class)) {
            lines.add(element.getActionFromType(InteractiveElement.HoverAction.class).text());
        }
        if (element.hasActionFromType(InteractiveElement.ClickAction.class)) {
            InteractiveElement.ClickAction action = element.getActionFromType(InteractiveElement.ClickAction.class);
            if (action.actionName().equals(InteractiveElement.ClickAction.OPEN_URL)) {
                lines.add(TextUtils.text(action.value()).withStyle(ChatFormatting.GRAY));
                lines.add(TextUtils.text("Click to open URL").withStyle(ChatFormatting.YELLOW));
            } else if (action.actionName().equals(InteractiveElement.ClickAction.COPY_TO_CLIPBAORD)) {
                lines.add(TextUtils.text("Click to copy to clipboard").withStyle(ChatFormatting.YELLOW));
            } else {
                lines.add(TextUtils.text("Click to run action").withStyle(ChatFormatting.YELLOW));
            }
        }
        
        GuiUtils.drawTooltip(graphics, Minecraft.getInstance().font, mouseX, mouseY, lines, (int)(getWindowManager().getScreenWidth() * 0.75f));
    }
}
