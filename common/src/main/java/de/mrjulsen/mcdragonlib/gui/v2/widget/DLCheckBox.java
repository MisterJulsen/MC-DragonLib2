package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public class DLCheckBox extends DLAbstractToggleButton {

    public static final int BOX_GAP = 3;

    public static class Renderer extends DLAbstractButton.Renderer {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.CHECKBOX);
        private static final int STATE_NORMAL = 0;
        private static final int STATE_CHECKED = 1;
        private static final int STATE_SELECTED = 2;
        private static final int STATE_CHECKED_SELECTED = 3;

        private static final String[] CHECKBOX_SPRITES = new String[] {
                "checkbox",
                "checkbox_checked",
                "checkbox_selected",
                "checkbox_checked_selected"
        };
        private static final String[] RADIOBUTTON_SPRITES = new String[] {
                "radiobutton",
                "radiobutton_checked",
                "radiobutton_selected",
                "radiobutton_checked_selected"
        };

        private int spriteWidth;
        private int spriteHeight;

        public Renderer(DLTextureSheet spriteSheet) {
            super(spriteSheet);
        }

        public int getSpriteWidth() {
            return spriteWidth;
        }

        public int getSpriteHeight() {
            return spriteHeight;
        }

        @Override
        public void render(DLGuiGraphics g, GuiComponent component, double x, double y, double width, double height) {
            if (!(component instanceof DLCheckBox cb)) {
                return;
            }

            String[] spriteSet = cb.radioButtonMode.get() ? RADIOBUTTON_SPRITES : CHECKBOX_SPRITES;
            String sprite;
            if (cb.isEnabled() && cb.isHovered() && cb.checked.get()) {
                sprite = spriteSet[STATE_CHECKED_SELECTED];
            } else if (cb.isEnabled() && cb.isHovered()) {
                sprite = spriteSet[STATE_SELECTED];
            } else if (cb.isEnabled() && cb.checked.get()) {
                sprite = spriteSet[STATE_CHECKED];
            } else {
                sprite = spriteSet[STATE_NORMAL];
            }

            DLTextureSheetData.AbstractSprite as = spriteSheet.getSprite(sprite);
            if (as instanceof DLTextureSheetData.StretchedSprite s) {
                spriteWidth = s.width();
                spriteHeight = s.height();
            }
            double sX;
            double sY;
            switch (cb.iconAlignment.get()) {
                case CENTER -> {
                    sX = x + width / 2 - spriteWidth / 2D;
                    sY = y;
                }
                case RIGHT -> {
                    sX = x + width - spriteWidth;
                    sY = y + height / 2 - spriteHeight / 2D;
                }
                default -> {
                    sX = x;
                    sY = y + height / 2 - spriteHeight / 2D;
                }
            }
            GuiUtils.sprite(g, spriteSheet.getSprite(sprite), sX, sY, spriteWidth, spriteHeight);
        }
    }

    public final Property<Component> text = new Property<>(TextUtils.empty());
    public final BooleanProperty textWrap = new BooleanProperty(true);

    private int cachedLinesW;
    private int cachedLinesH;
    private List<FormattedText> lines;

    public DLCheckBox(int x, int y, int w, int h) {
        super(x, y, w, h);
        iconAlignment.set(ETextAlignment.LEFT);
        textAlignment.set(ETextAlignment.LEFT);
        overrideThemeKey(THEME_KEY_RENDERER, Renderer.STANDARD);
    }

    @Override
    protected DLSprite getIcon() {
        return null;
    }

    @Override
    protected Component getText() {
        return text.get();
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.withTint(getThemeValue(THEME_KEY_BACKGROUND_COLOR), () -> {
            getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
        });
        int spriteWidth = 0;
        int spriteHeight = 0;
        if (getThemeValue(THEME_KEY_RENDERER) instanceof Renderer r) {
            spriteWidth = r.getSpriteWidth();
            spriteHeight = r.getSpriteHeight();
        }

        int lineHeight = TextUtils.font().lineHeight;
        double w = width() - (iconAlignment.get() == ETextAlignment.CENTER ? 0 : BOX_GAP + spriteWidth);
        double h = height() - (iconAlignment.get() == ETextAlignment.CENTER ? BOX_GAP + spriteHeight : 0);
        if (lines == null || (int)w != cachedLinesW || (int)h != cachedLinesH) {
            lines = textWrap.get() ? TextUtils.splitText(TextUtils.font(), List.of(getText()), (int)w, (int)h, lineHeight) : List.of(TextUtils.truncateWithEllipsis(TextUtils.font(), getText(), (int)w));
            cachedLinesW = (int)w;
            cachedLinesH = (int)h;
        }
        double x = iconAlignment.get() == ETextAlignment.LEFT ? BOX_GAP + spriteWidth : 0;
        double y = iconAlignment.get() == ETextAlignment.CENTER ? BOX_GAP + spriteHeight : (h - lineHeight * lines.size()) / 2;
        Rectangle textRect = Rectangle.withSize(x, y, w, h);

        for (int i = 0; i < lines.size(); i++) {
            FormattedText line = lines.get(i);
            GuiUtils.text(g, line, textRect.x(), textRect.y() + TextUtils.font().lineHeight * i, getThemeValue(!enabled.get() ? Theme.TEXT_DISABLED : Theme.TEXT), drawFontShadow.get());
        }
    }
}
