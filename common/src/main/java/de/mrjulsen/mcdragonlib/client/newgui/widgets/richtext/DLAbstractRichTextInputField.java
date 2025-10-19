package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents.MouseDownEvent;
import de.mrjulsen.mcdragonlib.client.newgui.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.client.newgui.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.client.newgui.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.client.newgui.properties.Property;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLRichTextLabel;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.action.InteractiveElement;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.TextCursorPosition;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.Color;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.*;

@SupportsEvents({
    DLGuiCommonEvents.TextReadOnlyChangedEvent.class,
    DLGuiCommonEvents.TextAcceptKeyPressedEvent.class,
    DLGuiCommonEvents.TextCancelKeyPressedEvent.class
})
public abstract class DLAbstractRichTextInputField extends DLRichTextLabel {

    private static final int CURSOR_BLINK_RATE = 20;
    private static final long DOUBLE_CLICK_TIME_MS = 300;
    private static final float LINE_SCROLL_MULTIPLIER = 12.0f;
    private static final int CURSOR_SCROLL_Y_OFFSET_PIXELS = 5;
    private static final int SCROLL_X_ADVANCE_PIXELS = 10;
    private static final int SCROLL_Y_ADVANCE_PIXELS = 10;
    private static final int MOUSE_MULTICLICK_PRECISION_THRESHOLD = 2;

    public final ColorProperty selectionColor = new ColorProperty(Color.fromInt(0x800055FF), Color.fromInt(0x800055FF));
    public final ColorProperty lineHighlightColor = new ColorProperty(Color.fromInt(0x30FFFFFF), Color.TRANSPARENT);
    public final BooleanProperty showLineHighlight = new BooleanProperty(false, false);
    public final BooleanProperty readOnly = new BooleanProperty(false, false)
        .withAfterPropertyChangedCallback((o, x) -> invokeEvent(this, new DLGuiCommonEvents.TextReadOnlyChangedEvent(x)));
    public final Property<Component> placeholderText = new Property<>(TextUtils.empty());
    public final NumberProperty<Byte> cursorWidth = new NumberProperty<>((byte)1, (byte)1, Byte.MAX_VALUE);
    public final Property<Padding> decoratedPadding = new Property<>(Padding.ZERO);
    public final BooleanProperty acceptAndCancelKeysEnabled = new BooleanProperty(false, false);
    public final BooleanProperty hideSelection = new BooleanProperty(true, false); // TODO
    public final NumberProperty<Integer> cursorXOffset = new NumberProperty<>(5);


    // Cursor
    private int cursorBlink = 0;
    private int globalCursorIndex = 0;
    private int selectionAnchorIndex = 0;
    // Selection
    private boolean isDragging = false;
    private long lastClickTime = 0;
    private int clickCount = 0;
    private double lastClickMouseX = 0;
    private double lastClickMouseY = 0;


