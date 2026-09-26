package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class HeightIndex {

    private final NavigableMap<Integer, Float> deviations = new TreeMap<>();

    public static final float FALLBACK_LINE_HEIGHT = 9.0F;
    private static final float HEIGHT_EPSILON = 0.01F;
    private static final float MIN_LINE_HEIGHT = 1.0F;

    private int lineCount = 1;
    private float defaultHeight = FALLBACK_LINE_HEIGHT;
    private float deviationSum;

    public void reset(int lineCount, float defaultHeight) {
        this.lineCount = Math.max(1, lineCount);
        this.defaultHeight = Math.max(MIN_LINE_HEIGHT, defaultHeight);
        this.deviations.clear();
        this.deviationSum = 0.0F;
    }

    public void setDefaultHeight(float defaultHeight) {
        float next = Math.max(MIN_LINE_HEIGHT, defaultHeight);
        if (next != this.defaultHeight) {
            this.defaultHeight = next;

            this.deviations.clear();
            this.deviationSum = 0.0F;
        }
    }

    public float defaultHeight() {
        return defaultHeight;
    }

    public int lineCount() {
        return lineCount;
    }

    public void setHeight(int line, float height) {
        if (line < 0 || line >= lineCount) {
            return;
        }
        float deviation = height - defaultHeight;
        if (Math.abs(deviation) < HEIGHT_EPSILON) {
            forget(line);
            return;
        }
        Float previous = deviations.put(line, deviation);
        deviationSum += deviation - (previous != null ? previous : 0.0F);
    }

    public void forget(int line) {
        Float previous = deviations.remove(line);
        if (previous != null) {
            deviationSum -= previous;
        }
    }

    public float heightOf(int line) {
        return defaultHeight + deviations.getOrDefault(line, 0.0F);
    }

    public float totalHeight() {
        return lineCount * defaultHeight + deviationSum;
    }

    public float offsetOf(int line) {
        int clamped = Math.max(0, Math.min(line, lineCount));
        float offset = clamped * defaultHeight;
        for (Map.Entry<Integer, Float> entry : deviations.headMap(clamped, false).entrySet()) {
            offset += entry.getValue();
        }
        return offset;
    }

    public int lineAtOffset(float y) {
        if (y <= 0.0F) {
            return 0;
        }

        float accumulated = 0.0F;
        int previousLine = 0;

        for (Map.Entry<Integer, Float> entry : deviations.entrySet()) {
            int line = entry.getKey();
            float offsetAtLine = line * defaultHeight + accumulated;
            if (y < offsetAtLine) {
                return clampLine(previousLine + (int) ((y - (previousLine * defaultHeight + accumulated)) / defaultHeight));
            }
            float lineHeight = defaultHeight + entry.getValue();
            if (y < offsetAtLine + lineHeight) {
                return clampLine(line);
            }
            accumulated += entry.getValue();
            previousLine = line + 1;
        }
        return clampLine(previousLine + (int) ((y - (previousLine * defaultHeight + accumulated)) / defaultHeight));
    }

    public void splice(int atLine, int removed, int inserted) {
        this.lineCount = Math.max(1, lineCount - removed + inserted);

        forget(atLine);
        if (removed == 0 && inserted == 0) {
            return;
        }

        int delta = inserted - removed;

        NavigableMap<Integer, Float> tailView = deviations.tailMap(atLine, false);
        Map<Integer, Float> tail = new LinkedHashMap<>(tailView);
        tailView.clear();
        for (Float deviation : tail.values()) {
            deviationSum -= deviation;
        }

        for (Map.Entry<Integer, Float> entry : tail.entrySet()) {
            int line = entry.getKey();
            if (line <= atLine + removed) {
                continue;
            }
            int shifted = line + delta;
            if (shifted > atLine && shifted < lineCount) {
                deviations.put(shifted, entry.getValue());
                deviationSum += entry.getValue();
            }
        }
    }

    public void setLineCount(int lineCount) {
        this.lineCount = Math.max(1, lineCount);
    }

    private int clampLine(int line) {
        return Math.max(0, Math.min(line, lineCount - 1));
    }
}
