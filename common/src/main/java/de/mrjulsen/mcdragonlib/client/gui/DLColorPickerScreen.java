package de.mrjulsen.mcdragonlib.client.gui;

import java.util.Locale;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLNumberSelector;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.ColorObject;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DLColorPickerScreen extends DLScreen {

    public static final Component title = TextUtils.translate("gui.dragonlib.colorpicker.title");

    private static final int WIDTH = 250;
    private static final int HEIGHT = 185;
    private static final int SELECTION_W = 9;
    private static final int SELECTION_H = 22;
    private static final int SELECTION_Y = 0;

    private static final int COLOR_PICKER_WIDTH = 180;
      
    private int guiLeft;
    private int guiTop;
    private boolean scrollingH = false, scrollingS = false, scrollingV = false;

    private final Screen lastScreen;
    private final Consumer<ColorObject> result;
    private final int currentColor;
    private final boolean vanillaStyle;

    // color
    private double h = 0, s = 0, v = 0;
    private DLNumberSelector hBox;
    private DLNumberSelector sBox;
    private DLNumberSelector vBox;
    
    private DLNumberSelector rBox;
    private DLNumberSelector gBox;
    private DLNumberSelector bBox;
    
    private DLEditBox colorIntBox;

    // fix
    private boolean rgbNoUpdate = false;
    private boolean initialized = false;

    private Component textHSV = TextUtils.translate("gui.dragonlib.colorpicker.hsv");
    private Component textRGB = TextUtils.translate("gui.dragonlib.colorpicker.rgb");
    private Component textInteger = TextUtils.translate("gui.dragonlib.colorpicker.hex");

    private static final ResourceLocation gui = new ResourceLocation(DragonLib.MODID, "textures/gui/color_picker.png");

    public DLColorPickerScreen(Screen lastScreen, int currentColor, Consumer<ColorObject> result, boolean vanillaLookAndFeel) {
        super(title);
        this.lastScreen = lastScreen;
        this.currentColor = currentColor;
        this.result = result;
        this.vanillaStyle = vanillaLookAndFeel;

        float[] hsv = ColorObject.fromInt(currentColor).toHSV();
        this.h = hsv[0];
        this.s = hsv[1];
        this.v = hsv[2];
    }

    @Override
    public void onClose() {
        if (lastScreen != null) {            
            this.minecraft.setScreen(this.lastScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        initialized = false;
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - (HEIGHT + 24) / 2;

        DLButton btn1 = addButton(guiLeft + WIDTH - 8 - 160 - 4, guiTop + HEIGHT - 28, 80, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        DLButton btn2 = addButton(guiLeft + WIDTH - 8 - 80, guiTop + HEIGHT - 28, 80, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);
        
        if (!vanillaStyle) {            
            btn1.setRenderStyle(AreaStyle.DRAGONLIB);
            btn1.setBackColor(DragonLib.PRIMARY_BUTTON_COLOR);
            btn2.setRenderStyle(AreaStyle.DRAGONLIB);
        }


        this.hBox = addRenderableWidget(new DLNumberSelector(guiLeft + 196, guiTop + 40, 46, 18, h * 360, false,
        (box, val) -> {
            h = val / 360.0D;
        }) {
            @Override
            public void onFocusChangeEvent(boolean focus) {
                super.onFocusChangeEvent(focus);
                if (!focus) {
                    updateInputBoxes();
                }
            }
        });
        this.hBox.setNumberBounds(0, 360);

        this.sBox = addRenderableWidget(new DLNumberSelector(guiLeft + 196, guiTop + 66, 46, 18, s * 100, false,
        (box, val) -> {
            s = val / 100.0D;
        }) {
            @Override
            public void onFocusChangeEvent(boolean focus) {
                super.onFocusChangeEvent(focus);
                if (!focus) {
                    updateInputBoxes();
                }
            }
        });
        this.sBox.setNumberBounds(0, 100);

        this.vBox = addRenderableWidget(new DLNumberSelector(guiLeft + 196, guiTop + 92, 46, 18, v * 100, false,
        (box, val) -> {
            v = val / 100.0D;
        }) {
            @Override
            public void onFocusChangeEvent(boolean focus) {
                super.onFocusChangeEvent(focus);
                if (!focus) {
                    updateInputBoxes();
                }
            }
        });
        this.vBox.setNumberBounds(0, 100);

        rgbNoUpdate = true;

        this.rBox = addRenderableWidget(new DLNumberSelector(guiLeft + 49, guiTop + 114, 34, 18, 0, false,
        (box, val) -> {
            if (rgbNoUpdate) {
                return;
            }
            ColorObject c = new ColorObject(val.intValue(), gBox.getAsInt(), bBox.getAsInt());
            float[] hsv = c.toHSV();
            h = hsv[0];
            s = hsv[1];
            v = hsv[2];
        }) {
            @Override
            public void onFocusChangeEvent(boolean focus) {
                super.onFocusChangeEvent(focus);
                if (!focus) {
                    updateInputBoxes();
                }
            }
        });
        this.rBox.setNumberBounds(0, 255);

        this.gBox = addRenderableWidget(new DLNumberSelector(guiLeft + 50 + 31, guiTop + 114, 34, 18, 0, false,
        (box, val) -> {
            if (rgbNoUpdate) {
                return;
            }
            ColorObject c = new ColorObject(rBox.getAsInt(), val.intValue(), bBox.getAsInt());
            float[] hsv = c.toHSV();
            h = hsv[0];
            s = hsv[1];
            v = hsv[2];
        }) {
            @Override
            public void onFocusChangeEvent(boolean focus) {
                super.onFocusChangeEvent(focus);
                if (!focus) {
                    updateInputBoxes();
                }
            }
        });
        this.gBox.setNumberBounds(0, 255);

        this.bBox = addRenderableWidget(new DLNumberSelector(guiLeft + 50 + 63, guiTop + 114, 34, 18, 0, false,
        (box, val) -> {
            if (rgbNoUpdate) {
                return;
            }
            ColorObject c = new ColorObject(rBox.getAsInt(), gBox.getAsInt(), val.intValue());
            float[] hsv = c.toHSV();
            h = hsv[0];
            s = hsv[1];
            v = hsv[2];
        }) {
            @Override
            public void onFocusChangeEvent(boolean focus) {
                super.onFocusChangeEvent(focus);
                if (!focus) {
                    updateInputBoxes();
                }
            }
        });
        this.bBox.setNumberBounds(0, 255);

        this.colorIntBox = addEditBox(
            guiLeft + 50, guiTop + 135, 48, 16,
            "0", TextUtils.empty(), true,
            (x) -> {
                if (rgbNoUpdate) {
                    return;
                }
                ColorObject c = ColorObject.fromInt((int)Long.parseLong(nullCheck(x), 16));
                float[] hsv = c.toHSV();
                h = hsv[0];
                s = hsv[1];
                v = hsv[2];
            },
            (box, focusLost) -> {
                if (focusLost) {
                    updateInputBoxes();
                }
            }, null
        );
        this.colorIntBox.setFilter(this::editBoxHexFilter);
        this.colorIntBox.setMaxLength(6);

        initialized = true;
        
        this.updateInputBoxes();
        rgbNoUpdate = false;
    }

    private String nullCheck(String in) {
        return in == null || in.isEmpty() || in.equals("-") ? "0" : in;
    }

    private boolean editBoxHexFilter(String input) {
        if (input.isEmpty())
            return true;

        try {
            Integer.parseInt(input, 16);
			return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void onDone() {
        this.result.accept(ColorObject.fromHSV(h, s, v));
        this.onClose();
    }

    @Override
    public void renderBackLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {

        renderBackground(graphics.poseStack(), 0);

        //GuiUtils.drawTexture(gui, graphics, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIDTH, HEIGHT, vanillaStyle ? 0xFFFFFFFF : DragonLib.DARK_WINDOW_COLOR, vanillaStyle);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + 196, guiTop + 9, 46, 26, vanillaStyle ? 0xFFFFFFFF : DragonLib.DARK_WINDOW_COLOR, AreaStyle.GRAY, ButtonState.DOWN);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + 8, guiTop + 40, 182, 18, vanillaStyle ? 0xFFFFFFFF : DragonLib.DARK_WINDOW_COLOR, AreaStyle.GRAY, ButtonState.DOWN);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + 8, guiTop + 66, 182, 18, vanillaStyle ? 0xFFFFFFFF : DragonLib.DARK_WINDOW_COLOR, AreaStyle.GRAY, ButtonState.DOWN);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + 8, guiTop + 92, 182, 18, vanillaStyle ? 0xFFFFFFFF : DragonLib.DARK_WINDOW_COLOR, AreaStyle.GRAY, ButtonState.DOWN);

        for (int i = 0; i < COLOR_PICKER_WIDTH; i++) {
            ColorObject ch = getH(i, COLOR_PICKER_WIDTH);
            ColorObject cs = getS(h, i, COLOR_PICKER_WIDTH);
            ColorObject cv = getV(h, i, COLOR_PICKER_WIDTH);
            GuiUtils.fill(graphics, guiLeft + 9 + i, guiTop + 41, 1, 16, ch.toInt());
            GuiUtils.fill(graphics, guiLeft + 9 + i, guiTop + 67, 1, 16, cs.toInt());
            GuiUtils.fill(graphics, guiLeft + 9 + i, guiTop + 93, 1, 16, cv.toInt());

        }
        
        // Preview
        GuiUtils.fill(graphics, guiLeft + 197, guiTop + 10, 22, 24, ColorObject.fromHSV(h, s, v).toInt());
        GuiUtils.fill(graphics, guiLeft + 197 + 22, guiTop + 10, 22, 24, currentColor);

        String title = getTitle().getString();
        GuiUtils.drawString(graphics, font, guiLeft + 9, guiTop + 28, textHSV, vanillaStyle ? DragonLib.NATIVE_UI_FONT_COLOR : DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, TextUtils.text(title), vanillaStyle ? DragonLib.NATIVE_UI_FONT_COLOR : DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, guiLeft + 9, guiTop + 119, textRGB, vanillaStyle ? DragonLib.NATIVE_UI_FONT_COLOR : DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, guiLeft + 9, guiTop + 139, textInteger, vanillaStyle ? DragonLib.NATIVE_UI_FONT_COLOR : DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);

        // Draw selections
        GuiUtils.drawTexture(gui, graphics, guiLeft + 5 + (int)(h * COLOR_PICKER_WIDTH), guiTop + 38, SELECTION_W, SELECTION_H, inSliderH(mouseX, mouseY) ? SELECTION_W : 0, SELECTION_Y, SELECTION_W, SELECTION_H, 32, 32);
        GuiUtils.drawTexture(gui, graphics, guiLeft + 5 + (int)(s * COLOR_PICKER_WIDTH), guiTop + 64, SELECTION_W, SELECTION_H, inSliderS(mouseX, mouseY) ? SELECTION_W : 0, SELECTION_Y, SELECTION_W, SELECTION_H, 32, 32);
        GuiUtils.drawTexture(gui, graphics, guiLeft + 5 + (int)(v * COLOR_PICKER_WIDTH), guiTop + 90, SELECTION_W, SELECTION_H, inSliderV(mouseX, mouseY) ? SELECTION_W : 0, SELECTION_Y, SELECTION_W, SELECTION_H, 32, 32);

        super.renderBackLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        
        if (button == 0 && inSliderH(mouseX, mouseY)){            
            scrollingH = true;
            this.setH(setMouseValue(mouseX));
        } else if (button == 0 && inSliderS(mouseX, mouseY)){            
            scrollingS = true;
            this.setS(setMouseValue(mouseX));
        } else if (button == 0 && inSliderV(mouseX, mouseY)){            
            scrollingV = true;
            this.setV(setMouseValue(mouseX));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isDown()) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {

        if (pButton == 0) {
            this.scrollingH = false;
            this.scrollingS = false;
            this.scrollingV = false;
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

        double fh = 0;
        if (this.scrollingH || this.scrollingS || this.scrollingV) {
            fh = setMouseValue(pMouseX);
        }

        if (this.scrollingH) {
            this.setH(fh);
            return true;
        } else if (this.scrollingS) {
            this.setS(fh);
            return true;
        } else if (this.scrollingV) {
            this.setV(fh);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    private void setH(double d) {
        this.h = MathUtils.clamp(d, 0d, 1d);
        this.updateInputBoxes();
    }

    private void setS(double d) { 
        this.s = MathUtils.clamp(d, 0d, 1d);
        this.updateInputBoxes();
    }

    private void setV(double d) {
        this.v = MathUtils.clamp(d, 0d, 1d);
        this.updateInputBoxes();
    }

    private void updateInputBoxes() {
        if (!initialized) {
            return;
        }
        rgbNoUpdate = true;
        this.hBox.setValue(h * 360, true);
        this.sBox.setValue(s * 100, true);
        this.vBox.setValue(v * 100, true);

        ColorObject c = ColorObject.fromHSV(h, s, v);
        this.rBox.setValue(c.getR(), true);
        this.gBox.setValue(c.getG(), true);
        this.bBox.setValue(c.getB(), true);
        this.colorIntBox.setValue(convertToHex(c.toInt()));
        rgbNoUpdate = false;
    }

    private static String convertToHex(int rgbValue) {
        String hexString = Integer.toHexString(rgbValue & 0xFFFFFF);
        while (hexString.length() < 6) {
            hexString = "0" + hexString;
        }
        return hexString.toUpperCase(Locale.ENGLISH);
    }

    private double setMouseValue(double mouseX) {
        return (mouseX - (double)(this.guiLeft + 9)) / (double)(COLOR_PICKER_WIDTH - 1);
    }

    public static ColorObject getH(int i, int w) {
        float hue = (float) i / w;
        return ColorObject.fromHSV(hue, 1, 1);
    }

    public static ColorObject getS(double h, int i, int w) {
        float hue = (float) i / w;
        return ColorObject.fromHSV(h, hue, 1);
    }

    public static ColorObject getV(double h, int i, int w) {
        float hue = (float) i / w;
        return ColorObject.fromHSV(h, 1, hue);
    }

    protected boolean inSliderH(double mouseX, double mouseY) {
        int x = 9;
        int y = 42;
        int w = 180;
        int h = 16;

        int x1 = guiLeft + x;
        int y1 = guiTop + y;
        int x2 = x1 + w;
        int y2 = y1 + h;

        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    protected boolean inSliderS(double mouseX, double mouseY) {
        int x = 9;
        int y = 68;
        int w = 180;
        int h = 16;

        int x1 = guiLeft + x;
        int y1 = guiTop + y;
        int x2 = x1 + w;
        int y2 = y1 + h;

        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    protected boolean inSliderV(double mouseX, double mouseY) {
        int x = 9;
        int y = 94;
        int w = 180;
        int h = 16;

        int x1 = guiLeft + x;
        int y1 = guiTop + y;
        int x2 = x1 + w;
        int y2 = y1 + h;

        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }
}

