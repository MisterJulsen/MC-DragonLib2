package de.mrjulsen.mcdragonlib.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
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
import de.mrjulsen.mcdragonlib.gui.v2.theme.WidgetState;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public abstract class DLAbstractButton extends GuiComponent {

    public static class Renderer implements WidgetRenderer<DLAbstractButton> {

        public static final String SPRITE_NORMAL = "normal";
        public static final String SPRITE_DISABLED = "disabled";
        public static final String SPRITE_SELECTED = "selected";
        public static final String SPRITE_DOWN = "down";
        public static final String SPRITE_DOWN_SELECTED = "down_selected";

        protected final DLTextureSheet spriteSheet;

        public Renderer(DLTextureSheet spriteSheet) {
            this.spriteSheet = spriteSheet;
        }

        protected String getSprite(DLAbstractButton component) {
            return switch (WidgetState.of(component)) {
                case DISABLED, DISABLED_HOVERED -> SPRITE_DISABLED;
                case HOVERED, FOCUSED -> SPRITE_SELECTED;
                case PRESSED -> SPRITE_DOWN_SELECTED;
                default -> SPRITE_NORMAL;
            };
        }

        @Override
        public void render(DLGuiGraphics g, DLAbstractButton component, double x, double y, double width, double height) {
            GuiUtils.sprite(g, spriteSheet.getSprite(getSprite(component)), x, y, width, height);
        }
    }

    public static class FlatRenderer extends Renderer {

        private final String spritePrefix;

        public FlatRenderer(DLTextureSheet spriteSheet, String spritePrefix) {
            super(spriteSheet);
            this.spritePrefix = spritePrefix;
        }

        @Override
        protected String getSprite(DLAbstractButton component) {
            return "button_" + spritePrefix + "_" + switch (WidgetState.of(component)) {
                case DISABLED, DISABLED_HOVERED -> SPRITE_DISABLED;
                case HOVERED -> SPRITE_SELECTED;
                case PRESSED -> SPRITE_DOWN_SELECTED;
                default -> SPRITE_NORMAL;
            };
        }
    }


    public static final Renderer VANILLA_RENDERER = new Renderer(DLTextureSheet.VANILLA_BUTTON);
    public static final Renderer VANILLA_LEGACY_RENDERER = new Renderer(DLTextureSheet.VANILLA_BUTTON) {
        @Override
        protected String getSprite(DLAbstractButton component) {
            return "legacy_" + super.getSprite(component);
        }
    };
    public static final Renderer FLAT_GRAY_RENDERER = new FlatRenderer(DLTextureSheet.DRAGONLIB_UI, "gray");
    public static final Renderer FLAT_BROWN_RENDERER = new FlatRenderer(DLTextureSheet.DRAGONLIB_UI, "brown");
    public static final Renderer FLAT_RED_RENDERER = new FlatRenderer(DLTextureSheet.DRAGONLIB_UI, "red");


    public record CaptionChangedEvent(Component text) implements IEvent {}

    public static final Theme.Key<WidgetRenderer<DLAbstractButton>> THEME_KEY_RENDERER = Theme.key(DragonLib.MODID + ".button.renderer", VANILLA_RENDERER);
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
        cursor.set(CursorType.HAND);
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
        final double iconSpace = iconWidth > 0 && textWidth > 0 ? iconWidth + ICON_GAP : iconWidth;
        final double contentWidth = dWidth() - TEXT_PADDING * 2;

        final ETextAlignment textAlign = textWidth > 0 ? textAlignment.get() : ETextAlignment.CENTER;
        final boolean attached = iconAlignment.get() == ETextAlignment.CENTER;
        final double iconFactor = attached ? 0 : iconAlignment.get().getId() / 2D;

        final double textX = TEXT_PADDING + iconSpace * (1 - iconFactor) + (contentWidth - iconSpace - textWidth) * textAlign.getId() / 2D;
        final double iconX = attached ? textX - iconSpace : TEXT_PADDING + (contentWidth - iconWidth) * iconFactor;
        final double offset = isPressed() && getPressedButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT ? 1 : 0;

        if (iconWidth > 0) {
            sprite.render(g.legacy(), (int)Math.floor(iconX), (int)Math.floor((dHeight() - sprite.getHeight()) / 2D + offset));
        }
        if (textWidth > 0) {
            GuiUtils.text(g, label, textX, TextUtils.centerTextY(g.font(), dHeight()) + offset, getThemeValue(!enabled.get() ? Theme.TEXT_DISABLED : Theme.TEXT), drawFontShadow.get());
        }
    }
}
