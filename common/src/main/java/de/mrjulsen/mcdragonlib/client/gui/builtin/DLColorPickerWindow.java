package de.mrjulsen.mcdragonlib.client.gui.builtin;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLColorPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLComboBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.INumberFormatAdapter;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;

public class DLColorPickerWindow extends DLWindow {

    private static enum ColorSpace implements ITranslatableEnum {
        RGB("rgb"),
        HSV("hsv");

        private final String name;

        private ColorSpace(String name) {
            this.name = name;
        }

        @Override
        public Data getTranslationData() {
            return new Data(DragonLib.MODID, "color_space", getSerializedName());
        }

        @Override
        public String toString() {
            return name;
        }
        
    }

    private static final int WINDOW_PADDING = 8;

    private final DLColorPicker picker;
    private final DLComboBox<ColorSpace> colorSpace;
    private final DLNumberPicker r;
    private final DLNumberPicker g;
    private final DLNumberPicker b;
    private final DLNumberPicker a;
    private final DLNumberPicker hex;
    private boolean skipValueUpdate = false;

    private final boolean showAlpha;
    private final DLColor initialColor;

    public DLColorPickerWindow(DLWindowManager manager, boolean showAlpha, DLColor initialColor, Consumer<DLColor> onAccept) {
        super(manager);
        this.showAlpha = showAlpha;
        this.initialColor = initialColor;

        movable.set(true);
        windowSpawnPosition.set(WindowPosition.PARENT_CENTER);

        picker = new DLColorPicker(WINDOW_PADDING, WINDOW_PADDING * 2 + Minecraft.getInstance().font.lineHeight, 120, 120 + (DLColorPicker.INNER_PADDING + DLColorPicker.SLIDER_SIZE) * (showAlpha ? 2 : 1));
        picker.sliderAlign.set(EAlign.BOTTOM);
        picker.showAlphaSlider.set(false);
        picker.color.set(initialColor.isUndefined() ? DLColor.WHITE : initialColor);
        addComponent(picker);

        colorSpace = new DLComboBox<>(picker.x() + picker.width() + 5, picker.y() + 30, 60, 16);
        colorSpace.items.addAll(ColorSpace.values());
        colorSpace.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {            
            updateInputBoxes();
            return false;
        });
        addComponent(colorSpace);

