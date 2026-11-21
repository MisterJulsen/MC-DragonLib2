/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.client.gui.v2.widget.*;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.client.gui.v2.SizeMode;
import de.mrjulsen.mcdragonlib.client.gui.v2.input.MoveHandle;
import de.mrjulsen.mcdragonlib.client.gui.v2.input.ResizeHandle;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLColorPickerWindow;
import de.mrjulsen.mcdragonlib.client.gui.v2.window.DLWindow;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class TestGui extends DLWindow {
    public TestGui() {
        super(1, 1);
        fullscreen.set(true);

        DLProgressBar bar = new DLProgressBar(250, 50, 100, 10);
        bar.style.set(DLProgressBar.ProgressBarStyle.CONTINUOUS);
        bar.value.set(0.67d);
        addComponent(bar);

        DLSlider slider = new DLSlider(250, 80, 100, 20);
        slider.step.set(0.5);
        slider.textFormat.set(DLSlider.TEXT_FORMAT_DOUBLE_PERCENTAGE);
        addComponent(slider);

        DLTextBox textbox = new DLTextBox(250, 110, 100, 20);
        addComponent(textbox);

        DLScrollBar hsb = new DLScrollBar(280, 140, 100, DLScrollBar.Orientation.HORIZONTAL);
        hsb.showButtons.set(true);
        hsb.thumbSize.set(5);
        addComponent(hsb);

        DLScrollBar vsb = new DLScrollBar(370, 20, 150, DLScrollBar.Orientation.VERTICAL);
        vsb.showButtons.set(true);
        vsb.overrideThemeKey(DLScrollBar.THEME_KEY_RENDERER, DLScrollBar.Renderer.SIMPLE);
        vsb.thumbSize.set(5);
        addComponent(vsb);

        DLListBox<String> lb = new DLListBox<>(400, 20, 150, 150);
        lb.items.addAll("Banane", "Apfel", "Birne", "Salz");
        lb.checkMode.set(DLListBox.CheckMode.MULTIPLE);
        lb.showIcons.set(true);
        lb.icons.set(i -> new DLSprite(new ItemStack(Items.DIAMOND.asItem()), 12, false));
        for (int i = 0; i < 10; i++) {
            lb.items.add("Item #" + i);
        }
        addComponent(lb);

        DLColorPicker color = new DLColorPicker(400, 190, 150, 100);
        color.showAlphaSlider.set(true);
        color.sliderPosition.set(DLColorPicker.SliderPosition.LEFT);
        addComponent(color);

        DLButton btn = new DLButton(20, 20, 150, 20);
        btn.text.set(TextUtils.text("A Button"));
        btn.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn.iconAlignment.set(ETextAlignment.LEFT);
        btn.textAlignment.set(ETextAlignment.LEFT);
        btn.addBehaviour(new MoveHandle());
        btn.addBehaviour(new ResizeHandle());
        btn.drawFontShadow.set(false);

        addComponent(btn);

        DLButton btn2 = new DLButton(20, 40, 150, 20);
        btn2.text.set(TextUtils.text("Dragon's Button"));
        btn2.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn2.iconAlignment.set(ETextAlignment.CENTER);
        btn2.textAlignment.set(ETextAlignment.LEFT);
        addComponent(btn2);
        btn2.addEventListener(ClickEvent.class, (s, e) -> {
            getWindowManager().openModal(new DLColorPickerWindow(false, DLColor.RED, (c) -> {}));
        });

        DLButton btn3 = new DLButton(20, 60, 150, 20);
        btn3.text.set(TextUtils.text("Test Btn"));
        btn3.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn3.overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.FLAT_GRAY_RENDERER);
        btn3.iconAlignment.set(ETextAlignment.RIGHT);
        btn3.textAlignment.set(ETextAlignment.LEFT);
        addComponent(btn3);


        DLButton btn4 = new DLButton(20, 80, 150, 20);
        btn4.text.set(TextUtils.text("Click Me!"));
        btn4.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn4.overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.FLAT_BROWN_RENDERER);
        btn4.iconAlignment.set(ETextAlignment.LEFT);
        btn4.textAlignment.set(ETextAlignment.CENTER);
        addComponent(btn4);

        DLButton btn5 = new DLButton(20, 100, 150, 20);
        btn5.text.set(TextUtils.text("Test with Test"));
        btn5.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn5.overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.FLAT_RED_RENDERER);
        btn5.iconAlignment.set(ETextAlignment.CENTER);
        btn5.textAlignment.set(ETextAlignment.CENTER);
        addComponent(btn5);

        DLButton btn6 = new DLButton(20, 120, 150, 20);
        btn6.text.set(TextUtils.text("Test"));
        btn6.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn6.overrideThemeKey(DLAbstractButton.THEME_KEY_RENDERER, DLAbstractButton.VANILLA_LEGACY_RENDERER);
        btn6.iconAlignment.set(ETextAlignment.RIGHT);
        btn6.textAlignment.set(ETextAlignment.CENTER);
        addComponent(btn6);


        DLButton btn7 = new DLButton(20, 140, 150, 20);
        btn7.text.set(TextUtils.text("Yeah"));
        btn7.icon.set(new DLSprite(new ItemStack(Blocks.CRAFTING_TABLE.asItem()), 16, true));
        btn7.iconAlignment.set(ETextAlignment.LEFT);
        btn7.textAlignment.set(ETextAlignment.RIGHT);
        addComponent(btn7);

        DLToggleButton tbtn1 = new DLToggleButton(20, 170, 80, 20);
        tbtn1.addBehaviour(new ResizeHandle());
        tbtn1.text.set(TextUtils.empty());
        tbtn1.icon.set((itm) -> {
            if (itm) {
                return new DLSprite(new ItemStack(Blocks.LIME_CONCRETE.asItem()), 16, true);
            }
            return new DLSprite(new ItemStack(Blocks.BARRIER.asItem()), 16, true);
        });
        addComponent(tbtn1);

        DLToggleButton tbtn2 = new DLToggleButton(100, 170, 80, 20);
        tbtn2.text.set(TextUtils.text("test 4"));
        tbtn2.radioButtonMode.set(true);
        tbtn2.textAlignment.set(ETextAlignment.LEFT);
        addComponent(tbtn2);

        DLToggleButton tbtn3 = new DLToggleButton(180, 170, 80, 20);
        tbtn3.text.set(TextUtils.text("test 5"));
        tbtn3.addBehaviour(new ResizeHandle());
        tbtn3.radioButtonMode.set(true);
        tbtn3.textAlignment.set(ETextAlignment.CENTER);
        addComponent(tbtn3);

        DLToggleButton tbtn4 = new DLToggleButton(260, 170, 80, 20);
        tbtn4.text.set(TextUtils.text("test 6"));
        tbtn4.radioButtonMode.set(true);
        tbtn4.textAlignment.set(ETextAlignment.RIGHT);
        tbtn4.addEventListener(DLAbstractToggleButton.CheckedChangedEvent.class, (s, e) -> {
            tbtn1.enabled.set(e.checked());
        });
        addComponent(tbtn4);

        DLCycleButton<Boolean> tbtn5 = DLCycleButton.createOnOffButton(350, 170, 80, 20);
        tbtn5.text.set(TextUtils.text("CycleButton"));
        tbtn2.addEventListener(ClickEvent.class, (s, e) -> {
            tbtn5.items.add(true);
        });
        addComponent(tbtn5);

        DLCycleButton<String> btn9 = new DLCycleButton<>(20, 200, 150, 20);
        btn9.icon.set((itm) -> {
            if (itm.isEmpty()) {
                return null;
            }
            return itm.map(x -> {
                if (x.equals("Dragon")) {
                    return new DLSprite(new ItemStack(Blocks.DRAGON_EGG.asItem()), 16, true);
                }
                return null;
            }).orElse(null);
        });
        btn9.text.set(TextUtils.text("Selection"));
        btn9.items.addAll("Item 1", "Dragon", "Item 3", "Item 4");
        btn9.selectedItem.set(Optional.of("Dragon"));
        btn9.widthMode.set(SizeMode.AUTO);
        btn9.heightMode.set(SizeMode.AUTO);
        addComponent(btn9);

        DLNumberPicker num = new DLNumberPicker(200, 200, 80, 20);
        num.min.set(-100d);
        addComponent(num);

        DLItemPicker<String> items = new DLItemPicker<>(300, 200, 80, 20);
        items.items.addAll("Item 1", "Item 2", "Item 3", "Item 4");
        addComponent(items);

        DLComboBox<String> combobox = new DLComboBox<>(200, 230, 80, 20);
        combobox.items.addAll("Item 1", "Item 2", "Item 3", "Item 4");
        for (int i = 0; i < 100; i++) {
            combobox.items.add("Item #" + i);
        }
        addComponent(combobox);

        DLCheckBox chk = new DLCheckBox(20, 230, 150, 20);
        chk.text.set(TextUtils.text("Test mit sehr langem Text mit vielen Wörtern und Zeichen und Buchstaben"));
        chk.addBehaviour(new ResizeHandle());
        chk.textWrap.set(false);
        addComponent(chk);

        DLCheckBox chk2 = new DLCheckBox(20, 250, 150, 20);
        chk2.text.set(TextUtils.text("Dragon A"));
        chk2.radioButtonMode.set(true);
        addComponent(chk2);

        DLCheckBox chk3 = new DLCheckBox(20, 270, 150, 20);
        chk3.text.set(TextUtils.text("Dragon B"));
        chk3.radioButtonMode.set(true);
        chk3.enabled.set(false);
        addComponent(chk3);
    }

    @Override
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
        GuiUtils.entityFollowingMouse(g, Minecraft.getInstance().player, 200, 50);
        GuiUtils.shadow(g, 200, 100, 16, 16, 8, DLColor.BLACK.withAlpha(128));
        GuiUtils.blockState(g, Blocks.DIAMOND_BLOCK.defaultBlockState(), 200, 100, RenderType.solid());
    }
}
