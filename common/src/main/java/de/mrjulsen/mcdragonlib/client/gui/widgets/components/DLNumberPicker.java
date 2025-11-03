package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.client.gui.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.client.gui.properties.Property;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu.ItemEntry;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaTextBoxRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.DLAbstractRichTextInputField;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.INumberFormatAdapter;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

@SupportsEvents({
    DLNumberPicker.ValueChangedEvent.class,
    DLNumberPicker.ValueRangeChangedEvent.class
})
public class DLNumberPicker extends DLGuiComponent {
    
    public record ValueChangedEvent(double value) implements IEvent {}
    public record ValueRangeChangedEvent(double min, double max) implements IEvent {}

    private static final int BUTTON_WIDTH = 16;


    protected final DLRichTextEditBox textBox;
    protected final DLButton addBtn;
    protected final DLButton subBtn;

    public final BooleanProperty showButtons = new BooleanProperty(true, false);
    public final NumberProperty<Double> step = new NumberProperty<Double>(1D);
    public final NumberProperty<Double> min = new NumberProperty<Double>(0D);
    public final NumberProperty<Double> max = new NumberProperty<Double>(100D);
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, () -> min.get(), () -> max.get())
        .withAfterPropertyChangedCallback((o, val) -> invokeEvent(this, new DLNumberPicker.ValueChangedEvent(val)));
    public final Property<INumberFormatAdapter> format = new Property<>(new INumberFormatAdapter.DecimalNumberFormat(0));
    public final Property<IStateRenderer<DLButton.ButtonState>> buttonsComponentRenderer = new Property<>(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
    public final Property<IStateRenderer<DLRichTextEditBox.TextBoxState>> textboxComponentRenderer = new Property<>(VanillaTextBoxRenderer.VANILLA_TEXTBOX);

    protected boolean valueUpdateLoopFix = false;

    public DLNumberPicker(int x, int y, int w, int h) {
        super(x, y, w, h);

        textBox = new DLRichTextEditBox(0, 0, width() - BUTTON_WIDTH, height()) {
            @Override
            public List<ItemEntry> buildContextMenuContents(int x, int y) {
                List<ItemEntry> entries = super.buildContextMenuContents(x, y);
                
                if (!readOnly.get()) {
                    entries.add(DLContextMenu.ItemEntry.SEPARATOR);
                    entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".menu.increment"), DLSprite.empty(), value.get() < max.get(), () -> {
                        value.set(value.get() + step.get());
                    }, null));
                    
                    entries.add(new DLContextMenu.ItemEntry(TextUtils.translate("gui." + DragonLib.MODID + ".menu.decrement"), DLSprite.empty(), value.get() > min.get(), () -> {
                        value.set(value.get() - step.get());
                    }, null));
                }
                return entries;
            }
        };
        textBox.multiline.set(false);
        textBox.contentPadding.set(new Padding(0, 2, 0, 2));
        textBox.decoratedPadding.set(new Padding(1));
        textBox.lineSpacing.set(2);
        textBox.componentRenderer.set(textboxComponentRenderer.get());
        textBox.cursorXOffset.set(1);
        textBox.acceptAndCancelKeysEnabled.set(true);
        textBox.inputConsumptionPolicy.set((type) -> {
            return type != ConsumptionType.SCROLL;
        });
        addComponent(textBox);

        addBtn = new DLButton(width() - BUTTON_WIDTH, 0, BUTTON_WIDTH, height() / 2);
        addBtn.text.set(TextUtils.text("+"));
        addBtn.componentRenderer.set(buttonsComponentRenderer.get());
        addBtn.addEventListener(DLGuiStandardEvents.MouseHoldDownEvent.class, (src, event) -> {
            this.value.set(this.value.get() + this.step.get());
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
            this.value.set(this.value.get() - this.step.get());
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
            this.value.set(this.value.get() - (Math.signum(event.deltaY()) * step.get()));
            updateTextboxValue();
            return false;
        });
        
        textBox.addEventListener(DLGuiStandardEvents.FocusChangedEvent.class, (src, event) -> {
            if (!event.focus()) {
                updateValueFromTextbox();
            }
            return false;
        });
        textBox.addEventListener(DLAbstractRichTextInputField.TextAcceptKeyPressedEvent.class, (src, event) -> {
            updateValueFromTextbox();
            return false;
        });
        textBox.addEventListener(DLAbstractRichTextInputField.TextCancelKeyPressedEvent.class, (src, event) -> {
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
                textBox.setWidth(width() - BUTTON_WIDTH);
            } else {
                textBox.setWidth(width());
            }
        });        

        buttonsComponentRenderer.withAfterPropertyChangedCallback((o, val) -> {
            addBtn.componentRenderer.set(val);
            subBtn.componentRenderer.set(val);
        });  

        textboxComponentRenderer.withAfterPropertyChangedCallback((o, val) -> {
            textBox.componentRenderer.set(val);
        });
    }

    protected void updateTextboxValue() {
        valueUpdateLoopFix = true;
        String formatted = format.get().format(value.get());
        textBox.text.get().set(formatted);
        valueUpdateLoopFix = false;
    }

    protected void updateValueFromTextbox() {
        String input = textBox.text.get().getPlainText().trim();
        try {
            double parsed = format.get().parse(input);
            value.set(parsed);
        } catch (NumberFormatException e) {}
        updateTextboxValue();
    }

    protected void updateButtons() {
        addBtn.visible.set(showButtons.get());
        subBtn.visible.set(showButtons.get());
        this.textBox.setWidth(showButtons.get() ? width() : width() - BUTTON_WIDTH);
    }


    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);
    }
    
}
