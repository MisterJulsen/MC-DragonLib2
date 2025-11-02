package de.mrjulsen.mcdragonlib.client.gui.test;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import de.mrjulsen.mcdragonlib.client.gui.builtin.DLColorPickerWindow;
import de.mrjulsen.mcdragonlib.client.gui.container.DLSlot;
import de.mrjulsen.mcdragonlib.client.gui.container.TestContainerMenu;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLMenuWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.time.ConfiguredTimeSystem;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;
import de.mrjulsen.mcdragonlib.util.time.TimeZone;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat24Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatDigitalDuration;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatISO8601;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatRFC3339;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatTicks;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormaturVerboseDuration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class RedWindow extends DLMenuWindow<TestContainerMenu> {

    public RedWindow(DLWindowManager manager) {
        super(TestContainerMenu.class, manager);
        movable.set(true);
        setPosition(50, 30);
        setSize(200, 200);
        
        DLButton btn = new DLButton(width() - 20, 0, 20, 20);
        btn.text.set(TextUtils.text("×"));
        btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            manager.closeWindow(this);
            return false;
        });
        addComponent(btn);

        DLButton btn2 = new DLButton(width() - 40, 0, 20, 20);
        btn2.text.set(TextUtils.text("⬜"));
        btn2.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> {
                DLColorPickerWindow z = new DLColorPickerWindow(mgr, false, DLColor.UNDEFINED, (c) -> {});                
                return z;
            });
            DLOverlayManager.addOverlay(mgr -> new DLTestWindow(mgr));
            return false;
        });
        addComponent(btn2);

        DLButton btn3 = new DLButton(width() - 60, 0, 20, 20);
        btn3.text.set(TextUtils.text("—"));
        btn3.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            if (Minecraft.getInstance().level != null) {
                getWindowManager().createWindow(mgr -> {
                    RedWindow z = new RedWindow(mgr);
                    return z;
                });
            }
            return false;
        });
        addComponent(btn3);

        if (Minecraft.getInstance().player != null) {
            for (int a = 0; a < 4; a++) {
                for (int i = 0; i < 9; i++) {
                    System.out.println(menu + ": " + menu.slots.get(i).getItem().getItem());
                    DLSlot slot = new DLSlot(10 + (i * 18), 10 + (a * 18), 18, 18, menu.slots.get(a * 9 + i), menu);
                    addComponent(slot);
                }
            }
        }
    }

    

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite("window_rounded").render(graphics, 0, 0, width(), height());

        Level level = Minecraft.getInstance().level;
        if (level != null) {
            ITimeSystem provider = new ConfiguredTimeSystem();
            ITimeSystem v = new ITimeSystem() {                
                @Override
                public long getTicksPerDay() {
                    return 24000L;
                }

                @Override
                public List<TimeZone> getTimeZones() {
                    return List.of(new TimeZone(0, getTicksPerDay(), 24000D / 1440D));
                }
            };
            DLTime time = DLTime.fromReal(DOUBLE_CLICK_COUNT, MULTI_CLICK_SPEED_MS, MOUSE_DRAG_THRESHOLD, MOUSE_DOWN_INITIAL_DELAY, DOUBLE_CLICK_COUNT, provider);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 0, "Daytime: " + level.getDayTime(), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 1, "Time: " + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormatTicks(), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 2, "Time: " + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormat24Hours(), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 3, "Time: " + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormat24Hours(), TimeContext.REAL), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 4, "Time (+1h): " + DLTime.fromTicks(level.getDayTime(), provider).addTicks(1000, v).format(new TimeFormat24Hours(), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 6, "" + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormaturVerboseDuration(null, true, true, true, true, true), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 7, "" + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormaturVerboseDuration(null, true, true, true, true, true), TimeContext.REAL), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 8, "" + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormatDigitalDuration(DLTime.fromTicks(12000, provider), false, false, true, true, false), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 9, "" + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormatDigitalDuration(DLTime.fromTicks(12000, provider), false, true, true, false, false), TimeContext.REAL), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 11, "" + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormatISO8601(), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, 10 + graphics.defaultFont().lineHeight * 12, "" + DLTime.fromTicks(level.getDayTime(), provider).format(new TimeFormatRFC3339(), TimeContext.INGAME), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        }

        /*
        GuiUtils.renderItem(graphics, new ItemStack(Items.DIAMOND, 5), 2, 2, 2, true);

        Quaternionf quat = new Quaternionf();
        quat.rotateXYZ((float)Math.toRadians(((double)System.currentTimeMillis() / 20d) % 360), (float)Math.toRadians(((double)System.currentTimeMillis() / 20d) % 360), (float)Math.toRadians(((double)System.currentTimeMillis() / 20d) % 360));
        Vector3f pivot = new Vector3f(-0.5f);

        Matrix4f matrix = new Matrix4f()
            .translate(pivot.negate(new Vector3f()))
            .rotate(quat)
            .translate(pivot);
        
        GuiUtils.renderBlockState(graphics, 102, 52, 2, Blocks.CRAFTING_TABLE.defaultBlockState(), RenderType.solid(), matrix, LightTexture.FULL_BRIGHT);

        if (Minecraft.getInstance().player != null) {
            GuiUtils.renderEntityFollowingMouse(graphics, 50, 100, 2, (float)getWindowManager().mouseXOnScreen(), (float)getWindowManager().mouseYOnScreen(), Minecraft.getInstance().player, LightTexture.FULL_BRIGHT);
        }
            */
    }    
    
}
