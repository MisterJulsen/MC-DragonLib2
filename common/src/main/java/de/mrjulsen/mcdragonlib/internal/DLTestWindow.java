package de.mrjulsen.mcdragonlib.internal;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.gui.builtin.DLColorPickerWindow;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLBasicDataView;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCheckBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLComboBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLEditableLabel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLItemPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLItemSelectionBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLProgressBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLAbstractDataView.DataSlot;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLAbstractDataView.SizeMode;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLAbstractDataView.DataSlotComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLProgressBar.ProgressBarStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.autocomplete.DLAutocompleteWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class DLTestWindow extends DLWindow {

    private DLButton btnTest;
    private DLCheckBox chbTest;
    private DLCycleButton<String> cycleButtonTest;
    private DLPanel panel;

    private Component txt = TextUtils.empty();

    
    private DLAutocompleteWindow<String> win;

    public DLTestWindow(DLWindowManager manager) {
        super(manager);
        anchor.set(EAlign.values());
        fullscreen.set(true);
        
        addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (s, e) -> {
            if (e.keyCode() == GLFW.GLFW_KEY_ESCAPE) {
                getWindowManager().close();
            }
            return false;
        });

        DLButton closeBtn = new DLButton(100, 0, 80, 20);
        //closeBtn.anchor.set2(EAlign.TOP, EAlign.RIGHT);
        closeBtn.text.set(TextUtils.text("Close"));
        closeBtn.tooltip.set(new DLTooltip(List.of(TextUtils.text("Close")), 100));
        closeBtn.icon.set(new DLSprite(new ItemStack(Blocks.BARRIER), 16, false));
        closeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().close();
            return false;
        });
        addComponent(closeBtn);

        DLContextMenu contextMenu = new DLContextMenu((x, y) -> {
            List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
            entries.add(new DLContextMenu.ItemEntry(TextUtils.text("Item 1"), new DLSprite(new ItemStack(Blocks.RAIL, 27), 16, true), true, () -> {}, (pX, pY) -> {
                return List.of(new DLContextMenu.ItemEntry(TextUtils.text("Testitem 435"), DLSprite.empty(), true, () -> {}, null));
            }));
            entries.add(new DLContextMenu.ItemEntry(TextUtils.text("Item 2"), DLSprite.empty(), true, () -> {                
                getWindowManager().createWindow((mgr) -> new DLColorPickerWindow(mgr, false, DLColor.BLACK, (c) -> {}));
            }, null));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.text("item 3"), DLSprite.empty(), true, () -> {                
                getWindowManager().createModal((mgr) -> new DLColorPickerWindow(mgr, false, DLColor.BLACK, (c) -> {}));
            }, (pX, pY) -> entries));
            return entries;
        });

        btnTest = new DLButton(20, 20);
        btnTest.textAlignment.set(ETextAlignment.CENTER);
        btnTest.iconAlignment.set(ETextAlignment.CENTER);
        btnTest.tooltip.set(new DLTooltip(List.of(TextUtils.text("Open Menu")), 100));
        btnTest.addEventListener(DLGuiStandardEvents.ClickEvent.class, (src, event) -> {
            contextMenu.open(getWindowManager());
            return false;
        });
        addComponent(btnTest);

        DLCheckBox checkbox1 = new DLCheckBox(20, 45, 100, 20);
        checkbox1.text.set(TextUtils.text("Check A"));
        checkbox1.radioButtonMode.set(true);
        addComponent(checkbox1);
        DLCheckBox checkbox2 = new DLCheckBox(120, 45, 100, 20);
        checkbox2.text.set(TextUtils.text("Check B"));
        checkbox2.radioButtonMode.set(true);
        addComponent(checkbox2);

        cycleButtonTest = new DLCycleButton<>(20, 65, 150, 20);
        for (int i = 0; i < 10; i++) {
            cycleButtonTest.items.add("Test " + i);
        }
        cycleButtonTest.cycling.set(true);
        addComponent(cycleButtonTest);

        DLSlider slider = new DLSlider(20, 90, 114, 20);
        slider.min.set(2D);
        slider.max.set(9D);
        slider.value.set(7D);
        //slider.textFormat.set(DLSlider.DEFAULT_TEXT_DOUBLE_PERCENTAGE_FORMAT);
        addComponent(slider);

        DLScrollBar scrollbarV = new DLScrollBar(20, 115, 100, Orientation.VERTICAL);
        scrollbarV.componentRenderer.set(VanillaListScrollBarRenderer.VANILLA_SCROLLBAR);
        addComponent(scrollbarV);
        
        DLScrollBar scrollbarH = new DLScrollBar(40, 115, 100, Orientation.HORIZONTAL);
        scrollbarH.showButtons.set(true);
        addComponent(scrollbarH);

        DLToggleButton toggleBtn = new DLToggleButton(150, 20, 50, 20);
        toggleBtn.radioButtonMode.set(true);
        addComponent(toggleBtn);
        DLToggleButton toggleBtn2 = new DLToggleButton(200, 20, 50, 20);
        toggleBtn2.radioButtonMode.set(true);
        addComponent(toggleBtn2);
        DLToggleButton toggleBtn3 = new DLToggleButton(250, 20, 50, 20);
        toggleBtn3.radioButtonMode.set(true);
        addComponent(toggleBtn3);


        DLRichTextEditBox textbox = new DLRichTextEditBox(200, 50, 150, 150);
        textbox.multiline.set(true);
        textbox.resizable.set(true);
        textbox.showLineHighlight.set(true);
        textbox.contentPadding.set(new Padding(2));
        textbox.decoratedPadding.set(new Padding(1));
        textbox.acceptAndCancelKeysEnabled.set(true);
        textbox.addEventListener(DLRichTextEditBox.TextAcceptKeyPressedEvent.class, (src, e) -> {
            txt = textbox.text.get().toComponent();
            return false;
        });
        addComponent(textbox);

        DLRichTextEditBox searchBox = new DLRichTextEditBox(100, 200, 80, 16);
        searchBox.readOnly.set(true);
        searchBox.multiline.set(false);
        searchBox.contentPadding.set(new Padding(0, 2, 0, 2));
        searchBox.decoratedPadding.set(new Padding(1));
        searchBox.lineSpacing.set(2);
        searchBox.filterRegex.set("^-?\\d+$");
        addComponent(searchBox);
        
        DLRichTextEditBox autocompleteBox = new DLRichTextEditBox(200, 200, 150, 16);
        autocompleteBox.multiline.set(false);
        autocompleteBox.autocompleteManager.set((DLAutocompleteWindow<String> win, DLRichTextEditBox box) -> {
            List<String> str = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                str.add("Test " + i);
            }
            win.suggestions.set(str);
            win.filter.set(s -> s.toLowerCase().contains(box.text.get().getPlainText().toLowerCase()));
        });
        addComponent(autocompleteBox);
        

        DLNumberPicker number = new DLNumberPicker(100, 225, 80, 20);
        number.min.set(-100D);
        number.step.set(1D);
        number.showButtons.set(false);
        addComponent(number);

        DLProgressBar progressBar = new DLProgressBar(300, 20, 100, 10);
        progressBar.style.set(ProgressBarStyle.CONTINUOUS);
        progressBar.value.set(0.3d);
        addComponent(progressBar);

        DLBasicDataView<String> dataView = new DLBasicDataView<>(2, 2, 200 - 4, 150 - 4);
        dataView.dataSlots.add(new DataSlot("slot1", TextUtils.text("Slot 1"), 50, SizeMode.FIXED));
        dataView.dataSlots.add(new DataSlot("slot2", TextUtils.text("Slot 2"), 20, SizeMode.FIXED));
        dataView.dataSlots.add(new DataSlot("slot3", TextUtils.text("Slot 3"), 70, SizeMode.PERCENTAGE));
        dataView.dataSlots.add(new DataSlot("slot4", TextUtils.text("Slot 4"), 30, SizeMode.PERCENTAGE));
        dataView.items.addAll(List.of("Test 1", "Test 2", "Test 3", "Test 4", "Test 5"));
        dataView.itemBuilder.set((in) -> {
            DLBasicDataView.DLBasicItem<String> item = new DLBasicDataView.DLBasicItem<>(dataView, in);
            DLEditableLabel lbl1 = new DLEditableLabel(0, 0, 1, 20);
            lbl1.text.set(in);
            lbl1.editable.set(true);
            DLEditableLabel lbl2 = new DLEditableLabel(0, 0, 1, 20);
            lbl2.text.set("Test Text A");
            lbl2.editable.set(true);
            DLEditableLabel lbl3 = new DLEditableLabel(0, 0, 1, 20);
            lbl3.text.set("Text B");
            lbl3.editable.set(true);
            DLEditableLabel lbl4 = new DLEditableLabel(0, 0, 1, 20);
            lbl4.text.set("C Text");
            lbl4.editable.set(true);
            item.subComponents.add(new DataSlotComponent("slot1", lbl1));
            item.subComponents.add(new DataSlotComponent("slot2", lbl2));
            item.subComponents.add(new DataSlotComponent("slot3", lbl3));
            item.subComponents.add(new DataSlotComponent("slot4", lbl4));
            return item;
        });
        dataView.resizable.set(true);
        //addComponent(dataView);
        
        DLPanel pnl = new DLPanel(360, 50, 200, 150);
        pnl.backgroundTint.set(DLColor.RED);
        DLPanel pnl2 = new DLPanel(2, 2, 200 - 4, 150 - 4);
        FlowLayout layout = new FlowLayout();
        layout.fillCrossAxis.set(true);
        layout.wrap.set(false);
        layout.verticalGap.set(2);
        layout.flowDirection.set(Direction.VERTICAL);
        pnl2.layout.set(layout);
        for (int i = 0; i < 30; i++) {
            pnl2.addComponent(new DLButton(0, 0, 20, 20));
        }
        pnl.resizable.set(true);
        pnl2.anchor.set(EAlign.values());
        pnl.addComponent(pnl2);
        //addComponent(pnl);
        
        DLItemSelectionBox<String> listBox = new DLItemSelectionBox<>(360, 50, 150, 150);
        listBox.multiselect.set(true);
        listBox.resizable.set(true);
        for (int i = 0; i < 50; i++) {
            listBox.items.add("Test " + i);
        }
        //listBox.selectedItems.set(List.of("Test 5"));
        addComponent(listBox);

        DLComboBox<String> comboBox = new DLComboBox<>(100, 225, 80, 20);
        for (int i = 0; i < 50; i++) {
            comboBox.items.add("Test " + i);
        }
        //comboBox.selectedIndex.set(17);
        addComponent(comboBox);

        
        DLItemPicker<String> picker = new DLItemPicker<>(200, 225, 80, 20);
        for (int i = 0; i < 50; i++) {
            picker.items.add("Test " + i);
        }
        //comboBox.selectedIndex.set(17);
        addComponent(picker);
    }

    @Override
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
/*
        PolygonRenderUtil.drawPolygon(graphics, List.of(
            new Vector2f(20, 200),
            new Vector2f(130, 250),
            new Vector2f(150, 200),
            new Vector2f(120, 200),
            new Vector2f(80, 150),
            new Vector2f(100, 100),
            new Vector2f(90, 30),
            new Vector2f(70, 30),
            new Vector2f(80, 50),
            new Vector2f(70, 60),
            new Vector2f(60, 40)
        ), 0, 0x4400FF00, 0x44FF0000, 5);
        PolygonRenderUtil.drawPolygon(graphics, List.of(
            new Vector2f(60, 40),
            new Vector2f(70, 60),
            new Vector2f(80, 50),
            new Vector2f(70, 30),
            new Vector2f(90, 30),
            new Vector2f(100, 100),
            new Vector2f(80, 150),
            new Vector2f(120, 200),
            new Vector2f(150, 200),
            new Vector2f(130, 250),
            new Vector2f(20, 200)
        ), 0, 0x440000FF, 0x44FFFF00, 5);

        int segments = Math.max(12, (int)(2 * Math.PI * 50 / 4)); 

        //PolygonRenderUtil.drawCircle(graphics, 200, 100, 100, segments, 0xFF0000FF, 0xFFFF0000, 2);
        //PolygonRenderUtil.drawTriangle(graphics, 250, 100, 300, 100, 275, 150, 0xFFFF00FF, 0xFFFF0000, 2);
        //PolygonRenderUtil.drawEllipse(graphics, 200, 200, 20, 10, 20, 0xFFFF00FF, 0xFFFF0000, 2);
        */
    }
    
}
