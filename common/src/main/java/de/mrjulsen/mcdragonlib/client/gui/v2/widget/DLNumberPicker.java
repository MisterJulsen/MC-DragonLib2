package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.INumberFormatAdapter;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.gui.v2.Alignment;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.client.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.client.gui.v2.input.InputKind;
import de.mrjulsen.mcdragonlib.client.gui.v2.layout.StackLayout;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.AbstractContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.ContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.v2.widget.contextmenu.ContextMenuSeparator;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class DLNumberPicker extends GuiComponent {
    
    public record ValueChangedEvent(double value) implements IEvent {}
    public record ValueRangeChangedEvent(double min, double max) implements IEvent {}

    public static final int BUTTON_WIDTH = 16;


    protected final DLTextBox textBox;
    protected final DLPanel buttonPanel;
    protected final DLButton addBtn;
    protected final DLButton subBtn;

    public final BooleanProperty showButtons = new BooleanProperty(true);
    public final NumberProperty<Double> step = new NumberProperty<>(1D);
    public final NumberProperty<Double> shiftStep = new NumberProperty<>(5D);
    public final NumberProperty<Double> min = new NumberProperty<>(0D);
    public final NumberProperty<Double> max = new NumberProperty<>(100D);
    public final NumberProperty<Double> value = new NumberProperty<Double>(0D, min::get, max::get);
    public final Property<INumberFormatAdapter> format = new Property<>(new INumberFormatAdapter.DecimalNumberFormat(0));

    protected boolean valueUpdateLoopFix = false;

    public DLNumberPicker(int x, int y, int w, int h) {
        super(x, y, w, h);
        overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.FLAT_GRAY_RENDERER);

        StackLayout layout = new StackLayout(StackLayout.Direction.HORIZONTAL);
        layout.crossAlignment.set(Alignment.STRETCH);
        this.layout.set(layout);

        textBox = new DLTextBox(0, 0, Math.max(0, w - BUTTON_WIDTH), h) {
            @Override
            protected List<AbstractContextMenuItem> createContextMenu() {
                List<AbstractContextMenuItem> items = super.createContextMenu();
                items.add(new ContextMenuSeparator());
                items.add(new ContextMenuItem.Builder()
                        .text(TextUtils.translate("gui." + DragonLib.MODID + ".menu.increment"))
                        .enabled(() -> value.get() < max.get())
                        .clickAction((menu, localX, localY, button) -> {
                            addToValue(step.get());
                        })
                        .build());
                items.add(new ContextMenuItem.Builder()
                        .text(TextUtils.translate("gui." + DragonLib.MODID + ".menu.decrement"))
                        .enabled(() -> value.get() > min.get())
                        .clickAction((menu, localX, localY, button) -> {
                            addToValue(-step.get());
                        })
                        .build());
                return items;
            }
        };
        textBox.widthMode.set(SizeMode.FILL);
        textBox.multiline.set(false);
        textBox.lineSpacing.set(2F);
        textBox.acceptAndCancelKeysEnabled.set(true);
        textBox.acceptedInput.set(InputKind.allExcept(InputKind.SCROLL));
        addComponent(textBox);

        buttonPanel = new DLPanel(0, 0, BUTTON_WIDTH, h);
        buttonPanel.acceptedInput.set(InputKind.allExcept(InputKind.SCROLL));
        StackLayout buttonLayout = new StackLayout(StackLayout.Direction.VERTICAL);
        buttonLayout.crossAlignment.set(Alignment.STRETCH);
        buttonPanel.layout.set(buttonLayout);

        addBtn = createStepButton(GuiIcons.ARROW_UP, 1);
        buttonPanel.addComponent(addBtn);

        subBtn = createStepButton(GuiIcons.ARROW_DOWN, -1);
        buttonPanel.addComponent(subBtn);

        addComponent(buttonPanel);

        addEventListener(ValueChangedEvent.class, (src, event) -> {
            updateTextboxValue();
        });
        addEventListener(GuiEvents.ScrollEvent.class, (s, e) -> {
            addToValue(Math.signum(e.deltaY()));
            updateTextboxValue();
        });
        
        textBox.addEventListener(GuiEvents.FocusChangedEvent.class, (s, e) -> {
            if (!e.focused()) {
                updateValueFromTextbox();
            }
        });
        textBox.addEventListener(DLTextBox.TextAcceptKeyPressedEvent.class, (s, e) -> {
            updateValueFromTextbox();
        });
        textBox.addEventListener(DLTextBox.TextCancelKeyPressedEvent.class, (s, e) -> {
            updateTextboxValue();
        });
        
        updateTextboxValue();
        updateButtons();

        this.value.addChangeListener((o, n) -> invokeEvent(this, new ValueChangedEvent(n)));
        this.min.addChangeListener((o, n) -> invokeEvent(this, new ValueRangeChangedEvent(min.get(), max.get())));
        this.max.addChangeListener((o, n) -> invokeEvent(this, new ValueRangeChangedEvent(min.get(), max.get())));
        this.showButtons.addChangeListener((o, n) -> updateButtons());
    }

    protected DLButton createStepButton(GuiIcons icon, double direction) {
        DLButton button = new DLButton(0, 0, BUTTON_WIDTH, 0);
        button.heightMode.set(SizeMode.FILL);
        button.text.set(TextUtils.empty());
        button.icon.set(new DLSprite(GuiIcons.ICON_TEXTURE, BUTTON_WIDTH, BUTTON_WIDTH, icon.getU(), icon.getV(), GuiIcons.ICON_SIZE, GuiIcons.ICON_SIZE));
        button.acceptedInput.set(InputKind.allExcept(InputKind.SCROLL));
        button.addEventListener(GuiEvents.MouseHoldDownEvent.class, (s, e) -> {
            if (e.leftClick()) {
                addToValue(direction);
            }
        });
        return button;
    }

    protected void addToValue(double fac) {
        this.value.set(this.value.get() + (Screen.hasShiftDown() ? this.shiftStep.get() : this.step.get()) * fac);
    }

    protected void updateTextboxValue() {
        valueUpdateLoopFix = true;
        String formatted = format.get().format(value.get());
        textBox.setText(formatted);
        valueUpdateLoopFix = false;
    }

    protected void updateValueFromTextbox() {
        String input = textBox.getPlainText().trim();
        try {
            double parsed = format.get().parse(input);
            value.set(parsed);
        } catch (NumberFormatException ignored) {}
        updateTextboxValue();
    }

    protected void updateButtons() {
        buttonPanel.visible.set(showButtons.get());
    }
}
