package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

import com.google.common.collect.Maps;
import de.mrjulsen.mcdragonlib.core.ETextAlignment;
import de.mrjulsen.mcdragonlib.mixin.FontAccessor;
import de.mrjulsen.mcdragonlib.util.Cache;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.action.InteractiveElement;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class RichTextComponent {

    public record ImmutableRichTextComponent(List<TextSegment.ImmutableTextSegment> segments) {
        public static final ImmutableRichTextComponent EMPTY = new ImmutableRichTextComponent(List.of());

        public String plainText() {
            return segments().stream().map(TextSegment.ImmutableTextSegment::text).collect(Collectors.joining());
        }
    }

    private static final WidthProvider widthProvider = (style, characterCodePoint) -> {
        FontAccessor accessor = (FontAccessor)style.font();
        return accessor.dragonlib$invokeGetFontSet(Style.DEFAULT_FONT).getGlyphInfo(characterCodePoint, accessor.dragonlib$filterFishyGlyphs()).getAdvance(style.bold()) * style.scale();
    };

    private final Cache<Float> textWidthCache = new Cache<>(() -> calcWidthOfSection(0, length()));
    private final Cache<Integer> textLengthCache = new Cache<>(() -> {
        int i = 0;
        for (TextSegment segment : getSegmentsRaw()) {
            i += segment.codePointLength();
        }
        return i;
    });
    private final Cache<String> plainTextCache = new Cache<>(() -> {
        StringBuilder sb = new StringBuilder();
        for (TextSegment seg : getSegmentsRaw()) {
            sb.append(seg.stringBuilder());
        }
        return sb.toString();
    });

    @FunctionalInterface
    public static interface ITextValidator {
        String validate(String current, String newText, String input);
    }

    private static final String NBT_SEGMENTS = "segments";
    private static final String NBT_ALIGNMENTS = "alignments";
    private static final String NBT_INTERACTIVE_ELEMENTS = "actions";

    private static final ETextAlignment DEFAULT_ALIGNMENT = ETextAlignment.LEFT;
    public static final TextStyle DEFAULT_LINK_STYLE = new TextStyle.Builder().underlined(true).color(0xFF5555FF).build();
    public static final ITextValidator DEFAULT_TEXT_VALIDATOR = (current, newText, input) -> input;

    private final List<TextSegment> segments = new LinkedList<>();
    private final TreeMap<Integer, ETextAlignment> paragraphAlignments = new TreeMap<>();
    private final List<InteractiveElement> interactiveElements = new LinkedList<>();

    private boolean multiline = false;
    private int maxCharacters = 1000000;
    private Pattern filterRegex = Pattern.compile("(?s).*");
    private ITextValidator textValidator = DEFAULT_TEXT_VALIDATOR;

    // Events
    private Runnable onTextChanged;
 
    // --- Getters and Setters ---

    public RichTextComponent() {
        paragraphAlignments.put(0, DEFAULT_ALIGNMENT);
    }

    public void setTextChangedCallback(Runnable callback) {
        this.onTextChanged = callback;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
    }

    public Pattern getFilterRegex() {
        return filterRegex;
    }

    public void setFilterRegex(String regex) throws PatternSyntaxException {
        this.filterRegex = Pattern.compile(regex);
    }

    public ITextValidator getTextValidator() {
        return textValidator;
    }

    public void setTextValidator(ITextValidator textValidator) {
        this.textValidator = textValidator;
    }


    // --- Converters ---

    public Component toComponent() {
        MutableComponent component = TextUtils.empty();
        for (TextSegment segment : getSegmentsRaw()) {
            component.append(segment.toComponent());
        }
        return component;
    }

    // --- ---

    int toCharIndex(CharSequence cs, int codePointIndex) {
        if (cs == null) throw new NullPointerException("CharSequence cannot be null");
        String str = (cs instanceof String) ? (String) cs : cs.toString();
        if (codePointIndex < 0) {
            throw new StringIndexOutOfBoundsException("Code point index cannot be negative: " + codePointIndex);
        }
        int cpCount = str.codePointCount(0, str.length());
        if (codePointIndex > cpCount) {
            throw new StringIndexOutOfBoundsException("Code point index: " + codePointIndex + ", Total code points: " + cpCount);
        }
        if (codePointIndex == cpCount) {
            return str.length();
        }
        return str.offsetByCodePoints(0, codePointIndex);
    }

    private int toCharIndex(StringBuilder sb, int codePointIndex) {
        if (sb == null) throw new NullPointerException("StringBuilder cannot be null");
        return toCharIndex(sb.toString(), codePointIndex);
    }

    private void checkIndex(int index) {
        int len = length();
        if (index < 0 || index > len) {
            throw new IndexOutOfBoundsException("Index: " + index + ", CodePointLength: " + len);
        }
    }

    private void checkRange(int start, int end) {
        checkIndex(start);
        checkIndex(end);
        if (start > end) {
            throw new IndexOutOfBoundsException("Start code point index (" + start + ") > end code point index (" + end + ")");
        }
    }

    public TextStyle getStyleAt(int index) {
        checkIndex(index);
        if (segments.isEmpty()) {
            return TextStyle.EMPTY;
        }
        if (index == length()) {
            TextSegment lastSegment = segments.get(segments.size() - 1);
            if (lastSegment.isEmpty() && segments.size() > 1) {
                return segments.get(segments.size() - 2).style();
            }
            return lastSegment.style();
        }

        int currentCodePointPos = 0;
        for (TextSegment segment : segments) {
            int segCodePointLen = segment.length();
            if (index >= currentCodePointPos && index < currentCodePointPos + segCodePointLen) {
                return segment.style();
            }
            currentCodePointPos += segCodePointLen;
        }
        return TextStyle.EMPTY;
    }

    public record TextSegmentResult(int segmentIndex, int textStartIndex, TextSegment segment) {
        public boolean available() {
            return segment() != null;
        }
    }

    public TextSegmentResult getSegmentAt(int index) {
        checkIndex(index);
        List<TextSegment> segments = getSegmentsRaw();
        if (segments == null || segments.isEmpty()) {
            return new TextSegmentResult(-1, -1, null);
        }

        if (index >= length() - 1) {
            TextSegment segment = segments.get(segments.size() - 1);
            return new TextSegmentResult(segments.size() - 1, length() - segment.length(), segment);
        }

        int segIdx = 0;
        int txtIdx = 0;
        for (TextSegment segment : segments) {
            if (txtIdx + segment.length() > index) {
                return new TextSegmentResult(segIdx, txtIdx, segment);
            }
            txtIdx += segment.length();
            segIdx++;
        }
        return new TextSegmentResult(-1, -1, null);
    }

    public String filterText(String rawText) {
        String filteredText = rawText;
        if (!multiline) {
            filteredText = filteredText.replace("\n", "").replace("\r", "");
        }
        int avLength = maxCharacters - length();
        if (avLength <= 0) {
            return "";
        }
        filteredText = filteredText.substring(0, Math.min(avLength, filteredText.length()));
        return filteredText;
    }

    private String validateText(String current, String future, String input) {
        if (!filterRegex.matcher(future).matches()) {
            return null;
        }
        return textValidator.validate(current, future, input);
    }

    public int set(String text) {
        return set(text, null, null);
    }

    public int set(String text, TextStyle style) {
        return set(text, style, null);
    }

    public int set(String text, TextStyle style, ETextAlignment alignment) {
        clear();
        return insert(length(), text, style, alignment);
    }

    public int append(String text) {
        return append(text, null, null);
    }

    public int append(String text, TextStyle style) {
        return append(text, style, null);
    }

    public int append(String text, TextStyle style, ETextAlignment alignment) {
        return insert(length(), text, style, alignment);
    }

    public int insert(int index, String text) {
        return insert(index, text, null, null);
    }

    public int insert(int index, String text, TextStyle style) {
        return insert(index, text, style, null);
    }

    public int insert(int index, String text, TextStyle style, ETextAlignment alignment) {
        checkIndex(index);
        if (text == null || text.isEmpty()) return 0;

        String filteredText = filterText(text);
        if (filteredText.isEmpty()) return 0;

        TextStyle actualStyle = style;
        if (actualStyle == null) {
            actualStyle = getStyleAt(index);
        } else {
            actualStyle = style.copy();
        }

        String plainTextBeforeInsert = getPlainText();

        if (alignment != null) {
            ETextAlignment currentAlignment = getParagraphAlignment(index);
            if (alignment != currentAlignment) {
                if (index > 0) {
                    int prevCharIdx = toCharIndex(plainTextBeforeInsert, index - 1);
                    if (prevCharIdx < plainTextBeforeInsert.length() && plainTextBeforeInsert.codePointAt(prevCharIdx) != '\n') {
                        filteredText = "\n" + filteredText;
                    }
                }
            }
        }

        return insertInternal(index, filteredText, actualStyle, alignment, true);
    }
    private int insertInternal(int codePointInsertIndex, String textToInsert, TextStyle style, ETextAlignment alignment, boolean performUpdate) {

        StringBuilder futureText = new StringBuilder(getPlainText());
        futureText.insert(codePointInsertIndex, textToInsert);
        textToInsert = validateText(getPlainText(), futureText.toString(), textToInsert);
        if (textToInsert == null) {
            return 0;
        }

        if (segments.isEmpty()) {
            segments.add(new TextSegment(new StringBuilder(textToInsert), style));
            updateAlignmentsOnInsert(codePointInsertIndex, textToInsert);
            if (alignment != null) {
                setParagraphAlignment(codePointInsertIndex, alignment, false);
            }
        } else {
            int currentCodePointPos = 0;
            ListIterator<TextSegment> iterator = segments.listIterator();
            boolean inserted = false;
            while (iterator.hasNext()) {
                TextSegment currentSegment = iterator.next();
                int segCodePointLen = currentSegment.length();

                if (codePointInsertIndex >= currentCodePointPos && codePointInsertIndex <= currentCodePointPos + segCodePointLen) {
                    int codePointOffsetInSegment = codePointInsertIndex - currentCodePointPos;
                    int charOffsetInSegment = toCharIndex(currentSegment.stringBuilder(), codePointOffsetInSegment);

                    if (codePointOffsetInSegment == 0) {
                        if (iterator.previousIndex() > 0) {
                            TextSegment prevOfCurrent = segments.get(iterator.previousIndex() -1);
                            if (prevOfCurrent.style().equals(style)) {
                                prevOfCurrent.stringBuilder().append(textToInsert);
                                inserted = true;
                                break;
                            }
                        }
                        iterator.previous();
                        iterator.add(new TextSegment(new StringBuilder(textToInsert), style));
                    } else if (codePointOffsetInSegment == segCodePointLen) {
                        if (currentSegment.style().equals(style)) {
                            currentSegment.stringBuilder().append(textToInsert);
                        } else {
                            iterator.add(new TextSegment(new StringBuilder(textToInsert), style));
                        }
                    } else {
                        String beforeText = currentSegment.stringBuilder().substring(0, charOffsetInSegment);
                        String afterText = currentSegment.stringBuilder().substring(charOffsetInSegment);

                        currentSegment.stringBuilder().setLength(0);
                        currentSegment.stringBuilder().append(beforeText);

                        iterator.add(new TextSegment(new StringBuilder(textToInsert), style));
                        iterator.add(new TextSegment(new StringBuilder(afterText), currentSegment.style().copy()));
                    }
                    inserted = true;
                    break;
                }
                currentCodePointPos += segCodePointLen;
            }
            if (!inserted) {
                if (codePointInsertIndex == length() && !segments.isEmpty()) {
                    TextSegment lastSegment = segments.get(segments.size() - 1);
                    if (lastSegment.style().equals(style) && !lastSegment.isEmpty()) {
                        lastSegment.stringBuilder().append(textToInsert);
                    } else {
                        segments.add(new TextSegment(new StringBuilder(textToInsert), style));
                    }
                } else {
                    segments.add(new TextSegment(new StringBuilder(textToInsert), style));
                }
            }
            updateAlignmentsOnInsert(codePointInsertIndex, textToInsert);
            if (alignment != null) {
                setParagraphAlignment(codePointInsertIndex, alignment, false);
            }
        }

        int insertedCpLength = getFullCodepointCount(textToInsert);//textToInsert.codePointCount(0, textToInsert.length());
        if (insertedCpLength > 0) {
            for (InteractiveElement el : interactiveElements) {
                if (el.start >= codePointInsertIndex) {
                    el.start += insertedCpLength;
                }
                if (el.end >= codePointInsertIndex && el.start < codePointInsertIndex) {
                    el.end += insertedCpLength;
                } else if (el.end >= codePointInsertIndex) {
                    el.end += insertedCpLength;
                }
            }
        }
        if (performUpdate) mergeAndUpdate();
        return textToInsert.codePointCount(0, textToInsert.length());
    }

    private int getFullCodepointCount(String text) {
        int cpc = 0;
        for (char c : text.toCharArray()) {
            if (!Character.isLowSurrogate(c)) cpc++;
        }
        return cpc;
    }

    public void clear() {
        remove(0, length());
    }

    public RichTextComponent remove(int startCp, int endCp) {
        checkRange(startCp, endCp);
        if (startCp == endCp) {
            return this;
        }

        String plainTextBeforeRemove = getPlainText();
        String removedTextContent = "";
        if (startCp < endCp && endCp <= plainTextBeforeRemove.codePointCount(0, plainTextBeforeRemove.length())) {
            int charStartForRemoval = toCharIndex(plainTextBeforeRemove, startCp);
            int charEndForRemoval = toCharIndex(plainTextBeforeRemove, endCp);
            removedTextContent = plainTextBeforeRemove.substring(charStartForRemoval, charEndForRemoval);
        }


        List<TextSegment> newSegments = new LinkedList<>();
        int currentCodePointIndex = 0;

        for (TextSegment segment : this.segments) {
            int segmentStartCodePoint = currentCodePointIndex;
            int segmentEndCodePoint = currentCodePointIndex + segment.length();

            if (segmentEndCodePoint <= startCp || segmentStartCodePoint >= endCp) {
                newSegments.add(segment);
            } else {
                if (segmentStartCodePoint < startCp) {
                    int charCountBefore = toCharIndex(segment.stringBuilder(), startCp - segmentStartCodePoint);
                    newSegments.add(new TextSegment(new StringBuilder(segment.stringBuilder().substring(0, charCountBefore)), segment.style().copy()));
                }

                if (segmentEndCodePoint > endCp) {
                    int codePointsInSegmentToRemoveUpToEndCp = endCp - segmentStartCodePoint;
                    int charCountAfter = toCharIndex(segment.stringBuilder(), codePointsInSegmentToRemoveUpToEndCp);
                    newSegments.add(new TextSegment(new StringBuilder(segment.stringBuilder().substring(charCountAfter)), segment.style().copy()));
                }
            }
            currentCodePointIndex = segmentEndCodePoint;
        }

        this.segments.clear();
        this.segments.addAll(newSegments);
        updateAlignmentsOnRemove(startCp, endCp, removedTextContent);

        int lengthRemoved = endCp - startCp;
        if (lengthRemoved > 0) {
            ListIterator<InteractiveElement> iter = interactiveElements.listIterator();
            while (iter.hasNext()) {
                InteractiveElement el = iter.next();

                if (el.end <= startCp) {
                } else if (el.start >= endCp) {
                    el.start -= lengthRemoved;
                    el.end -= lengthRemoved;
                } else {
                    int oldStart = el.start;
                    int oldEnd = el.end;

                    if (el.start >= startCp && el.start < endCp) {
                        el.start = startCp;
                    }

                    if (el.end > startCp && el.end <= endCp) {
                        el.end = startCp;
                    }

                    if (oldStart < startCp && oldEnd > startCp) {
                        if (oldEnd > endCp) {
                            el.end -= lengthRemoved;
                        } else {
                            el.end = startCp;
                        }
                    }
                    else if (oldStart >= startCp && oldStart < endCp && oldEnd > endCp) {
                        el.start = startCp;
                        el.end -= lengthRemoved;
                    }

                    if (el.start >= el.end) {
                        iter.remove();
                    }
                }
            }
        }

        mergeAndUpdate();
        return this;
    }

    public RichTextComponent replace(int start, int end, String text) {
        return replace(start, end, text, null, null);
    }

    public RichTextComponent replace(int start, int end, String text, TextStyle style) {
        return replace(start, end, text, style, null);
    }

    public RichTextComponent replace(int start, int end, String text, TextStyle style, ETextAlignment alignment) {
        checkRange(start, end);
        String replacementText = (text == null) ? "" : text;
        if (!multiline) {
            replacementText = replacementText.replace("\n", "").replace("\r", "");
        }

        TextStyle actualStyle = style;
        if (actualStyle == null) {
            actualStyle = getStyleAt(start);
        } else {
            actualStyle = style.copy();
        }

        remove(start, end);
        insertInternal(start, replacementText, actualStyle, alignment, false);

        if (alignment != null && !replacementText.isEmpty()) {
            setParagraphAlignment(start, alignment, false);
        }

        mergeAndUpdate();
        return this;
    }


    public RichTextComponent replace(String searchText, String replacementText) {
        return replace(searchText, replacementText, null, null);
    }

    public RichTextComponent replace(String searchText, String replacementText, TextStyle style) {
        return replace(searchText, replacementText, style, null);
    }

    public RichTextComponent replace(String searchText, String replacementText, TextStyle style, ETextAlignment alignment) {
        if (searchText == null || searchText.isEmpty()) {
            return this;
        }
        String currentPlainText = getPlainText();
        int currentSearchCharIndex = 0;
        int searchTextCharLength = searchText.length();
        int replacementTextCodePointLength = (replacementText != null) ? replacementText.codePointCount(0, replacementText.length()) : 0;


        while(true) {
            int foundCharIndex = currentPlainText.indexOf(searchText, currentSearchCharIndex);
            if (foundCharIndex == -1) break;

            int foundCodePointIndex = currentPlainText.codePointCount(0, foundCharIndex);
            int searchTextCodePointLength = searchText.codePointCount(0, searchTextCharLength);

            replace(foundCodePointIndex, foundCodePointIndex + searchTextCodePointLength, replacementText, style, alignment);

            currentPlainText = getPlainText();
            if (replacementTextCodePointLength == 0 && searchTextCodePointLength == 0) {
                currentSearchCharIndex = foundCharIndex + 1;
                if (currentSearchCharIndex > currentPlainText.length()) break;
            } else {
                currentSearchCharIndex = toCharIndex(currentPlainText, foundCodePointIndex + replacementTextCodePointLength);
            }
            if (currentSearchCharIndex > currentPlainText.length()) break;
        }
        return this;
    }

    private void updateAlignmentsOnInsert(int index, String insertedText) {
        if (insertedText == null || insertedText.isEmpty()) return;

        int cpl = getFullCodepointCount(insertedText);//insertedText.codePointCount(0, insertedText.length());
        TreeMap<Integer, ETextAlignment> newAlignments = new TreeMap<>();

        for (Map.Entry<Integer, ETextAlignment> entry : paragraphAlignments.entrySet()) {
            int oldKey = entry.getKey();
            ETextAlignment value = entry.getValue();
            if (oldKey < index) {
                newAlignments.put(oldKey, value);
            } else {
                newAlignments.put(oldKey + cpl, value);
            }
        }
        paragraphAlignments.clear();
        paragraphAlignments.putAll(newAlignments);

        ETextAlignment alignmentForNewParagraphs = getParagraphAlignment(index > 0 ? index -1 : 0);

        int charIdx = 0;
        for (int cpIdx = 0; cpIdx < cpl; ++cpIdx) {
            int codePoint = insertedText.codePointAt(charIdx);
            if (codePoint == '\n') {
                int newParagraphStartIndex = index + cpIdx + 1;
                paragraphAlignments.put(newParagraphStartIndex, alignmentForNewParagraphs);
            }
            charIdx += Character.charCount(codePoint);
        }
        if (!paragraphAlignments.containsKey(0)) {
            paragraphAlignments.put(0, DEFAULT_ALIGNMENT);
        }
    }

    private void updateAlignmentsOnRemove(int start, int end, String removedText) {
        if (start == end) return;
        int removedCodePointLength = end - start;

        TreeMap<Integer, ETextAlignment> newAlignments = new TreeMap<>();
        ETextAlignment alignmentBeforeRemovalStart = getParagraphAlignment(start > 0 ? start -1 : 0);

        for (Map.Entry<Integer, ETextAlignment> entry : paragraphAlignments.entrySet()) {
            int oldKey = entry.getKey();
            ETextAlignment value = entry.getValue();

            if (oldKey < start) {
                newAlignments.put(oldKey, value);
            } else if (oldKey >= end) {
                newAlignments.put(oldKey - removedCodePointLength, value);
            }
        }
        paragraphAlignments.clear();
        paragraphAlignments.putAll(newAlignments);

        boolean newlineRemovedDuringOperation = false;
        if (removedText != null && !removedText.isEmpty()) {
            int charIter = 0;
            int removedTextCpLength = removedText.codePointCount(0, removedText.length());
            for(int cpIter = 0; cpIter < removedTextCpLength; cpIter++){
                if(removedText.codePointAt(charIter) == '\n') {
                    newlineRemovedDuringOperation = true;
                    break;
                }
                charIter += Character.charCount(removedText.codePointAt(charIter));
            }
        }


        if(newlineRemovedDuringOperation) {
            String currentPlainText = getPlainText();
            int currentPlainTextCpLength = currentPlainText.codePointCount(0, currentPlainText.length());

            int paragraphStartIndexForCursor = 0;
            if (start > 0 && start <= currentPlainTextCpLength) {
                String textBeforeStartCp = currentPlainText.substring(0, toCharIndex(currentPlainText, start));
                int lastNewlineCharIndex = textBeforeStartCp.lastIndexOf('\n');
                if (lastNewlineCharIndex != -1) {
                    paragraphStartIndexForCursor = currentPlainText.codePointCount(0, lastNewlineCharIndex + 1);
                }
            }
            if (!paragraphAlignments.containsKey(paragraphStartIndexForCursor) || paragraphAlignments.get(paragraphStartIndexForCursor) != alignmentBeforeRemovalStart) {
                if (paragraphStartIndexForCursor <= length()) {
                    paragraphAlignments.put(paragraphStartIndexForCursor, alignmentBeforeRemovalStart);
                }
            }
        }


        if (!paragraphAlignments.containsKey(0) && length() > 0) {
            paragraphAlignments.put(0, DEFAULT_ALIGNMENT);
        } else if (length() == 0) {
            paragraphAlignments.clear();
            paragraphAlignments.put(0, DEFAULT_ALIGNMENT);
        }
    }

    public RichTextComponent setFormat(int start, int end, TextStyle style) {
        checkRange(start, end);
        if (start == end || style == null) {
            return this;
        }

        List<TextSegment> newSegments = new LinkedList<>();
        int currentCodePointIndex = 0;
        TextStyle newStyle = style.copy();

        for (TextSegment segment : this.segments) {
            int segmentStartCp = currentCodePointIndex;
            int segmentEndCp = currentCodePointIndex + segment.length();

            if (segmentEndCp <= start || segmentStartCp >= end) {
                newSegments.add(segment);
            } else {
                if (segmentStartCp < start) {
                    int charEndOffset = toCharIndex(segment.stringBuilder(), start - segmentStartCp);
                    newSegments.add(new TextSegment(new StringBuilder(segment.stringBuilder().substring(0, charEndOffset)), segment.style().copy()));
                }

                int formatStartInSegCp = Math.max(0, start - segmentStartCp);
                int formatEndInSegCp = Math.min(segment.length(), end - segmentStartCp);

                if (formatStartInSegCp < formatEndInSegCp) {
                    int charFormatStart = toCharIndex(segment.stringBuilder(), formatStartInSegCp);
                    int charFormatEnd = toCharIndex(segment.stringBuilder(), formatEndInSegCp);
                    newSegments.add(new TextSegment(new StringBuilder(segment.stringBuilder().substring(charFormatStart, charFormatEnd)), newStyle));
                }

                if (segmentEndCp > end) {
                    int charStartOffset = toCharIndex(segment.stringBuilder(), end - segmentStartCp);
                    newSegments.add(new TextSegment(new StringBuilder(segment.stringBuilder().substring(charStartOffset)), segment.style().copy()));
                }
            }
            currentCodePointIndex = segmentEndCp;
        }

        this.segments.clear();
        this.segments.addAll(newSegments);
        mergeAndUpdate();
        return this;
    }


    public RichTextComponent clearFormat(int start, int end) {
        return setFormat(start, end, TextStyle.EMPTY);
    }

    public RichTextComponent clearFormatAt(int codePointIndex) {
        checkIndex(codePointIndex);
        if (codePointIndex == length() && length() > 0) {
            if (codePointIndex > 0) {
                return setFormat(codePointIndex -1, codePointIndex, TextStyle.EMPTY);
            } else {
                return this;
            }
        }
        return setFormat(codePointIndex, codePointIndex + 1, TextStyle.EMPTY);
    }

    public String getPlainText() {
        return plainTextCache.get();
    }

    private boolean mergeNeighbors(int fromSegmentIndex, int toSegmentIndex) {
        segments.removeIf(TextSegment::isEmpty);
        if (segments.size() < 2) {
            return false;
        }

        boolean changed = false;

        int from = Math.max(0, fromSegmentIndex);
        int to = Math.min(segments.size() - 1, toSegmentIndex);

        List<TextSegment> merged = new LinkedList<>();
        if (from <= 0) {
            merged.add(segments.get(0).copy());
        }

        TextSegment previous;
        TextSegment current;
        for (int i = from + 1; i <= to; i++) {
            previous = merged.get(merged.size() - 1);
            current = segments.get(i);
            if (previous.style().equals(current.style())) {
                previous.stringBuilder().append(current.stringBuilder());
                changed = true;
            } else {
                merged.add(current.copy());
            }
        }
        segments.clear();
        segments.addAll(merged);
        return changed;
    }
   


    private void update() {
        textWidthCache.clear();
        textLengthCache.clear();
        plainTextCache.clear();
    }

    private void mergeAndUpdate() {
        mergeAndUpdate(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private void mergeAndUpdate(int firstSegmentIndex, int secondSegmentIndex) {
        mergeNeighbors(firstSegmentIndex, secondSegmentIndex);
        update();
        onTextChanged();
    }

    public ImmutableRichTextComponent subComponent(LineMarker line) {
        if (line == null || line.isEmpty()) {
            return ImmutableRichTextComponent.EMPTY;
        }
        return subComponent(line.startIndex(), line.endIndex());
    }

    public ImmutableRichTextComponent subComponent(int start, int end) {
        checkRange(start, end);
        if (start == end) {
            return ImmutableRichTextComponent.EMPTY;
        }

        List<TextSegment.ImmutableTextSegment> resultSegments = new LinkedList<>();
        int currentCodePointIndex = 0;
        for (TextSegment segment : getSegmentsRaw()) {
            int segmentStartCp = currentCodePointIndex;
            int segmentEndCp = currentCodePointIndex + segment.length();

            if (segmentEndCp > start && segmentStartCp < end) {
                int subStartInSegmentCp = Math.max(0, start - segmentStartCp);
                int subEndInSegmentCp = Math.min(segment.length(), end - segmentStartCp);
                if (subStartInSegmentCp < subEndInSegmentCp) {
                    resultSegments.add(segment.copyImmutable(subStartInSegmentCp, subEndInSegmentCp));
                }
            }
            currentCodePointIndex = segmentEndCp;
            if (currentCodePointIndex >= end) {
                break;
            }
        }
        return new ImmutableRichTextComponent(List.copyOf(resultSegments));
    }


    public List<TextSegment> getSegmentsRaw() {
        return segments;
    }

    public int length() {
        return textLengthCache.get();
    }

    public int charLength() {
        int i = 0;
        for (TextSegment segment : getSegmentsRaw()) {
            i += segment.charLength();
        }
        return i;
    }

    public float width() {
        return width(0, length());
    }

    public float width(int endIndex) {
        return width(0, endIndex);
    }

    public float width(int startIndex, int endIndex) {
        checkRange(startIndex, endIndex);
        if (endIndex <= startIndex) {
            return 0;
        }
        if (startIndex == 0 && endIndex == length()) {
            return textWidthCache.get();
        }
        return calcWidthOfSection(startIndex, endIndex);
    }

    private float calcWidthOfSection(int startIndex, int endIndex) {
        MutableFloat currentWidth = new MutableFloat();
        MutableInt currentIdx = new MutableInt(0);

        for (TextSegment segment : getSegmentsRaw()) {
            if (currentIdx.intValue() + segment.length() <= startIndex) {
                currentIdx.add(segment.length());
                continue;
            } else if (currentIdx.intValue() >= endIndex) {
                break;
            }

            int segmentLength = segment.length();
            int effectiveStartInSegmentCp = Math.max(0, startIndex - currentIdx.intValue());
            int effectiveEndInSegmentCp = Math.min(segmentLength, endIndex - currentIdx.intValue());
            int numCodePointsToIterate = effectiveEndInSegmentCp - effectiveStartInSegmentCp;

            if (numCodePointsToIterate > 0) {
                final int baseCpIndexForThisSegment = currentIdx.intValue();
                if (!TextSegment.iterate(segment, effectiveStartInSegmentCp, (idxInIterationCp, style, codePoint) -> {
                    if (baseCpIndexForThisSegment + effectiveStartInSegmentCp + idxInIterationCp < endIndex) {
                        currentWidth.add(widthProvider.getWidth(style, codePoint));
                        return true;
                    }
                    return false;
                }, numCodePointsToIterate)) {
                    break;
                }
            }
            currentIdx.add(segmentLength);
        }
        return currentWidth.floatValue();
    }

    public int indexByWidth(float maxWidth) {
        return calcIndexByWidth(maxWidth, 0, length(), false);
    }

    public int indexByWidth(float maxWidth, int skip) {
        return calcIndexByWidth(maxWidth, skip, length(), false);
    }

    public int indexByWidth(float maxWidth, LineMarker line, boolean pickClosest) {
        if (line == null) return calcIndexByWidth(maxWidth, 0, length(), pickClosest);
        return calcIndexByWidth(maxWidth, line.startIndex(), line.endIndex(), pickClosest);
    }

    public int indexByWidth(float maxWidth, int skip, int max, boolean pickClosest) {
        return calcIndexByWidth(maxWidth, skip, max, pickClosest);
    }

    private int calcIndexByWidth(float targetWidth, int skip, int max, boolean pickClosest) {
        MutableFloat currentWidth = new MutableFloat(0.0F);
        MutableInt targetIndex = new MutableInt(0);
        int globalIndex = 0;

        for (TextSegment segment : getSegmentsRaw()) {
            if (globalIndex + segment.length() < skip) {
                globalIndex += segment.length();
                continue;
            } else if (globalIndex > max) break;

            int segmentLen = segment.length();
            int globalIdx = globalIndex;

            int localSkipInSegmentCp = Math.max(0, skip - globalIdx);
            int localEndInSegmentCp = Math.min(segmentLen, max - globalIdx);
            int numCodePointsToIterateInSegment = Math.max(0, localEndInSegmentCp - localSkipInSegmentCp);

            if (numCodePointsToIterateInSegment > 0) {
                boolean cancel = !TextSegment.iterate(segment, localSkipInSegmentCp, (iterCpIdx, style, codePoint) -> {
                    int currentIteratedGlobalCpIndex = globalIdx + localSkipInSegmentCp + iterCpIdx;

                    if (currentIteratedGlobalCpIndex >= max) return false;
                    if (currentIteratedGlobalCpIndex < skip) return true;

                    float charWidth = widthProvider.getWidth(style, codePoint);
                    if (currentWidth.floatValue() + charWidth <= targetWidth) {
                        currentWidth.add(charWidth);
                        targetIndex.increment();
                    } else {
                        if (pickClosest) {
                            if (Math.abs(targetWidth - (currentWidth.floatValue() + charWidth)) < Math.abs(targetWidth - currentWidth.floatValue())) {
                                currentWidth.add(charWidth);
                                targetIndex.increment();
                            }
                        }
                        return false;
                    }
                    return true;
                }, numCodePointsToIterateInSegment);

                if (cancel) {
                    break;
                }
            }
            globalIndex += segmentLen;
        }
        return skip + targetIndex.intValue();
    }


    public TreeMap<Integer, LineMarker> splitLines(int maxWidth) {
        TreeMap<Integer, LineMarker> calculatedMarkers = Maps.newTreeMap();
        if (segments.isEmpty()) {
            calculatedMarkers.put(0, new LineMarker(0, 0, 0, 1f, Minecraft.getInstance().font.lineHeight, 0, getParagraphAlignment(0)));
            return calculatedMarkers;
        }

        final String fullPlainText = getPlainText();
        final int[] fullPlainTextCodePoints;
        final int textCodePointLength;

        if (!fullPlainText.isEmpty()) {
            fullPlainTextCodePoints = fullPlainText.codePoints().toArray();
            textCodePointLength = fullPlainTextCodePoints.length;
        } else {
            fullPlainTextCodePoints = new int[0];
            textCodePointLength = 0;
        }

        LineSplitter splitter;
        MutableInt lastCodePointIndex = new MutableInt(0);
        MutableFloat currentY = new MutableFloat(0);

        splitter = new LineSplitter(maxWidth, 0, widthProvider, (lineBreak) -> {
            int breakCpIndex = lineBreak.breakIndex();
            int effectiveLineEndCodePointIndex = breakCpIndex;
            int nextLineStartCodePointIndex = breakCpIndex;

            if (breakCpIndex < textCodePointLength) {
                int codePointAtBreak = fullPlainTextCodePoints[breakCpIndex];
                if (codePointAtBreak == '\n') {
                    nextLineStartCodePointIndex = breakCpIndex + 1;
                } else if (codePointAtBreak == ' ' && maxWidth != Integer.MAX_VALUE) {
                    nextLineStartCodePointIndex = breakCpIndex + 1;
                }
            }

            float actualRenderedWidth = lineBreak.lineWidth();
            ETextAlignment lineAlignment = getParagraphAlignment(lastCodePointIndex.intValue());

            calculatedMarkers.put(lastCodePointIndex.intValue(),
                    new LineMarker(lastCodePointIndex.intValue(),
                            effectiveLineEndCodePointIndex,
                            actualRenderedWidth,
                            lineBreak.scale(),
                            lineBreak.lineHeight(),
                            currentY.floatValue(),
                            lineAlignment));

            currentY.add(lineBreak.lineHeight());
            lastCodePointIndex.setValue(nextLineStartCodePointIndex);
        });

        MutableInt segmentBaseCodePointIndex = new MutableInt(0);
        for (TextSegment segment : getSegmentsRaw()) {
            splitter.offset = segmentBaseCodePointIndex.intValue();
            TextSegment.iterate(segment, 0, splitter::accept, segment.length());
            segmentBaseCodePointIndex.add(segment.length());
        }

        int currentTotalCpLength = this.length();

        if (lastCodePointIndex.intValue() < currentTotalCpLength || calculatedMarkers.isEmpty()) {
            float finalLineWidth = splitter.widthSinceLastBreak();
            ETextAlignment lineAlignment = getParagraphAlignment(lastCodePointIndex.intValue());
            calculatedMarkers.put(lastCodePointIndex.intValue(), new LineMarker(lastCodePointIndex.intValue(), currentTotalCpLength, finalLineWidth, splitter.highestScale(), splitter.lineHeight(), currentY.floatValue(), lineAlignment));
        } else if (currentTotalCpLength > 0 && lastCodePointIndex.intValue() == currentTotalCpLength) {
            if (textCodePointLength > 0 &&
                    fullPlainTextCodePoints[textCodePointLength - 1] == '\n' &&
                    !calculatedMarkers.containsKey(lastCodePointIndex.intValue())) {
                ETextAlignment lineAlignment = getParagraphAlignment(lastCodePointIndex.intValue());
                float defaultLineHeight = Minecraft.getInstance().font.lineHeight;
                calculatedMarkers.put(lastCodePointIndex.intValue(), new LineMarker(lastCodePointIndex.intValue(), currentTotalCpLength, 0, 1f, defaultLineHeight, currentY.floatValue(), lineAlignment));
            }
        }
        return calculatedMarkers;
    }

    public void setParagraphAlignment(int charIndex, ETextAlignment alignment) {
        setParagraphAlignment(charIndex, alignment, true);
    }

    private void setParagraphAlignment(int charIndex, ETextAlignment alignment, boolean performUpdate) {
        checkIndex(charIndex);

        String text = getPlainText();
        int textCodePointLength = text.codePointCount(0, text.length());

        int effectiveCodePointIndex = charIndex;
        if (charIndex == textCodePointLength && textCodePointLength > 0) {
            int lastCharIdx = toCharIndex(text, textCodePointLength -1);
            if (lastCharIdx < text.length() && text.codePointAt(lastCharIdx) != '\n') {
                effectiveCodePointIndex = textCodePointLength -1;
            }
        }

        int paraStartIndexCodePoints = 0;
        if (effectiveCodePointIndex > 0) {
            int charIndexForSearch = toCharIndex(text, Math.min(effectiveCodePointIndex + 1, textCodePointLength));
            String subToEffectiveChar = text.substring(0, charIndexForSearch);
            int lastNewlineCharIndex = subToEffectiveChar.lastIndexOf('\n', Math.max(0, toCharIndex(text, effectiveCodePointIndex) -1) );


            if (lastNewlineCharIndex != -1) {
                paraStartIndexCodePoints = text.codePointCount(0, lastNewlineCharIndex + 1);
            }
        }

        if (effectiveCodePointIndex < textCodePointLength) {
            int charIdxAtEffective = toCharIndex(text, effectiveCodePointIndex);
            if (charIdxAtEffective < text.length() && text.codePointAt(charIdxAtEffective) == '\n') {
                if (effectiveCodePointIndex + 1 <= textCodePointLength) {
                    paraStartIndexCodePoints = effectiveCodePointIndex + 1;
                }
            }
        }

        if (paraStartIndexCodePoints <= textCodePointLength) {
            paragraphAlignments.put(paraStartIndexCodePoints, alignment);
        }

        if (performUpdate) {
            update();
        }
    }

    public ETextAlignment getParagraphAlignment(int charIndex) {
        if (charIndex < 0) charIndex = 0;
        int currentTotalCodePoints = length();
        if (charIndex > currentTotalCodePoints) charIndex = currentTotalCodePoints;

        if (paragraphAlignments.isEmpty()) return DEFAULT_ALIGNMENT;

        Map.Entry<Integer, ETextAlignment> entry = paragraphAlignments.floorEntry(charIndex);

        if (entry == null) {
            return paragraphAlignments.getOrDefault(0, DEFAULT_ALIGNMENT);
        }

        return entry.getValue();
    }

    private void addInteractiveElementInternal(InteractiveElement newElement) {
        if (newElement.start >= newElement.end) return;

        List<InteractiveElement> resultElements = new LinkedList<>();
        for (InteractiveElement existingElement : this.interactiveElements) {
            if (existingElement.end <= newElement.start || existingElement.start >= newElement.end) {
                resultElements.add(existingElement);
            } else {
                if (existingElement.start < newElement.start) {
                    InteractiveElement element = new InteractiveElement(existingElement.start, newElement.start);
                    element.copyFrom(existingElement);
                    resultElements.add(element);
                }
                if (existingElement.end > newElement.end) {                    
                    InteractiveElement element = new InteractiveElement(newElement.end, existingElement.end);
                    element.copyFrom(existingElement);
                    resultElements.add(element);
                }
            }
        }
        resultElements.add(newElement);
        resultElements.sort(Comparator.comparingInt(e -> e.start));

        this.interactiveElements.clear();
        this.interactiveElements.addAll(resultElements);
        update();
    }

    public Optional<InteractiveElement> createInteractiveElement(int start, int end) {
        checkRange(start, end);
        if (end <= start) return Optional.empty();

        InteractiveElement newElement = new InteractiveElement(start, end);
        addInteractiveElementInternal(newElement);
        return Optional.of(newElement);
    }

    public List<InteractiveElement> getInteractiveElements() {
        return this.interactiveElements;
    }

    public InteractiveElement getInteractiveElementAt(int codePointIndex) {
        checkIndex(codePointIndex);
        if (codePointIndex == length() && length() > 0) return null;

        for (InteractiveElement element : interactiveElements) {
            if (element.contains(codePointIndex)) {
                return element;
            }
        }
        return null;
    }

    public RichTextComponent removeActionAt(int codePointIndex) {
        checkIndex(codePointIndex);
        if (codePointIndex == length() && length() > 0) return this;

        InteractiveElement elementToRemove = null;
        for (InteractiveElement element : interactiveElements) {
            if (element.contains(codePointIndex)) {
                elementToRemove = element;
                break;
            }
        }

        if (elementToRemove != null) {
            interactiveElements.remove(elementToRemove);
            update();
        }
        return this;
    }


    // EVENTS
    public void onTextChanged() {
        DLUtils.doIfNotNull(onTextChanged, Runnable::run);
    }

}
