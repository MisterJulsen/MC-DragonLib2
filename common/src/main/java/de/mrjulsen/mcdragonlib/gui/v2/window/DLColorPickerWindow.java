package de.mrjulsen.mcdragonlib.gui.v2.window;

import java.util.Optional;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.INumberFormatAdapter;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLButton;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLColorPicker;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLComboBox;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLItemPicker;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLNumberPicker;
import de.mrjulsen.mcdragonlib.gui.v2.widget.DLPanel;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A ready-made dialog for picking a color: a {@link DLColorPicker} plus input fields for the channels
 * (RGB or HSV), the opacity and the hex code, and a preview of the new color next to the initial one.
 * <p>Open it through a {@link DLWindowManager}, preferably as a modal. The callback only fires when the
 * user confirms; cancelling or closing the window discards the selection.</p>
 */
public class DLColorPickerWindow extends DLWindow {

    private enum ColorSpace {
        RGB,
        HSV
    }

    private static final int PADDING = 8;
    private static final int GAP = 2;
    private static final int PICKER_SIZE = 120;
    private static final int SIDE_GAP = 5;
    private static final int SIDE_WIDTH = 60;
    private static final int FIELD_HEIGHT = 16;
    private static final int PREVIEW_HEIGHT = 24;
    private static final int BUTTON_HEIGHT = 16;

    private final DLColorPicker picker;
    private final DLComboBox<ColorSpace> colorSpace;
    private final DLNumberPicker channel1;
    private final DLNumberPicker channel2;
    private final DLNumberPicker channel3;
    private final DLNumberPicker alpha;
    private final DLNumberPicker hex;

    private final boolean showAlpha;
    private final DLColor initialColor;
    private boolean syncing;