    public DLAbstractRichTextInputField(int x, int y, int w, int h) {
        super(x, y, w, h);

        addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (a, b) -> this.onKeyPressed(b.keyCode(), b.scanCode(), b.modifiers()));
        addEventListener(DLGuiStandardEvents.CharTypeEvent.class, (a, b) -> this.onCharTyped(b.codePoint(), b.modifiers()));
        addEventListener(DLGuiStandardEvents.MouseDownEvent.class, (a, b) -> this.onMouseDown2(b.mouseX(), b.mouseY(), b.button()));
        addEventListener(DLGuiStandardEvents.MultiClickEvent.class, (a, b) -> this.onMouseDown2(b.mouseX(), b.mouseY(), b.button()));
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (src, e) -> this.onScroll(e.mouseX(), e.mouseY(), e.deltaX(), e.deltaY()));
        addEventListener(DLGuiStandardEvents.DragEvent.class, this::onMouseDragged2);
        addEventListener(DLGuiStandardEvents.MouseUpEvent.class, this::onMouseUp2);

        // TEST
        setTextCursor(0, true);
        cursor.set(CursorType.IBEAM);        
    }

    private int getCursorXOffsetWrapper() {
        return cursorXOffset == null ? 5 : cursorXOffset.get();
    }

    @Override
    protected boolean onTextElementClicked(DLGuiComponent src, MouseDownEvent event) {
        if (Screen.hasControlDown()) {
            return super.onTextElementClicked(src, event);
        }
        return false;
    }
    
    @Override
    protected boolean onHoverInteractiveElements(double mouseX, double mouseY) {
        LineMarker line = getLineAt(getGlobalCursorIndex());
        if (line == null) return false;
        double yOffset = multiline.get() ? 0 : (float)getLayoutContentHeight() / 2f - (float)line.lineHeight() / 2f - lineSpacing.get() / 2f;
        return super.onHoverInteractiveElements(mouseX, mouseY - yOffset);
    }

    /* GETTERS AND SETTERS */

    @Override
    protected float getLayoutContentX() {
        return super.getLayoutContentX() + (decoratedPadding == null ? 0 : decoratedPadding.get().left());
    }

    @Override
    protected float getLayoutContentY() {
        return super.getLayoutContentY() + (decoratedPadding == null ? 0 : decoratedPadding.get().top());
    }

    @Override
    protected float getLayoutContentWidth() {
        return super.getLayoutContentWidth() - (decoratedPadding == null ? 0 : decoratedPadding.get().right() + decoratedPadding.get().left());
    }

    @Override
    protected float getLayoutContentHeight() {
        return super.getLayoutContentHeight() - (decoratedPadding == null ? 0 : decoratedPadding.get().bottom() + decoratedPadding.get().top());
    }

    public int getGlobalCursorIndex() {
        return globalCursorIndex;
    }

    public int getSelectionAnchorIndex() {
        return selectionAnchorIndex;
    }

    public int getSelectionStart() {
        return Math.min(selectionAnchorIndex, globalCursorIndex);
    }

    public int getSelectionEnd() {
        return Math.max(selectionAnchorIndex, globalCursorIndex);
    }

    public String getSelectedText() {
        if (!hasSelection() || text.get() == null) return "";
        return text.get().subComponent(getSelectionStart(), getSelectionEnd()).plainText(); // TODO substring
    }

    public boolean hasSelection() {
        return selectionAnchorIndex != globalCursorIndex;
    }

    public int getLineCount() {
        List<LineMarker> lines = getLinesOrdered();
        return lines != null ? lines.size() : 0;
    }

    public String getTextInRange(int startIndex, int endIndex) {
        if (text.get() == null || startIndex < 0 || endIndex > text.get().length() || startIndex >= endIndex) {
            return "";
        }
        return text.get().subComponent(startIndex, endIndex).plainText(); // TODO substring
    }

    public void scrollToLine(int lineIndex, boolean centerVertically) {
        List<LineMarker> lines = getLinesOrdered();
        if (lines == null || lines.isEmpty() || lineIndex < 0 || lineIndex >= lines.size()) {
            return;
        }

        LineMarker marker = lines.get(lineIndex);
        float y = marker.y() - lineSpacing.get() / 2f;
        if (centerVertically) {
            y = y - getLayoutContentHeight() / 2f + (marker.lineHeight() + lineSpacing.get()) / 2f;
        }
        setScrollOffsetY(MathUtils.clamp(y, 0, getMaxScrollY()));
    }

    private boolean isNewLineCharBeforeLine(LineMarker line, String plainText) {
        if (line.startIndex() <= 0) {
            return false;
        }
        RichTextComponent rtc = text.get();
        if (rtc == null || plainText == null) return false;
        return plainText.codePointBefore(line.startIndex()) == '\n';
    }

    @Override
    public void refresh() {
        Font font = Minecraft.getInstance().font;
        super.refresh();
        setTextCursor(globalCursorIndex, false);
    }

    private void deleteSelection() {
        if (readOnly.get()) return;
        if (hasSelection() && text.get() != null) {
            int start = getSelectionStart();
            int end = getSelectionEnd();
            text.get().remove(start, end);
            setTextCursor(start, true);
        }
    }

    public void replaceTextInternal(int startIndex, int endIndex, String text, TextStyle style) {
        if (readOnly.get()) return;
        if (this.text.get() == null) return;

        int originalLengthOfReplacement = text.codePointCount(0, text.length());

        if (startIndex == getSelectionStart() && endIndex == getSelectionEnd() && hasSelection()) {
            deleteSelection();
            this.text.get().insert(globalCursorIndex, text, style, null);
            setTextCursor(getGlobalCursorIndex() + originalLengthOfReplacement, true);
        } else {
            this.text.get().remove(startIndex, endIndex);
            this.text.get().insert(startIndex, text, style, null);
            setTextCursor(startIndex + originalLengthOfReplacement, true);
        }
    }

    public float getMaxScrollY() {
        if (!multiline.get()) return 0;
        List<LineMarker> lines = getLinesOrdered();
        if (lines.isEmpty()) return 0;
        LineMarker lastLine = lines.get(lines.size() - 1);
        return lastLine.y() + lastLine.lineHeight() + lineSpacing.get() - getLayoutContentHeight() + SCROLL_Y_ADVANCE_PIXELS;
    }

    public float getMaxScrollX() {
        if (lineWrap.get()) return 0;
        List<LineMarker> lines = getLinesOrdered();
        if (lines.isEmpty()) return 0;
        float maxW = 0;
        for (LineMarker marker : lines) {
            maxW = Math.max(marker.lineWidth(), maxW); // TODO Optimieren durch Speichern der maximalen Breite beim Berechnen der Zeilen
        }
        return maxW - getLayoutContentWidth() + SCROLL_X_ADVANCE_PIXELS;
    }

    public boolean onScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean changed = false;
        if (multiline.get() && scrollY != 0) {
            float currentScrollY = (float)getScrollOffsetY();
            float newScrollY = currentScrollY + (float)(scrollY * LINE_SCROLL_MULTIPLIER);
            newScrollY = MathUtils.clamp(newScrollY, 0, getMaxScrollY());
            if (Float.compare(currentScrollY, newScrollY) != 0) {
                setScrollOffsetY(newScrollY);
                changed = true;
            }
        }

        boolean shouldAllowHorizontalScroll = multiline.get() || !lineWrap.get();
        if (shouldAllowHorizontalScroll && scrollX != 0) {
            float currentScrollX = (float)getScrollOffsetX();
            float newScrollX = currentScrollX + (float)(scrollX * LINE_SCROLL_MULTIPLIER);
            newScrollX = MathUtils.clamp(newScrollX, 0, getMaxScrollX());
            if (Float.compare(currentScrollX, newScrollX) != 0) {
                setScrollOffsetX(newScrollX);
                changed = true;
            }
        }
        return true;
    }

    private String getClipboard() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    private void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    @Override
    public void tick() {
        super.tick();
        cursorBlink++;
        cursorBlink %= CURSOR_BLINK_RATE;
    }

    @Override
    public void renderMainLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        Font font = Minecraft.getInstance().font;

        LineMarker currentLine = getLineAt(globalCursorIndex);
        if (currentLine == null) return;

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, multiline.get() ? 0 : getLayoutContentHeight() / 2f - (float)currentLine.lineHeight() / 2f - lineSpacing.get() / 2f, 0);

        // Line marker
        if (isFocused() && showLineHighlight.get() && globalCursorIndex >= 0 && text.get() != null && globalCursorIndex <= text.get().length()) {            
            float lineMarkerY = (float)(currentLine.y() - (lineSpacing.get() / 2f) - getScrollOffsetY());
            float lineMarkerHeight = currentLine.lineHeight() + lineSpacing.get();
            if (lineMarkerY + lineMarkerHeight > 0 && lineMarkerY < height()) {
                int targetY = (int)(lineMarkerY + getLayoutContentY());
                int targetH = (int)lineMarkerHeight;
                int realY = (int)MathUtils.clamp(targetY, getLayoutContentY(), height() - getLayoutContentY());
                int diffY = Math.max(0, realY - targetY);
                int realH = (int)MathUtils.clamp(targetH - diffY, 0, height() - realY - contentPadding.get().bottom() - decoratedPadding.get().bottom());
                GuiUtils.fill(graphics, decoratedPadding.get().left(), realY, width() - decoratedPadding.get().left() - decoratedPadding.get().right(), realH, lineHighlightColor.get());
            }
        }

        // Render text
        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);

        if (text.get() != null && text.get().getPlainText().isEmpty() && placeholderText.get() != null && !isFocused()) {
            GuiUtils.drawString(graphics, font, (int)getLayoutContentX(), (int)(getLayoutContentY() + (lineSpacing.get() / 2f)), placeholderText.get(), Color.fromInt(0xFF808080), ETextAlignment.LEFT, false);
        }
        
        GuiUtils.enableScissor(graphics, (int)(renderBounds.x() + getLayoutContentX()), (int)(renderBounds.y() + getLayoutContentY()), (int)getLayoutContentWidth(), (int)getLayoutContentHeight());

        if (hasSelection()) {
            renderSelection(graphics);
        }

        if (isFocused() && !readOnly.get() && this.cursorBlink < CURSOR_BLINK_RATE / 2) {
            TextCursorPosition textCursorPos = getPosByIndex(globalCursorIndex);
            if (textCursorPos != null) {
                int curX = (int)(textCursorPos.x() - getScrollOffsetX() + getLayoutContentX());
                int curY = (int)(textCursorPos.y() - (lineSpacing.get() / 2f) - getScrollOffsetY() + getLayoutContentY());
                int curH = (int)(textCursorPos.lineHeight() + lineSpacing.get());
                graphics.graphics().fill(RenderType.guiTextHighlight(), curX, curY, curX + cursorWidth.get(), curY + curH, 0xFF0000FF);
            }
        }

        GuiUtils.enableScissor(graphics, (int)renderBounds.x(), (int)renderBounds.y(), (int)renderBounds.width(), (int)renderBounds.height());        
        graphics.poseStack().popPose();
    }

    public void renderTextRangeHighlights(de.mrjulsen.mcdragonlib.client.util.Graphics mcGraphics, List<TextRange> ranges, Color color) {
        if (getLinesOrdered().isEmpty() || ranges.isEmpty() || text.get() == null) return;

        mcGraphics.poseStack().pushPose();
        mcGraphics.poseStack().translate(0, 0, 0.5F);

        final Map.Entry<Integer, LineMarker> lowerMarkerY = getLineMarkersByY().floorEntry((int)getScrollOffsetY());
        final Map.Entry<Integer, LineMarker> upperMarkerY = getLineMarkersByY().ceilingEntry((int)(getScrollOffsetY() + getLayoutContentHeight()));
        final int minVisibleIdx = lowerMarkerY == null ? Integer.MIN_VALUE : lowerMarkerY.getValue().startIndex();
        final int maxVisibleIdx = upperMarkerY == null ? Integer.MAX_VALUE : upperMarkerY.getValue().startIndex();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        for (TextRange range : ranges) {
            int selStart = range.start();
            int selEnd = range.end();
            if (selEnd <= selStart) continue;
            if (selEnd < minVisibleIdx || selStart > maxVisibleIdx) continue;

            Map.Entry<Integer, LineMarker> lowerMarker = getLineMarkers().floorEntry(selStart);
            Map.Entry<Integer, LineMarker> upperMarker = getLineMarkers().floorEntry(selEnd);
            if (lowerMarker == null || upperMarker == null) continue;

            int fromIdx = Math.max(lowerMarker.getValue().startIndex(), minVisibleIdx);
            int toIdx = Math.min(upperMarker.getValue().startIndex(), maxVisibleIdx);
            if (toIdx < fromIdx) continue;

            Collection<LineMarker> markers = getLineMarkers().subMap(fromIdx, true, toIdx, true).values();
            for (LineMarker marker : markers) {
                TextCursorPosition posA = getPosByIndex(Math.max(selStart, marker.startIndex()));
                TextCursorPosition posB = getPosByIndex(Math.min(selEnd, marker.endIndex()));
                GuiUtils.fill(mcGraphics, (int)(getLayoutContentX() + posA.x() - getScrollOffsetX()), (int)(getLayoutContentY() + posA.y() - (lineSpacing.get() / 2f) - getScrollOffsetY()), (int)(posB.x() - posA.x()), (int)posA.lineHeight() + lineSpacing.get(), color);
            }

        }
        RenderSystem.disableBlend();
        mcGraphics.poseStack().popPose();
    }

    private void renderSelection(de.mrjulsen.mcdragonlib.client.util.Graphics mcGraphics) {
        renderTextRangeHighlights(mcGraphics, List.of(new TextRange(getSelectionStart(), getSelectionEnd())), selectionColor.get());
    }

    @Override
    public LineMarker getLineByYCoord(float y) {
        return super.getLineByYCoord((float)(y + getScrollOffsetY()));
    }

    public boolean onMouseDown2(double mouseX, double mouseY, int button) {
        if (isFocused() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int clickedIndex = getIndexByPos((float)mouseX, (float)mouseY, true);

            //InteractiveElement element = text.get() != null ? text.get().getInteractiveElementAt(clickedIndex) : null;
            //if (element != null && element.clickAction != null && Screen.hasControlDown()) {
            //    element.clickAction.onClick();
            //    isDragging = false;
            //        return true;
            //}

            this.isDragging = true;
            long currentTime = Util.getMillis();
            if (currentTime - lastClickTime < DOUBLE_CLICK_TIME_MS && Math.abs(mouseX - lastClickMouseX) < MOUSE_MULTICLICK_PRECISION_THRESHOLD && Math.abs(mouseY - lastClickMouseY) < MOUSE_MULTICLICK_PRECISION_THRESHOLD) {
                this.clickCount++;
            } else {
                this.clickCount = 1;
            }
            this.lastClickTime = currentTime;
            this.lastClickMouseX = mouseX;
            this.lastClickMouseY = mouseY;

            if (clickCount == 2) {
                selectWordAt(globalCursorIndex);
            } else if (clickCount >= 3) {
                selectLineAt(globalCursorIndex);
                clickCount = 0;
            } else {
                setTextCursor(clickedIndex, !Screen.hasShiftDown());
            }
        }
        return true;
    }

    public boolean onMultiClicked(double mouseX, double mouseY, int button, int clickCount) {
        if (isFocused() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int clickedIndex = getIndexByPos((float)mouseX, (float)mouseY, true);

            //InteractiveElement element = text.get() != null ? text.get().getInteractiveElementAt(clickedIndex) : null;
            //if (element != null && element.clickAction != null && Screen.hasControlDown()) {
            //    element.clickAction.onClick();
            //    isDragging = false;
            //        return true;
            //}

            this.isDragging = true;
            long currentTime = Util.getMillis();
            if (currentTime - lastClickTime < DOUBLE_CLICK_TIME_MS && Math.abs(mouseX - lastClickMouseX) < MOUSE_MULTICLICK_PRECISION_THRESHOLD && Math.abs(mouseY - lastClickMouseY) < MOUSE_MULTICLICK_PRECISION_THRESHOLD) {
                this.clickCount++;
            } else {
                this.clickCount = 1;
            }
            this.lastClickTime = currentTime;
            this.lastClickMouseX = mouseX;
            this.lastClickMouseY = mouseY;

            if (clickCount == 2) {
                selectWordAt(globalCursorIndex);
            } else if (clickCount >= 3) {
                selectLineAt(globalCursorIndex);
                clickCount = 0;
            } else {
                setTextCursor(clickedIndex, !Screen.hasShiftDown());
            }
        }
        return true;
    }

    public boolean onMouseDragged2(DLGuiComponent src, DLGuiStandardEvents.DragEvent event) {
        if (isFocused() && isDragging && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setTextCursor(getIndexByPos((float)event.mouseX(), (float)event.mouseY(), true), false);
        }
        return true;
    }

    public boolean onMouseUp2(DLGuiComponent src, DLGuiStandardEvents.MouseUpEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.isDragging = false;
        }
        return true;
    }

    public boolean onCharTyped(char codePointChar, int modifiers) {
        if (readOnly.get()) return false;

        if (isFocused() && SharedConstants.isAllowedChatCharacter(codePointChar) && text.get() != null) {
            int cursorPosBeforeInsert = getGlobalCursorIndex();
            deleteSelection();
            cursorPosBeforeInsert = getGlobalCursorIndex();
            String charToInsert = SharedConstants.filterText(Character.toString(codePointChar));
            int codePointsInserted = text.get().insert(cursorPosBeforeInsert, charToInsert, null, null);

            if (!Character.isLowSurrogate(codePointChar)) {
                setTextCursor(cursorPosBeforeInsert + codePointsInserted, true);
            }
            return true;
        }
        return false;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused() && text.get() != null) {
            boolean controlDown = Screen.hasControlDown();
            boolean shiftDown = Screen.hasShiftDown();
            boolean canModify = !readOnly.get();

            if (controlDown) {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_ENTER:
                    case GLFW.GLFW_KEY_KP_ENTER:
                        if (canModify) {
                            if (acceptAndCancelKeysEnabled.get() && !controlDown && !shiftDown) {
                                invokeEvent(this, new DLGuiCommonEvents.TextAcceptKeyPressedEvent());
                            } else if (multiline.get()) {
                                deleteSelection();
                                text.get().insert(getGlobalCursorIndex(), "\n", null, null);
                                setTextCursor(getGlobalCursorIndex() + 1, true);
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_A:
                        selectAll();
                        break;
                    case GLFW.GLFW_KEY_C:
                        copySelected();
                        break;
                    case GLFW.GLFW_KEY_X:
                        cutSelected();
                        break;
                    case GLFW.GLFW_KEY_V:
                        paste();
                        break;
                    case GLFW.GLFW_KEY_BACKSPACE:
                        if (canModify) {
                            if (hasSelection()) {
                                deleteSelection();
                            } else if (getGlobalCursorIndex() > 0) {
                                String text = this.text.get().getPlainText();
                                int wordStart = findPrevWordBoundary2(text, getGlobalCursorIndex(), true);
                                this.text.get().remove(wordStart, getGlobalCursorIndex());
                                setTextCursor(wordStart, true);
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_DELETE:
                        if (canModify) {
                            if (hasSelection()) {
                                deleteSelection();
                            } else if (getGlobalCursorIndex() < text.get().length()) {
                                String text = this.text.get().getPlainText();
                                int wordEnd = findNextWordBoundary2(text, getGlobalCursorIndex(), true);
                                this.text.get().remove(getGlobalCursorIndex(), wordEnd);
                                setTextCursor(getGlobalCursorIndex(), true);
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_LEFT:
                        if (getGlobalCursorIndex() > 0) {
                            setTextCursor(findPrevWordBoundary2(text.get().getPlainText(), getGlobalCursorIndex(), true), !shiftDown);
                        }
                        break;
                    case GLFW.GLFW_KEY_RIGHT:
                        if (getGlobalCursorIndex() < text.get().length()) {
                            setTextCursor(findNextWordBoundary2(text.get().getPlainText(), getGlobalCursorIndex(), true), !shiftDown);
                        }
                        break;
                    case GLFW.GLFW_KEY_UP:
                        moveCursorToPreviousParagraphStart(shiftDown);
                        break;
                    case GLFW.GLFW_KEY_DOWN:
                        moveCursorToNextParagraphStart(shiftDown);
                        break;
                    case GLFW.GLFW_KEY_HOME:
                        setTextCursor(0, !shiftDown);
                        break;
                    case GLFW.GLFW_KEY_END:
                        setTextCursor(text.get().length(), !shiftDown);
                        break;
                    default:
                        break;
                }
            } else {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_ESCAPE:
                        if (canModify && acceptAndCancelKeysEnabled.get()) {
                            invokeEvent(this, new DLGuiCommonEvents.TextCancelKeyPressedEvent());
                        }
                        break;
                    case GLFW.GLFW_KEY_ENTER:
                    case GLFW.GLFW_KEY_KP_ENTER:
                        if (canModify) {
                            if (acceptAndCancelKeysEnabled.get() && !shiftDown) {
                                invokeEvent(this, new DLGuiCommonEvents.TextAcceptKeyPressedEvent());
                            } else if (multiline.get()) {
                                deleteSelection();
                                text.get().insert(getGlobalCursorIndex(), "\n", null, null);
                                setTextCursor(getGlobalCursorIndex() + 1, true);
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_BACKSPACE:
                        if (canModify) {
                            if (hasSelection()) {
                                deleteSelection();
                            } else if (getGlobalCursorIndex() > 0) {
                                int newCursorPos = getGlobalCursorIndex() - 1;
                                text.get().remove(getGlobalCursorIndex() - 1, getGlobalCursorIndex());
                                setTextCursor(newCursorPos, true);
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_DELETE:
                        if (canModify) {
                            if (hasSelection()) {
                                deleteSelection();
                            } else if (getGlobalCursorIndex() < text.get().length()) {
                                text.get().remove(getGlobalCursorIndex(), getGlobalCursorIndex() + 1);
                                setTextCursor(getGlobalCursorIndex(), true);
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_LEFT:
                        if (getGlobalCursorIndex() > 0) {
                            setTextCursor(getGlobalCursorIndex() - 1, !shiftDown);
                        }
                        break;
                    case GLFW.GLFW_KEY_RIGHT:
                        if (getGlobalCursorIndex() < text.get().length()) {
                            setTextCursor(getGlobalCursorIndex() + 1, !shiftDown);
                        }
                        break;
                    case GLFW.GLFW_KEY_UP:
                        TextCursorPosition textCursorPosUp = getPosByIndex(getGlobalCursorIndex());
                        LineMarker currentLineUp = getLineAt(getGlobalCursorIndex());
                        int lineIndexUp = getLinesOrdered().indexOf(currentLineUp); // TODO lineIndex in marker
                        if (lineIndexUp > 0) {
                            LineMarker prevLine = getLineByLineIndex(lineIndexUp - 1);
                            float targetComponentLocalX = (float)((x() + getLayoutContentX() + textCursorPosUp.x() - getScrollOffsetX()) - x());
                            float targetComponentLocalY = (float)((y() + getLayoutContentY() + prevLine.y() + prevLine.lineHeight() / 2f - getScrollOffsetY()) - y());
                            setTextCursor(getIndexByPos(targetComponentLocalX, targetComponentLocalY, true), !shiftDown);
                        } else {
                            setTextCursor(0, !shiftDown);
                        }
                        break;
                    case GLFW.GLFW_KEY_DOWN:
                        TextCursorPosition textCursorPosDown = getPosByIndex(getGlobalCursorIndex());
                        LineMarker currentLineDown = getLineAt(getGlobalCursorIndex());
                        int lineIndexDown = getLinesOrdered().indexOf(currentLineDown); // TODO lineIndex in marker
                        if (lineIndexDown < getLinesOrdered().size() - 1) {
                            LineMarker nextLine = getLineByLineIndex(lineIndexDown + 1);
                            float targetComponentLocalX = (float)((x() + getLayoutContentX() + textCursorPosDown.x() - getScrollOffsetX()) - x());
                            float targetComponentLocalY = (float)((y() + getLayoutContentY() + nextLine.y() + nextLine.lineHeight() / 2f - getScrollOffsetY()) - y());
                            setTextCursor(getIndexByPos(targetComponentLocalX, targetComponentLocalY, true), !shiftDown);
                        } else {
                            setTextCursor(text.get().length(), !shiftDown);
                        }
                        break;
                    case GLFW.GLFW_KEY_HOME:
                        LineMarker currentLineHome = getLineAt(getGlobalCursorIndex());
                        setTextCursor(currentLineHome.startIndex(), !shiftDown);
                        break;
                    case GLFW.GLFW_KEY_END:
                        LineMarker currentLineEnd = getLineAt(getGlobalCursorIndex());
                        setTextCursor(currentLineEnd.endIndex(), !shiftDown);
                        break;
                    default:
                        break;
                }
            }
        }
        return true;
    }


    public void setTextCursor(int index, boolean clearAnchor) {
        if (text.get() == null) return;
        this.globalCursorIndex = MathUtils.clamp(index, 0, text.get().length());
        this.selectionAnchorIndex = clearAnchor ? globalCursorIndex : MathUtils.clamp(selectionAnchorIndex, 0, text.get().length());
        TextCursorPosition textCursorPosition = getPosByIndex(globalCursorIndex);

        if (multiline.get()) {
            float y = (float)getScrollOffsetY();
            if (textCursorPosition.y() - CURSOR_SCROLL_Y_OFFSET_PIXELS < getScrollOffsetY()) {
                y = textCursorPosition.y() - CURSOR_SCROLL_Y_OFFSET_PIXELS;
            } else if (textCursorPosition.y() + CURSOR_SCROLL_Y_OFFSET_PIXELS + textCursorPosition.lineHeight() + lineSpacing.get() > getScrollOffsetY() + getLayoutContentHeight()) {
                y = textCursorPosition.y() - getLayoutContentHeight() + textCursorPosition.lineHeight() + lineSpacing.get() + CURSOR_SCROLL_Y_OFFSET_PIXELS;
            }
            if (getScrollOffsetY() != y) {
                setScrollOffsetY(MathUtils.clamp(y, 0, getMaxScrollY()));
            }
        }

        float x = (float)getScrollOffsetX();
        if (textCursorPosition.x() - getCursorXOffsetWrapper() < getScrollOffsetX()) {
            x = textCursorPosition.x() - getCursorXOffsetWrapper();
        } else if (textCursorPosition.x() + cursorWidth.get() + getCursorXOffsetWrapper() > getScrollOffsetX() + getLayoutContentWidth()) {
            x = textCursorPosition.x() - getLayoutContentWidth() + cursorWidth.get() + getCursorXOffsetWrapper();
        }
        if (getScrollOffsetX() != x) {
            setScrollOffsetX(MathUtils.clamp(x, 0, getMaxScrollX()));
        }

        this.cursorBlink = 0;
    }

    private int findPrevWordBoundary2(String text, int startIndex, boolean includeWhitespaces) {
        if (startIndex <= 1) {
            return 0;
        }

        int idx = startIndex;
        int c = text.codePointAt(text.offsetByCodePoints(0, idx - 1));
        boolean whitespace = Character.isWhitespace(c);
        boolean stopAtWhitespace = false;
        final boolean initialIsWhitespace = whitespace;
        while ((!Character.isWhitespace(c) || !stopAtWhitespace) && (includeWhitespaces || whitespace == initialIsWhitespace)) {
            idx--;
            if (idx <= 0) {
                break;
            }
            stopAtWhitespace |= !Character.isWhitespace(c);
            c = text.codePointAt(text.offsetByCodePoints(0, idx - 1));
            whitespace = Character.isWhitespace(c);
        }
        return idx;
    }

    private int findNextWordBoundary2(String text, int startIndex, boolean includeWhitespaces) {
        int cpLength = text.codePointCount(0, text.length());
        if (startIndex >= cpLength - 1) {
            return cpLength;
        }

        int idx = startIndex;
        int c = text.codePointAt(text.offsetByCodePoints(0, idx));
        boolean whitespace = Character.isWhitespace(c);
        boolean stopAtNonWhitespace = false;
        final boolean initialIsWhitespace = whitespace;
        while ((Character.isWhitespace(c) || !stopAtNonWhitespace) && (includeWhitespaces || whitespace == initialIsWhitespace)) {
            idx++;
            if (idx >= cpLength) {
                break;
            }
            stopAtNonWhitespace |= Character.isWhitespace(c);
            c = text.codePointAt(text.offsetByCodePoints(0, idx));
            whitespace = Character.isWhitespace(c);
        }
        return idx;
    }

    public void selectWordAt(int index) {
        int a = findPrevWordBoundary2(text.get().getPlainText(), index, false);
        int b = findNextWordBoundary2(text.get().getPlainText(), index, false);
        select(Math.min(a, b), Math.max(a, b));
    }

    public void selectLineAt(int index) {
        if (text.get() == null) return;
        LineMarker line = getLineAt(index);
        select(line.startIndex(), line.endIndex());
    }

    public void selectAll() {
        if (text.get() == null) return;
        select(0, text.get().length());
    }

    public void cutSelected() {
        if (!readOnly.get() && hasSelection()) {
            setClipboard(getSelectedText());
            deleteSelection();
        }
    }

    public void copySelected() {        
        if (hasSelection()) {
            setClipboard(getSelectedText());
        }
    }

    public void paste() {
        if (!readOnly.get()) {
            deleteSelection();
            String clipboardText = getClipboard();
            if (!clipboardText.isEmpty()) {
                String filteredText = SharedConstants.filterText(clipboardText);
                boolean validUrl = DLUtils.isValidURL(clipboardText);
                int insertIndex = getGlobalCursorIndex();
                int insertedCodePoints = text.get().insert(insertIndex, filteredText, validUrl ? TextStyle.URL_STYLE : null, null);
                if (validUrl) {
                    text.get().createInteractiveElement(insertIndex, insertIndex + insertedCodePoints).ifPresent(x -> {
                        x.withAction(new InteractiveElement.ClickAction(InteractiveElement.ClickAction.OPEN_URL, clipboardText));
                    });
                }
                setTextCursor(getGlobalCursorIndex() + insertedCodePoints, true);
            }
        }
    }

    public void select(int start, int end) {
        if (text.get() == null) return;
        selectionAnchorIndex = MathUtils.clamp(start, 0, text.get().length());
        setTextCursor(MathUtils.clamp(end, 0, text.get().length()), false);
    }
    public void deselect() {
        setTextCursor(globalCursorIndex, true);
    }

    public void selectParagraphAt(int codePointIndex) {
        if (text.get() == null || text.get().getPlainText().isEmpty()) {
            selectionAnchorIndex = 0;
            globalCursorIndex = 0;
            return;
        }
        String text = this.text.get().getPlainText();
        int textCpLength = this.text.get().length();
        int targetCpIndex = Math.max(0, Math.min(codePointIndex, textCpLength));
        int paraStartCp = 0;
        if (targetCpIndex > 0) {
            boolean currentIsParaStart = (targetCpIndex == 0);
            if (!currentIsParaStart && targetCpIndex > 0 && targetCpIndex <= textCpLength) {
                int charIdxBefore = this.text.get().toCharIndex(text, targetCpIndex - 1);
                if (charIdxBefore < text.length() && text.codePointAt(charIdxBefore) == '\n') currentIsParaStart = true;
            }
            if (currentIsParaStart) paraStartCp = targetCpIndex;
            else {
                int searchEndCharIndexForStart = this.text.get().toCharIndex(text, targetCpIndex);
                int prevNewlineCharIdx = -1;
                if (searchEndCharIndexForStart > 0)
                    prevNewlineCharIdx = text.substring(0, searchEndCharIndexForStart).lastIndexOf('\n');
                if (prevNewlineCharIdx != -1) paraStartCp = text.codePointCount(0, prevNewlineCharIdx + 1);
                else paraStartCp = 0;
            }
        }
        int paraEndCp = textCpLength;
        int searchStartCharIdxForEnd = this.text.get().toCharIndex(text, paraStartCp);
        int nextNewlineCharIdx = text.indexOf('\n', searchStartCharIdxForEnd);
        if (nextNewlineCharIdx != -1) paraEndCp = text.codePointCount(0, nextNewlineCharIdx);
        selectionAnchorIndex = paraStartCp;
        globalCursorIndex = paraEndCp;
    }

    private void moveCursorToPreviousParagraphStart(boolean select) {
        if (text.get() == null) return;
        String text = this.text.get().getPlainText();
        if (text.isEmpty()) globalCursorIndex = 0;
        else {
            int currentCPI = globalCursorIndex;
            if (currentCPI != 0) {
                int searchBeforeCPI = currentCPI;
                if (currentCPI > 0) {
                    int charBeforeCursorIdx = this.text.get().toCharIndex(text, currentCPI - 1);
                    if (charBeforeCursorIdx < text.length() && text.codePointAt(charBeforeCursorIdx) == '\n')
                        searchBeforeCPI = currentCPI - 1;
                }
                if (searchBeforeCPI <= 0) globalCursorIndex = 0;
                else {
                    int searchEndCharIndex = this.text.get().toCharIndex(text, searchBeforeCPI);
                    int lastNewlineCharIndex = -1;
                    if (searchEndCharIndex > 0)
                        lastNewlineCharIndex = text.substring(0, searchEndCharIndex).lastIndexOf('\n');
                    if (lastNewlineCharIndex != -1)
                        globalCursorIndex = text.codePointCount(0, lastNewlineCharIndex + 1);
                    else globalCursorIndex = 0;
                }
            }
        }
        if (!select) selectionAnchorIndex = globalCursorIndex;
    }

    private void moveCursorToNextParagraphStart(boolean select) {
        if (text.get() == null) return;
        String text = this.text.get().getPlainText();
        int textCpLength = this.text.get().length();
        if (text.isEmpty() || globalCursorIndex >= textCpLength) globalCursorIndex = textCpLength;
        else {
            int searchStartCharIndex = this.text.get().toCharIndex(text, globalCursorIndex);
            int nextNewlineCharIndex = text.indexOf('\n', searchStartCharIndex);
            if (nextNewlineCharIndex != -1) {
                globalCursorIndex = text.codePointCount(0, nextNewlineCharIndex + 1);
                globalCursorIndex = Math.min(globalCursorIndex, textCpLength);
            } else globalCursorIndex = textCpLength;
        }
        if (!select) selectionAnchorIndex = globalCursorIndex;
    }

    public enum LineNumberDisplayMode {NONE, LINE, PARAGRAPH}
    public record TextRange(int start, int end) {}
}
