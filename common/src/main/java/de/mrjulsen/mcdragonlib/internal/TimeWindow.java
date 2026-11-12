package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.time.GameDuration;
import de.mrjulsen.mcdragonlib.time.GameTime;
import de.mrjulsen.mcdragonlib.time.GameTimeUnit;
import de.mrjulsen.mcdragonlib.time.TimeSystem;
import de.mrjulsen.mcdragonlib.time.TimeSystems;
import de.mrjulsen.mcdragonlib.time.format.ClockFormat;
import de.mrjulsen.mcdragonlib.time.format.DurationFormat;
import de.mrjulsen.mcdragonlib.time.format.TimeParts.Field;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;

public class TimeWindow extends DLWindow {

    private static final DurationFormat DIGITAL_MILLIS = DurationFormat.digital(Field.DAYS, Field.SECONDS);

    public static final TimeSystem TEST_TIME_SYSTEM = TimeSystem.builder(24000L)
        .section(0, 12000, 10D)
        .section(12000, 24000, 80D)
        .build();

    public static final TimeSystem REAL_LIFE_TIME_SYSTEM = TimeSystem.builder(24000L).defaultSpeed(24000d / 86400d).build();

    public TimeWindow(DLWindowManager manager) {
        super(manager);
        movable.set(true);

        addEventListener(DLGuiStandardEvents.KeyPressEvent.class, (s, e) -> {
            if (e.keyCode() == GLFW.GLFW_KEY_K) {
                getWindowManager().closeWindow(this);
            }
            return true;
        });
    }

    @Override
    public Rectangle getRenderBounds() {
        return Rectangle.withSize(x(), y(), width() + 20, height() + 20);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (!isFocused()) {
            GuiUtils.setTint(DLColor.fromInt(0xFFAAAAAA));
        }
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.resetTint();

        GuiUtils.renderItem(graphics, new ItemStack(Blocks.LECTERN.asItem()), width() - 20, 5);

        TimeSystem system = REAL_LIFE_TIME_SYSTEM;//TimeSystems.current();
        GameTime time = GameTime.now();
        GameTime plusHour = time.plusReal(GameDuration.of(1, GameTimeUnit.HOURS, system).toReal(time), system);
        GameTime minusHour = time.plusReal(GameDuration.of(-1, GameTimeUnit.HOURS, system).toReal(time), system);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 0, time.format(ClockFormat.H24, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 1, time.format(ClockFormat.H12, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 2, (long)time.ticks() + "t", DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 5, "Game: " + sinceOrigin(time).format(DIGITAL_MILLIS, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 6, "Real:  " + sinceOrigin(time).formatReal(DIGITAL_MILLIS, GameTime.ZERO, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 7, "Game: " + sinceOrigin(time).format(DurationFormat.VERBOSE, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 8, "Real:  " + sinceOrigin(time).formatReal(DurationFormat.VERBOSE, GameTime.ZERO, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 10, "(+1h) Game:  " + sinceOrigin(plusHour).format(DurationFormat.VERBOSE, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 11, "(+1h) Real:  " + sinceOrigin(plusHour).formatReal(DurationFormat.VERBOSE, GameTime.ZERO, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 13, "(-1h) Game:  " + sinceOrigin(minusHour).format(DurationFormat.VERBOSE, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 14, "(-1h) Real:  " + sinceOrigin(minusHour).formatReal(DurationFormat.VERBOSE, GameTime.ZERO, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        GuiUtils.drawString(graphics, graphics.defaultFont(), 5, 5 + graphics.defaultFont().lineHeight * 15, "10min:  " + GameDuration.ofReal(Duration.ofMinutes(10), time, system).format(DurationFormat.VERBOSE, system), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }

    /**
     * The game time from tick 0 to the given point, which is what the old {@code DLTime} formatted.
     */
    private static GameDuration sinceOrigin(GameTime time) {
        return GameTime.ZERO.until(time);
    }
}
