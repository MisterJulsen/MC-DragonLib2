package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

import java.util.List;

public class EffectBatch {
    private final List<BakedGlyph.Effect> effects;
    private final float x;
    private final float y;
    private final float scale;

    public EffectBatch(float x, float y, float scale) {
        this.effects = Lists.newLinkedList();
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public void addEffectToBatch(BakedGlyph.Effect effect) {
        this.effects.add(effect);
    }

    public List<BakedGlyph.Effect> getEffects() {
        return effects;
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float scale() {
        return scale;
    }
}
