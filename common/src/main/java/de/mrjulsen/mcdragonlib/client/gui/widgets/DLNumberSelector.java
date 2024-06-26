package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.BiConsumer;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class DLNumberSelector extends WidgetContainer {

    private static final int DROP_DOWN_BUTTON_WIDTH = 13;

    private DLEditBox innerTextBox;
    private double min = 0;
    private double max = 100;
    private double value = 0;
    private boolean useDecimals = false;
    private final BiConsumer<DLNumberSelector, Double> onNumberChanged;

    @SuppressWarnings("resource")
    protected DLContextMenu menu = new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
        DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.cut"), Sprite.empty(), innerTextBox.canConsumeInput() && !innerTextBox.getHighlighted().isEmpty(), (b) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(innerTextBox.getHighlighted());
            if (innerTextBox.canConsumeInput()) {
                innerTextBox.insertText("");
            }
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.copy"), Sprite.empty(), innerTextBox.canConsumeInput() && !innerTextBox.getHighlighted().isEmpty(), (b) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(innerTextBox.getHighlighted());
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.paste"), Sprite.empty(), innerTextBox.canConsumeInput(), (b) -> {
            if (innerTextBox.canConsumeInput()) {
                innerTextBox.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.delete"), Sprite.empty(), !innerTextBox.getValue().isEmpty(), (b) -> {
            innerTextBox.setValue("");
        }, null));
        builder.addSeparator();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.select_all"), Sprite.empty(), true, (b) -> {
            innerTextBox.moveCursorToEnd(true);
            innerTextBox.setHighlightPos(0);
        }, null));
        builder.addSeparator();        
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.decrement"), Sprite.empty(), true, (b) -> {
            decrement();
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.increment"), Sprite.empty(), true, (b) -> {
            increment();
        }, null));
        return builder;
    });

    private boolean isUpdating = true;

    public DLNumberSelector(int x, int y, int width, int height, double initialValue, boolean showButtons, BiConsumer<DLNumberSelector, Double> onNumberChanged) {
        super(x, y, width, height);
        innerTextBox = new DLEditBox(font, x, y, width - (showButtons ? DROP_DOWN_BUTTON_WIDTH : 0), height, TextUtils.empty());
        innerTextBox.setMenu(null);
        innerTextBox.setFilter(this::isNumber);
        innerTextBox.setResponder((text) -> {
            if (!isUpdating) {
                setValue(text, false);
            }
            isUpdating = false;
        });
        innerTextBox.withOnFocusChanged((box, focus) -> {
            if (!focus) {
                setValue(box.getValue(), true);
            }
        });
        setValue(initialValue, true);
        addRenderableWidget(innerTextBox);

        if (showButtons) {
            int usableHeight = height - 2;
            int heightA = usableHeight / 2;
            DLButton btn1 = addRenderableWidget(new DLButton(x + width - DROP_DOWN_BUTTON_WIDTH, y + 1, DROP_DOWN_BUTTON_WIDTH - 1, heightA, TextUtils.text("+"), (btn) -> {
                increment();
            }));
            btn1.setRenderStyle(AreaStyle.GRAY);
            DLButton btn2 = addRenderableWidget(new DLButton(x + width - DROP_DOWN_BUTTON_WIDTH, y + 1 + heightA, DROP_DOWN_BUTTON_WIDTH - 1, usableHeight - heightA, TextUtils.text("-"), (btn) -> {
                decrement();
            }));
            btn2.setRenderStyle(AreaStyle.GRAY);
        }

        setMenu(menu);

        this.onNumberChanged = onNumberChanged;
    }

    /**
     * Set the minimum and maximum number (both inclusive) that is valid.
     * @param min
     * @param max
     */
    public void setNumberBounds(double min, double max) {
        this.min = min;
        this.max = max;
    }

    protected boolean isNumber(String input) {
        if (input.isEmpty())
            return true;

        String i = input;
        if (input.equals("-"))
            i = "-0";

        try {
            double d = useDecimals ? Double.parseDouble(i) : Integer.parseInt(i);            
            return d >= min && d <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double getValue() {
        return value;
    }

    public int getAsInt() {
        return (int)value;
    }

    public void setValue(String text, boolean updateTextbox) {
        double d = 0;
        try { 
            d = Double.parseDouble(text); 
        } catch (Exception e) {}
        setValue(d, updateTextbox);
    }

    public void setValue(double value, boolean updateTextbox) {
        this.value = value;
        if (updateTextbox) {
            innerTextBox.setValue(useDecimals ? String.valueOf(value) : String.valueOf((int)value));
        }
        if (onNumberChanged != null) {
            onNumberChanged.accept(this, value);
        }
    }

    public double increment() {
        double val = MathUtils.clamp(++this.value, min, max);
        setValue(val, true);
        return val;
    }

    public double decrement() {
        double val = MathUtils.clamp(--this.value, min, max);
        setValue(val, true);
        return val;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) { 
        GuiUtils.fill(graphics, x, y, width, height, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        GuiUtils.fill(graphics, x + 1, y + 1, width - 2, height - 2, 0xFF000000);
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public DLContextMenu getContextMenu() {
        return menu;
    }

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }

}