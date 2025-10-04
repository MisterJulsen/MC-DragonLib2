package de.mrjulsen.mcdragonlib.client.newgui.test;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLCheckBox;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLComboBox;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLItemSelectionBox;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLProgressBar;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLSlider;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLProgressBar.ProgressBarStyle;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.render.VanillaListScrollBarRenderer;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.PolygonRenderUtil;
import de.mrjulsen.mcdragonlib.client.util.ShapeRenderer;
import de.mrjulsen.mcdragonlib.client.util.Triangulator;
import de.mrjulsen.mcdragonlib.client.util.PolygonRenderUtil.OutlineMode;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

public class DLTestWindow extends DLWindow {

    private DLButton btnTest;
    private DLCheckBox chbTest;
    private DLCycleButton<String> cycleButtonTest;
    private DLPanel panel;

    private Component txt = TextUtils.empty();

    

    public DLTestWindow(DLWindowManager manager) {
        super(manager);
        anchor.set(EAlign.values());

        DLContextMenu contextMenu = new DLContextMenu((x, y) -> {
            List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
            entries.add(new DLContextMenu.ItemEntry(TextUtils.text("Item 1"), Sprite.empty(), true, () -> {}, (pX, pY) -> {
                return List.of(new DLContextMenu.ItemEntry(TextUtils.text("Testitem 435"), Sprite.empty(), true, () -> {}, null));
            }));
            entries.add(new DLContextMenu.ItemEntry(TextUtils.text("Item 2"), Sprite.empty(), false, () -> {}, null));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(TextUtils.text("item 3"), Sprite.empty(), true, () -> {}, (pX, pY) -> entries));
            return entries;
        });

        btnTest = new DLButton(20, 20);
        btnTest.textAlignment.set(EAlignment.CENTER);
        btnTest.iconAlignment.set(EAlignment.CENTER);
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

        DLSlider slider = new DLSlider(20, 90, 150, 20);
        slider.max.set(1D);
        slider.step.set(1D / 100D);
        slider.textFormat.set(DLSlider.DEFAULT_TEXT_DOUBLE_PERCENTAGE_FORMAT);
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
        textbox.enableEnterAcceptKey.set(true);
        textbox.addEventListener(DLGuiCommonEvents.TextAcceptKeyPressedEvent.class, (src, e) -> {
            txt = textbox.text.get().toComponent();
            return false;
        });
        addComponent(textbox);

        DLRichTextEditBox searchBox = new DLRichTextEditBox(100, 200, 150, 16);
        searchBox.readOnly.set(true);
        searchBox.multiline.set(false);
        searchBox.contentPadding.set(new Padding(0, 2, 0, 2));
        searchBox.decoratedPadding.set(new Padding(1));
        searchBox.lineSpacing.set(2);
        searchBox.filterRegex.set("^-?\\d+$");
        addComponent(searchBox);

        DLNumberPicker number = new DLNumberPicker(100, 225, 80, 20);
        number.min.set(-100D);
        number.step.set(1D);
        number.showButtons.set(false);
        addComponent(number);

        DLProgressBar progressBar = new DLProgressBar(300, 20, 100, 10);
        progressBar.style.set(ProgressBarStyle.CONTINUOUS);
        progressBar.value.set(0.3d);
        addComponent(progressBar);
        
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
    }

    @Override
    public void renderFrontLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        ShapeRenderer renderer = new ShapeRenderer(graphics.graphics());
        GuiUtils.drawString(graphics, Minecraft.getInstance().font, 10, 90, txt, 0xFFFFFFFF, EAlignment.LEFT, false);

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

        PolygonRenderUtil.drawCircle(graphics, 200, 100, 100, segments, 0xFF0000FF, 0xFFFF0000, 2);
        PolygonRenderUtil.drawTriangle(graphics, 250, 100, 300, 100, 275, 150, 0xFFFF00FF, 0xFFFF0000, 2);
        PolygonRenderUtil.drawEllipse(graphics, 200, 200, 20, 10, 20, 0xFFFF00FF, 0xFFFF0000, 2);
    }
    
}
