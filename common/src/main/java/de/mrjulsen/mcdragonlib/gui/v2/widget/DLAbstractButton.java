package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetRenderer;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public abstract class DLAbstractButton extends GuiComponent {

    public static class Renderer implements WidgetRenderer<DLAbstractButton> {

        public static final Renderer STANDARD = new Renderer(DLTextureSheet.VANILLA_BUTTON);

        public static final String SPRITE_NORMAL = "normal";
        public static final String SPRITE_PRESSED = "down_selected";
        public static final String SPRITE_HOVERED = "selected";
        public static final String SPRITE_DISABLED = "disabled";

        protected final DLTextureSheet spriteSheet;

        public Renderer(DLTextureSheet spriteSheet) {
            this.spriteSheet = spriteSheet;
        }

        @Override
        public void render(DLGuiGraphics g, GuiComponent component, double x, double y, double width, double height) {
            if (!component.enabled.get()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_DISABLED), x, y, width, height);
            } else if (component.isPressed()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_PRESSED), x, y, width, height);
            } else if (component.isHovered() || component.isFocused()) {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_HOVERED), x, y, width, height);
            } else {
                GuiUtils.sprite(g, spriteSheet.getSprite(SPRITE_NORMAL), x, y, width, height);
            }
        }
    }

    public record CaptionChangedEvent(Component text) implements IEvent {}

    public static final Theme.Key<WidgetRenderer<? extends DLAbstractButton>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".button.renderer", Renderer.STANDARD);
    public static final Theme.Key<DLColor> THEME_KEY_BACKGROUND_COLOR = Theme.key(DragonLib.MODID + ".button.background_color", DLColor.WHITE);

    protected static final int TEXT_PADDING = 6;
    protected static final int DEFAULT_BUTTON_HEIGHT = 20;
    protected static final int ICON_GAP = 4;

    public final EventListenerId defaultClickSoundEventId;

    public final Property<ETextAlignment> textAlignment = new Property<>(ETextAlignment.CENTER);
    public final Property<ETextAlignment> iconAlignment = new Property<>(ETextAlignment.CENTER);
    public final BooleanProperty drawFontShadow = new BooleanProperty(true);

    public DLAbstractButton(int x, int y, int w, int h) {
        super(x, y, w, h);
        focusable.set(true);
        clipsChildren.set(false);
        defaultClickSoundEventId = addEventListener(ClickEvent.class, (s, e) -> {
            playClickSound();
        }, GuiEvent.PRIORITY_LOWEST);
    }

    protected void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected Size measureContent(double availableWidth, double availableHeight) {
        double width = TextUtils.textWidth(TextUtils.font(), getText()) + TEXT_PADDING * 2;
        double height = 0;
        DLSprite sprite = getIcon();
        if (sprite != null && !sprite.isEmpty()) {
            width += sprite.getWidth() + ICON_GAP;
            height = Math.max(height, sprite.getHeight());
        }
        return Size.of(width, Math.max(DEFAULT_BUTTON_HEIGHT, height));
    }

    protected abstract DLSprite getIcon();
    protected abstract Component getText();

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.withTint(getThemeValue(THEME_KEY_BACKGROUND_COLOR), () -> {
            getThemeValue(THEME_KEY_RENDERER).render(g, this, 0, 0, dWidth(), dHeight());
        });

        final DLSprite sprite = getIcon();
        final Component label = getText();
        final double iconWidth = sprite == null || sprite.isEmpty() ? 0 : sprite.getWidth();
        final double textWidth = label == null ? 0 : TextUtils.textWidth(g.font(), label);
        final double gap = iconWidth > 0 && textWidth > 0 ? ICON_GAP : 0;

        final double contentX = TEXT_PADDING;
        final double contentWidth = Math.max(0, dWidth() - TEXT_PADDING * 2);

        double iconX;
        double textAreaX;
        double textAreaWidth;

        switch (iconAlignment.get()) {
            case LEFT -> {
                iconX = contentX;
                textAreaX = contentX + iconWidth + gap;
                textAreaWidth = contentWidth - iconWidth - gap;
            }
            case RIGHT -> {
                iconX = contentX + contentWidth - iconWidth;
                textAreaX = contentX;
                textAreaWidth = contentWidth - iconWidth - gap;
            }
            default -> {
                ETextAlignment blockAlignment = textWidth > 0 ? textAlignment.get() : ETextAlignment.CENTER;
                iconX = contentX + Math.max(0, GuiUtils.alignmentOffset(blockAlignment, contentWidth, iconWidth + gap + textWidth));
                textAreaX = iconX + iconWidth + gap;
                textAreaWidth = textWidth;
            }
        }

        final double textX = textAreaX + Math.max(0, GuiUtils.alignmentOffset(textAlignment.get(), textAreaWidth, textWidth));
        final double offset = isPressed() ? 1 : 0;

        if (iconWidth > 0) {
            sprite.render(g.legacy(), (int)iconX, (int)((dHeight() - sprite.getHeight()) / 2.0D + offset));
        }
        if (textWidth > 0) {
            GuiUtils.text(g, label, textX, TextUtils.centerTextY(g.font(), dHeight()) + offset, getThemeValue(!enabled.get() ? Theme.TEXT_DISABLED : Theme.TEXT), drawFontShadow.get());
        }
    }
}