    /**
     * @param showAlpha whether the opacity can be edited
     * @param initialColor the color to start with, shown next to the new one for comparison. {@link DLColor#UNDEFINED} starts with white and hides the comparison
     * @param onAccept called with the selected color when the user confirms
     */
    public DLColorPickerWindow(boolean showAlpha, DLColor initialColor, Consumer<DLColor> onAccept) {
        super(1, 1);
        this.showAlpha = showAlpha;
        this.initialColor = initialColor;

        spawnPosition.set(SpawnPosition.OWNER_CENTER);

        int titleHeight = PADDING * 2 + Minecraft.getInstance().font.lineHeight;

        picker = new DLColorPicker(PADDING, titleHeight, PICKER_SIZE, 0);
        picker.sliderPosition.set(DLColorPicker.SliderPosition.BOTTOM);
        picker.showAlphaSlider.set(showAlpha);
        int sliderCount = 1 + (showAlpha ? 1 : 0);
        picker.setHeight(PICKER_SIZE + sliderCount * (picker.getThemeValue(DLColorPicker.THEME_KEY_SLIDER_SIZE) + picker.getThemeValue(DLColorPicker.THEME_KEY_SPACING)));
        picker.color.set(initialColor.isUndefined() ? DLColor.WHITE : initialColor);
        addComponent(picker);

        int sideX = picker.right() + SIDE_GAP;

        colorSpace = new DLComboBox<>(sideX, picker.y() + PREVIEW_HEIGHT + 6, SIDE_WIDTH, FIELD_HEIGHT);
        colorSpace.items.addAll(ColorSpace.values());
        colorSpace.textFormat.set(item -> TextUtils.text(item.map(ColorSpace::name).orElse("")));
        colorSpace.addEventListener(DLItemPicker.SelectedItemChanged.class, (s, e) -> updateInputs());
        addComponent(colorSpace);

        channel1 = addField(sideX, colorSpace.bottom() + GAP, new INumberFormatAdapter.DecimalNumberFormat(0));
        channel2 = addField(sideX, channel1.bottom() + GAP, new INumberFormatAdapter.DecimalNumberFormat(0));
        channel3 = addField(sideX, channel2.bottom() + GAP, new INumberFormatAdapter.DecimalNumberFormat(0));
        channel1.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> applyChannels());
        channel2.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> applyChannels());
        channel3.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> applyChannels());

        alpha = addField(sideX, channel3.bottom() + GAP, new INumberFormatAdapter.UnitNumberFormat(0, "%"));
        alpha.visible.set(showAlpha);
        alpha.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> applyColor(picker.color.get().withAlpha((int)Math.round(e.value() / 100 * 255))));

        hex = addField(sideX, picker.bottom() - FIELD_HEIGHT, new INumberFormatAdapter.HexARGBColorFormat(showAlpha));
        hex.min.set((double)Integer.MIN_VALUE);
        hex.max.set((double)Integer.MAX_VALUE);
        hex.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            DLColor parsed = DLColor.fromInt((int)e.value());
            applyColor(showAlpha ? parsed : parsed.withAlpha(picker.color.get().getAlpha()));
        });

        int windowWidth = sideX + SIDE_WIDTH + PADDING;
        int buttonY = picker.bottom() + PADDING;
        int buttonWidth = (windowWidth - PADDING * 2 - GAP * 2) / 2;

        DLButton doneBtn = addButton(PADDING, buttonY, buttonWidth, CommonComponents.GUI_DONE);
        doneBtn.addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
            onAccept.accept(picker.color.get());
            close();
        });

        DLButton cancelBtn = addButton(windowWidth - PADDING - buttonWidth, buttonY, buttonWidth, CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(GuiEvents.ClickEvent.class, (s, e) -> close());

        setSize(windowWidth, buttonY + BUTTON_HEIGHT + PADDING);

        DLPanel titleBar = new DLPanel(0, 0, windowWidth, titleHeight);
        addComponent(titleBar);
        movable.set(true);
        moveWith(titleBar);

        picker.addEventListener(DLColorPicker.ColorChangedEvent.class, (s, e) -> updateInputs());

        updateInputs();
    }

    private DLNumberPicker addField(int x, int y, INumberFormatAdapter format) {
        DLNumberPicker field = new DLNumberPicker(x, y, SIDE_WIDTH, FIELD_HEIGHT);
        field.showButtons.set(false);
        field.format.set(format);
        return addComponent(field);
    }

    private DLButton addButton(int x, int y, int width, Component text) {
        DLButton button = new DLButton(x, y, width, BUTTON_HEIGHT);
        button.text.set(text);
        return addComponent(button);
    }

    private ColorSpace currentColorSpace() {
        return colorSpace.selectedItem.get().orElse(ColorSpace.RGB);
    }

    /**
     * The color currently selected in the dialog.
     */
    public DLColor getColor() {
        return picker.color.get();
    }

    private void applyChannels() {
        if (syncing) {
            return;
        }
        int a = picker.color.get().getAlpha();
        applyColor(switch (currentColorSpace()) {
            case HSV -> DLColor.fromHsv(channel1.value.get().floatValue(), channel2.value.get().floatValue() / 100f, channel3.value.get().floatValue() / 100f).withAlpha(a);
            case RGB -> DLColor.of(a, channel1.value.get().intValue(), channel2.value.get().intValue(), channel3.value.get().intValue());
        });
    }

    private void applyColor(DLColor color) {
        if (syncing) {
            return;
        }
        picker.color.set(color);
    }

    private void updateInputs() {
        syncing = true;
        try {
            DLColor color = picker.color.get();
            switch (currentColorSpace()) {
                case HSV -> {
                    setChannel(channel1, 360, picker.getHue() * 360);
                    setChannel(channel2, 100, picker.getSaturation() * 100);
                    setChannel(channel3, 100, picker.getBrightness() * 100);
                }
                case RGB -> {
                    setChannel(channel1, 255, color.getRed());
                    setChannel(channel2, 255, color.getGreen());
                    setChannel(channel3, 255, color.getBlue());
                }
            }
            alpha.value.set((double)Math.round(color.getAlphaF() * 100));
            hex.value.set((double)color.getAsARGB());
        } finally {
            syncing = false;
        }
    }

    private static void setChannel(DLNumberPicker field, double max, double value) {
        field.max.set(max);
        field.value.set((double)Math.round(value));
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.sprite(g, DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED), 0, 0, dWidth(), dHeight());
        GuiUtils.text(g, TextUtils.translate("gui." + DragonLib.MODID + ".colorpicker.title"), PADDING, PADDING, getThemeValue(Theme.TEXT_INVERTED), false);

        double x = picker.right() + SIDE_GAP;
        double y = picker.y();
        double innerX = x + 1;
        double innerY = y + 1;
        double innerWidth = SIDE_WIDTH - 2;
        double innerHeight = PREVIEW_HEIGHT - 2;

        GuiUtils.sprite(g, DLTextureSheet.DRAGONLIB_UI.getSprite(DLColorPicker.Renderer.SPRITE_FRAME), x, y, SIDE_WIDTH, PREVIEW_HEIGHT);
        if (showAlpha) {
            GuiUtils.sprite(g, DLTextureSheet.DRAGONLIB_UI.getSprite(DLColorPicker.Renderer.SPRITE_TRANSPARENCY), innerX, innerY, innerWidth, innerHeight);
        }
        if (initialColor.isUndefined()) {
            GuiUtils.fill(g, innerX, innerY, innerWidth, innerHeight, picker.color.get());
        } else {
            double half = innerWidth / 2;
            GuiUtils.fill(g, innerX, innerY, half, innerHeight, picker.color.get());
            GuiUtils.fill(g, innerX + half, innerY, innerWidth - half, innerHeight, initialColor);
        }
    }

    /**
     * Opens a color picker as a modal on top of everything else in the given manager.
     * @return the opened window
     */
    public static DLColorPickerWindow open(DLWindowManager manager, boolean showAlpha, DLColor initialColor, Consumer<DLColor> onAccept) {
        DLColorPickerWindow window = new DLColorPickerWindow(showAlpha, initialColor, onAccept);
        manager.openModal(window);
        return window;
    }

    /**
     * Opens a color picker as a modal centered on the given window.
     * @return the opened window
     */
    public static DLColorPickerWindow open(DLWindow owner, boolean showAlpha, DLColor initialColor, Consumer<DLColor> onAccept) {
        DLColorPickerWindow window = new DLColorPickerWindow(showAlpha, initialColor, onAccept);
        window.owner.set(owner);
        Optional.ofNullable(owner.getWindowManager()).ifPresent(m -> m.openModal(window));
        return window;
    }
}
