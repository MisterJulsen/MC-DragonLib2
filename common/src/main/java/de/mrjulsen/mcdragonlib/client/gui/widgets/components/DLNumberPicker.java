package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaTextBoxRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.SpriteTextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.INumberFormatAdapter;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

@SupportsEvents({
    DLNumberPicker.ValueChangedEvent.class,
    DLNumberPicker.ValueRangeChangedEvent.class
})
public class DLNumberPicker extends DLGuiComponent {

    public record ValueChangedEvent(double value) implements IEvent {}
    public record ValueRangeChangedEvent(double min, double max) implements IEvent {}

    private static final int BUTTON_WIDTH = 16;
    private static final int CARET_MARGIN = 1;


    protected final DLTextBox input;
    protected final DLButton addBtn;
    protected final DLButton subBtn;

    @Deprecated
    protected final DLRichTextEditBox textBox = new DLRichTextEditBox(0, 0, 1, 1);

    public final BooleanProperty showButtons = new BooleanProperty(true);
    public final NumberProperty<Double> step = new NumberProperty<Double>(1D);
    public final NumberProperty<Double> shiftStep = new NumberProperty<>(5D);
    public final NumberProperty<Double> min = new NumberProperty<Double>(0D);
    public final NumberProperty<Double> max = new NumberProperty<Double>(100D);
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> min.get(), () -> max.get())
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new DLNumberPicker.ValueChangedEvent(val)));
    public final Property<INumberFormatAdapter> format = new Property<>(new INumberFormatAdapter.DecimalNumberFormat(0));
    public final Property<IStateRenderer<DLButton.ButtonState>> buttonsComponentRenderer = new Property<>(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
    public final Property<TextBoxStyle> textBoxRenderer = new Property<>(defaultTextBoxStyle());

    @Deprecated
    public final Property<IStateRenderer<DLRichTextEditBox.TextBoxState>> textboxComponentRenderer = new Property<>(VanillaTextBoxRenderer.VANILLA_TEXTBOX);

    protected boolean valueUpdateLoopFix = false;

    public DLNumberPicker(int x, int y, int w, int h) {
        super(x, y, w, h);

        input = new DLTextBox(0, 0, width() - BUTTON_WIDTH, height());
        input.multiline.set(false);
        input.padding.set(new Padding(0, 2, 0, 3));
        input.lineSpacing.set(2.0F);
        input.componentRenderer.set(textBoxRenderer.get());
        input.acceptAndCancelKeysEnabled.set(true);
        input.inputConsumptionPolicy.set((type) -> {
            return type != ConsumptionType.SCROLL;
        });
        input.addContextMenuContributor((box, context, entries) -> {
            if (!box.isEditable()) {
                return;
            }
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".menu.increment"), DLSprite.empty(), value.get() < max.get(), () -> {
                addToValue(1);
            }, null));
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".menu.decrement"), DLSprite.empty(), value.get() > min.get(), () -> {
                addToValue(-1);
            }, null));
        });
        addComponent(input);

        addBtn = new DLButton(width() - BUTTON_WIDTH, 0, BUTTON_WIDTH, height() / 2);
        addBtn.text.set(TextUtils.text("+"));
        addBtn.componentRenderer.set(buttonsComponentRenderer.get());
        addBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            addToValue(1);
            return true;
        });
        addBtn.inputConsumptionPolicy.set((type) -> {
            return type != ConsumptionType.SCROLL;
        });
        addComponent(addBtn);

        subBtn = new DLButton(width() - BUTTON_WIDTH, height() / 2, BUTTON_WIDTH, height() / 2);
        subBtn.text.set(TextUtils.text("-"));
        subBtn.componentRenderer.set(buttonsComponentRenderer.get());
        subBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            addToValue(-1);
            return true;
        });
        subBtn.inputConsumptionPolicy.set((type) -> {
            return type != ConsumptionType.SCROLL;
        });
        addComponent(subBtn);

        addEventListener(DLNumberPicker.ValueChangedEvent.class, (src, event) -> {
            updateTextboxValue();
            return false;
        });
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, (src, event) -> {
            addToValue(-Math.signum(event.deltaY()));
            updateTextboxValue();
            return false;
        });

        input.addEventListener(DLGuiStandardEvents.FocusChangedEvent.class, (src, event) -> {
            if (!event.focus()) {
                updateValueFromTextbox();
            }
            return false;
        });
        input.addEventListener(DLTextBox.TextAcceptKeyPressedEvent.class, (src, event) -> {
            updateValueFromTextbox();
            return false;
        });
        input.addEventListener(DLTextBox.TextCancelKeyPressedEvent.class, (src, event) -> {
            updateTextboxValue();
            return false;
        });

        updateTextboxValue();
        updateButtons();

        this.min.withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new DLNumberPicker.ValueRangeChangedEvent(min.get(), max.get())));
        this.max.withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new DLNumberPicker.ValueRangeChangedEvent(min.get(), max.get())));
        this.showButtons.withAfterPropertyChangedCallback((o, val) -> {
            updateButtons();
            if (val) {
                input.setWidth(width() - BUTTON_WIDTH);
            } else {
                input.setWidth(width());
            }
        });

        buttonsComponentRenderer.withAfterPropertyChangedCallback((o, val) -> {
            addBtn.componentRenderer.set(val);
            subBtn.componentRenderer.set(val);
        });

        textBoxRenderer.withAfterPropertyChangedCallback((o, val) -> {
            input.componentRenderer.set(val);
        });

        textboxComponentRenderer.withAfterPropertyChangedCallback((o, val) -> {
            textBoxRenderer.set(legacyTextBoxStyle(val));
        });
    }

    protected static TextBoxStyle defaultTextBoxStyle() {
        SpriteTextBoxStyle style = new SpriteTextBoxStyle();
        style.caretMargin = CARET_MARGIN;
        return style;
    }

    protected static TextBoxStyle legacyTextBoxStyle(IStateRenderer<DLRichTextEditBox.TextBoxState> renderer) {
        TextBoxStyle style = new TextBoxStyle() {
            @Override
            public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, DLTextBox.TextBoxState state) {
                renderer.renderSprite(graphics, x, y, w, h, component, switch (state) {
                    case SELECTED -> DLRichTextEditBox.TextBoxState.SELECTED;
                    case FOCUSED -> DLRichTextEditBox.TextBoxState.FOCUSED;
                    case DISABLED -> DLRichTextEditBox.TextBoxState.DISABLED;
                    default -> DLRichTextEditBox.TextBoxState.NORMAL;
                });
            }
        };
        style.caretMargin = CARET_MARGIN;
        return style;
    }

    public DLTextBox textInput() {
        return input;
    }

    protected void addToValue(double fac) {
        this.value.set(this.value.get() + (DLWindowManager.hasShiftDown() ? this.shiftStep.get() : this.step.get()) * fac);
    }

    protected void updateTextboxValue() {
        valueUpdateLoopFix = true;
        String formatted = format.get().format(value.get());
        input.setText(formatted);
        textBox.text.get().set(formatted);
        valueUpdateLoopFix = false;
    }

    protected void updateValueFromTextbox() {
        String value = input.getText().trim();
        try {
            double parsed = format.get().parse(value);
            this.value.set(parsed);
        } catch (NumberFormatException e) {}
        updateTextboxValue();
    }

    protected void updateButtons() {
        addBtn.visible.set(showButtons.get());
        subBtn.visible.set(showButtons.get());
        this.input.setWidth(showButtons.get() ? width() : width() - BUTTON_WIDTH);
    }


    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);
    }

}
