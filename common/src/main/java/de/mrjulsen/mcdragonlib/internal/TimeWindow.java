package de.mrjulsen.mcdragonlib.internal;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.DLTimeUnit;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;
import de.mrjulsen.mcdragonlib.util.time.TimeZone;
import de.mrjulsen.mcdragonlib.util.time.VanillaTimeSystem;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat12Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat24Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatDigitalDuration;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatTicks;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatVerboseDuration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class TimeWindow extends DLWindow {

    public TimeWindow(DLWindowManager manager) {
        super(manager);
        movable.set(true);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());

        GuiUtils.renderItem(graphics, new ItemStack(Blocks.LECTERN.asItem()), width() - 20, 5);

        ITimeSystem system = DLTime.defaultTimeSystem();
        DLTime time = new DLTime(Minecraft.getInstance().level, system);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 0, time.format(TimeFormat24Hours.INSTANCE, TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 1, time.format(TimeFormat12Hours.INSTANCE, TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 2, time.format(TimeFormatTicks.INSTANCE, TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 5, "Game: " + time.format(new TimeFormatDigitalDuration(DLTimeUnit.MILLIS, true), TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 6, "Real:  " + time.format(new TimeFormatDigitalDuration(DLTimeUnit.MILLIS, true), TimeContext.REAL, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 7, "Game: " + time.format(TimeFormatVerboseDuration.DEFAULT_INSTANCE, TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 8, "Real:  " + time.format(TimeFormatVerboseDuration.DEFAULT_INSTANCE, TimeContext.REAL, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 10, "(+1h) Game:  " + time.add(DLTime.fromGameHours(1, VanillaTimeSystem.INSTANCE)).format(TimeFormatVerboseDuration.DEFAULT_INSTANCE, TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 11, "(+1h) Real:  " + time.add(DLTime.fromGameHours(1, VanillaTimeSystem.INSTANCE)).format(TimeFormatVerboseDuration.DEFAULT_INSTANCE, TimeContext.REAL, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 13, "(-1h) Game:  " + time.sub(DLTime.fromGameHours(1, VanillaTimeSystem.INSTANCE)).format(TimeFormatVerboseDuration.DEFAULT_INSTANCE, TimeContext.INGAME, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 14, "(-1h) Real:  " + time.sub(DLTime.fromGameHours(1, VanillaTimeSystem.INSTANCE)).format(TimeFormatVerboseDuration.DEFAULT_INSTANCE, TimeContext.REAL, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
    
    
    public static class TestTimeSystem implements ITimeSystem {
        private static final List<TimeZone> VANILLA_ZONES = List.of(
            new TimeZone(0, 12000, 20D),
            new TimeZone(12000, 24000, 80D)
        );

        private TestTimeSystem() {}
        
        @Override
        public long getTicksPerDay() {
            return 24000L;
        }

        @Override
        public List<TimeZone> getTimeZones() {
            return VANILLA_ZONES;
        }
    }
}