        r = new DLNumberPicker(colorSpace.x(), colorSpace.y() + colorSpace.height() + 2, colorSpace.width(), colorSpace.height());
        r.showButtons.set(false);
        r.max.set(255D);
        r.format.set(new INumberFormatAdapter.DecimalNumberFormat(0));
        r.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            if (skipValueUpdate) return false;
            skipValueUpdate = true;
            DLColor color = picker.color.get();
            colorSpace.selectedItem.get().ifPresent(c -> {
                picker.color.set(switch (c) {
                    case HSV -> DLColor.fromHsv(r.value.get().floatValue(), color.getSaturation(), color.getBrightness()).withAlpha(color.getAlpha());
                    default -> DLColor.of(color.getAlpha(), (int)e.value(), color.getGreen(), color.getBlue());
                });
            });
            skipValueUpdate = false;
            return false;
        });
        addComponent(r);

        g = new DLNumberPicker(r.x(), r.y() + r.height() + 2, r.width(), r.height());
        g.showButtons.set(false);
        g.max.set(255D);
        g.format.set(new INumberFormatAdapter.DecimalNumberFormat(0));
        g.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            if (skipValueUpdate) return false;
            skipValueUpdate = true;
            DLColor color = picker.color.get();            
            colorSpace.selectedItem.get().ifPresent(c -> {
                picker.color.set(switch (c) {
                    case HSV -> DLColor.fromHsv(color.getHue(), g.value.get().floatValue() / 100, color.getBrightness()).withAlpha(color.getAlpha());
                    default -> DLColor.of(color.getAlpha(), color.getRed(), (int)e.value(), color.getBlue());
                });
            });
            skipValueUpdate = false;
            return false;
        });
        addComponent(g);

        b = new DLNumberPicker(g.x(), g.y() + g.height() + 2, g.width(), g.height());
        b.showButtons.set(false);
        b.max.set(255D);
        b.format.set(new INumberFormatAdapter.DecimalNumberFormat(0));        
        b.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            if (skipValueUpdate) return false;
            skipValueUpdate = true;
            DLColor color = picker.color.get();            
            colorSpace.selectedItem.get().ifPresent(c -> {
                picker.color.set(switch (c) {
                    case HSV -> DLColor.fromHsv(color.getHue(), color.getSaturation(), b.value.get().floatValue() / 100).withAlpha(color.getAlpha());
                    default -> DLColor.of(color.getAlpha(), color.getGreen(), color.getGreen(), (int)e.value());
                });
            });
            skipValueUpdate = false;
            return false;
        });
        addComponent(b);        

        
        a = new DLNumberPicker(b.x(), b.y() + b.height() + 2, b.width(), b.height());
        if (showAlpha) {
            a.showButtons.set(false);
            a.max.set(100D);
            a.format.set(new INumberFormatAdapter.UnitNumberFormat(0, "%"));      
            a.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
                if (skipValueUpdate) return false;
                skipValueUpdate = true;
                DLColor color = picker.color.get();
                picker.color.set(color.withAlpha((int)(e.value() / 100 * 255)));
                skipValueUpdate = false;
                return false;
            });
            addComponent(a);
        }

        
        hex = new DLNumberPicker(b.x(), picker.y() + picker.height() - b.height(), b.width(), b.height());
        hex.showButtons.set(false);
        hex.min.set((double)Integer.MIN_VALUE);
        hex.max.set((double)Integer.MAX_VALUE);
        hex.format.set(new INumberFormatAdapter.HexARGBColorFormat(showAlpha));   
        hex.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            if (skipValueUpdate) return false;
            skipValueUpdate = true;
            picker.color.set(DLColor.fromInt(hex.value.get().intValue()));
            skipValueUpdate = false;
            return false;
        });
        addComponent(hex);
        
        int windowWidth = WINDOW_PADDING + colorSpace.x() + colorSpace.width();

        DLButton doneBtn = new DLButton(WINDOW_PADDING, picker.y() + picker.height() + WINDOW_PADDING, windowWidth / 2 - WINDOW_PADDING - 2, 16);
        doneBtn.text.set(CommonComponents.GUI_DONE);
        doneBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onAccept.accept(picker.color.get());
            manager.closeWindow(this);            
            return false;
        });
        addComponent(doneBtn);
        
        DLButton cancelBtn = new DLButton(windowWidth / 2 + 2, picker.y() + picker.height() + WINDOW_PADDING, windowWidth / 2 - WINDOW_PADDING - 2, 16);
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            manager.closeWindow(this);
            return false;
        });
        addComponent(cancelBtn);

        setSize(windowWidth, doneBtn.y() + doneBtn.height() + WINDOW_PADDING);

        
        picker.addEventListener(DLColorPicker.ColorChangedEvent.class, (s, e) -> {
            skipValueUpdate = true;
            updateInputBoxes();
            skipValueUpdate = false;
            return false;
        });

        updateInputBoxes();

    }

    private void updateInputBoxes() {
        colorSpace.selectedItem.get().ifPresent(x -> {
            switch (x) {
                case HSV -> {
                    r.max.set(360D);
                    r.value.set((double)picker.color.get().getHue());
                    g.max.set(100D);
                    g.value.set((double)picker.color.get().getSaturation() * 100);
                    b.max.set(100D);
                    b.value.set((double)picker.color.get().getBrightness() * 100);
                }
                default -> {
                    r.max.set(255D);
                    r.value.set((double)picker.color.get().getRed());
                    g.max.set(255D);
                    g.value.set((double)picker.color.get().getGreen());
                    b.max.set(255D);
                    b.value.set((double)picker.color.get().getBlue());
                }
            }
        });
        a.value.set((double)picker.color.get().getAlphaF() * 100);
        hex.value.set((double)picker.color.get().getAsARGB());
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite("window_rounded").render(graphics, 0, 0, width(), height());
        DLTextureSheet.DRAGONLIB_UI.getSprite("slot").render(graphics, picker.x() + picker.width() + 5, picker.y(), 60, 24);
        if (showAlpha) {
            DLTextureSheet.DRAGONLIB_UI.getSprite("transparency").render(graphics, picker.x() + picker.width() + 6, picker.y() + 1, 58, 22);
        }

        if (initialColor.isUndefined()) {
            GuiUtils.fill(graphics, picker.x() + picker.width() + 6, picker.y() + 1, 58, 22, picker.color.get());
        } else {            
            GuiUtils.fill(graphics, picker.x() + picker.width() + 6, picker.y() + 1, 29, 22, picker.color.get());
            GuiUtils.fill(graphics, picker.x() + picker.width() + 6 + 29, picker.y() + 1, 29, 22, initialColor);
        }
        GuiUtils.drawString(graphics, graphics.defaultFont(), WINDOW_PADDING, WINDOW_PADDING, TextUtils.translate("gui." + DragonLib.MODID + ".colorpicker.title"), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }    
}
