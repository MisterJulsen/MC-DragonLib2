package de.mrjulsen.mcdragonlib.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.core.ColorObject;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.mixin.FontAccessor;
import de.mrjulsen.mcdragonlib.util.ColorUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

public class GuiUtils {

    public static void enableScissor(Graphics graphics, int x, int y, int w, int h) {
        int scale = (int)Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor(x * scale, Minecraft.getInstance().getWindow().getHeight() - (y + h) * scale, w * scale, h * scale);        
        graphics.poseStack().pushPose();
    }

    public static void disableScissor(Graphics graphics) {
        graphics.poseStack().popPose();
        RenderSystem.disableScissor();
    }

    public static void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static FormattedCharSequence toFormattedCharSequence(FormattedText text) {
        return text instanceof Component ? ((Component) text).getVisualOrderText() : Language.getInstance().getVisualOrder(text);
    }

    public static <T extends FormattedText> boolean renderTooltipAt(Screen screen, GuiAreaDefinition area, List<T> lines, int maxWidth, Graphics graphics, int xPos, int yPos, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (area.isInBounds((double) (mouseX + xOffset), (double) (mouseY + yOffset))) {
            screen.renderTooltip(graphics.poseStack(), GuiUtils.getTooltipData(screen, lines, maxWidth), xPos - 8, yPos + 16);
            return true;
        } else {
            return false;
        }
    }

    public static <W extends AbstractWidget, T extends FormattedText> boolean renderTooltip(Screen screen, W widget, List<T> lines, int maxWidth, Graphics graphics, int mouseX, int mouseY) {
        return renderTooltipWithOffset(screen, widget, lines, maxWidth, graphics, mouseX, mouseY, 0, 0);
    }

    public static <T extends FormattedText> boolean renderTooltip(Screen screen, GuiAreaDefinition area, List<T> lines, int maxWidth, Graphics graphics, int mouseX, int mouseY) {
        return renderTooltipWithOffset(screen, area, lines, maxWidth, graphics, mouseX, mouseY, 0, 0);
    }

    public static <W extends AbstractWidget, T extends FormattedText> boolean renderTooltipWithOffset(Screen screen, W widget, List<T> lines, int maxWidth, Graphics graphics, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (widget.isMouseOver(mouseX + xOffset, mouseY + yOffset)) {
            screen.renderTooltip(graphics.poseStack(), getTooltipData(screen, lines, maxWidth), mouseX, mouseY);
            return true;
        }
        return false;
    }

    public static <T extends FormattedText> boolean renderTooltipWithOffset(Screen screen, GuiAreaDefinition area, List<T> lines, int maxWidth, Graphics graphics, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (area.isInBounds(mouseX + xOffset, mouseY + yOffset)) {
            screen.renderTooltip(graphics.poseStack(), getTooltipData(screen, lines, maxWidth), mouseX, mouseY);
            return true;
        }
        return false;
    }

    @SuppressWarnings("resource")
    public static <T extends Enum<T> & ITranslatableEnum> List<FormattedCharSequence> getEnumTooltipData(String modid, Screen screen, Class<T> enumClass, int maxWidth) {
        List<FormattedCharSequence> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.addAll(((FontAccessor) Minecraft.getInstance().font).getSplitter()
                .splitLines(TextUtils.translate(enumValue.getEnumDescriptionTranslationKey(modid)), maxWidth, Style.EMPTY)
                .stream().map(x -> toFormattedCharSequence(x)).toList());
        c.add(TextUtils.text(" ").getVisualOrderText());
        c.addAll(Arrays.stream(enumClass.getEnumConstants()).map((tr) -> {
            return TextUtils.text(String.format("§l> %s§r§7\n%s", TextUtils.translate(tr.getValueTranslationKey(modid)).getString(), TextUtils.translate(tr.getValueInfoTranslationKey(modid)).getString()));
        }).map((x) -> ((FontAccessor) Minecraft.getInstance().font).getSplitter().splitLines(x, maxWidth, Style.EMPTY)
                .stream().map(a -> toFormattedCharSequence(a)).toList()).flatMap(List::stream).collect(Collectors.toList()));

        return c;
    }

    public static <T extends Enum<T> & ITranslatableEnum> List<Component> getEnumTooltipData(String modid, Class<T> enumClass) {
        List<Component> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.add(TextUtils.translate(enumValue.getEnumDescriptionTranslationKey(modid)));
        c.add(TextUtils.text(" "));
        c.addAll(Arrays.stream(enumClass.getEnumConstants()).map((tr) -> {
            return TextUtils.text(
                    String.format("§l> %s§r§7\n%s", TextUtils.translate(tr.getValueTranslationKey(modid)).getString(),
                            TextUtils.translate(tr.getValueInfoTranslationKey(modid)).getString()));
        }).toList());
        return c;
    }

    public static <T extends FormattedText> List<FormattedCharSequence> getTooltipData(Screen screen, T component, int maxWidth) {
        return getTooltipData(screen, List.of(component), maxWidth);
    }

    @SuppressWarnings("resource")
    public static <T extends FormattedText> List<FormattedCharSequence> getTooltipData(Screen screen, Collection<T> components, int maxWidth) {
        return components.stream().flatMap(a -> ((FontAccessor) Minecraft.getInstance().font).getSplitter().splitLines(a, maxWidth <= 0 ? screen.width : maxWidth, Style.EMPTY).stream()).map(x -> toFormattedCharSequence(x)).toList();
    }
    
    @SuppressWarnings("resource")
    public static <T extends FormattedText> List<FormattedText> getTooltipDataFormatted(Screen screen, Collection<T> components, int maxWidth) {
        return components.stream().flatMap(a -> ((FontAccessor) Minecraft.getInstance().font).getSplitter().splitLines(a, maxWidth <= 0 ? screen.width : maxWidth, Style.EMPTY).stream()).toList();
    }

    public static boolean editBoxNumberFilter(String input) {
        if (input.isEmpty())
            return true;

        String i = input;
        if (input.equals("-"))
            i = "-0";

        try {
            Integer.parseInt(i);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean editBoxPositiveNumberFilter(String input) {
        if (input.isEmpty()) {
            return true;
        } else {
            try {
                int i = Integer.parseInt(input);
                return i > 0;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }

    public static boolean editBoxNonNegativeNumberFilter(String input) {
        if (input.isEmpty()) {
            return true;
        } else {
            try {
                int i = Integer.parseInt(input);
                return i >= 0;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }

    public static void setTexture(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void setTexture(int textureId) {
        RenderSystem.setShaderTexture(0, textureId);
    }

    public static void setTint(float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void setTint(int color) {
        short[] argb = ColorUtils.decodeARGB(color);
        setTint(ColorObject.colorIntToFloat(argb[1]), ColorObject.colorIntToFloat(argb[2]), ColorObject.colorIntToFloat(argb[3]), ColorObject.colorIntToFloat(argb[0]));
    }

    public static void resetTint() {
        setTint(0xFFFFFFFF);
    }

    public static void drawTexture(ResourceLocation texture, Graphics graphics, int x, int y, int w, int h, int u, int v, int uW, int vH, int textureWidth, int textureHeight) {
        setTexture(texture);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, u, v, uW, vH, textureWidth, textureHeight);
    }

    public static void drawTexture(ResourceLocation texture, Graphics graphics, int x, int y, int w, int h, int u, int v, int textureWidth, int textureHeight) {
        setTexture(texture);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, u, v, w, h, textureWidth, textureHeight);
    }

    public static void drawTexture(ResourceLocation texture, Graphics graphics, int x, int y, int u, int v, int w, int h) {
        setTexture(texture);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, u, v, w, h, 256, 256);
    }

    public static void drawTexture(ResourceLocation texture, Graphics graphics, int x, int y, int w, int h) {
        setTexture(texture);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, 0, 0, w, h, w, h);
    }

    public static void drawTexture(int textureId, Graphics graphics, int x, int y, int w, int h, int u, int v, int uW, int vH, int textureWidth, int textureHeight) {
        setTexture(textureId);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, u, v, uW, vH, textureWidth, textureHeight);
    }

    public static void drawTexture(int textureId, Graphics graphics, int x, int y, int w, int h, int u, int v, int textureWidth, int textureHeight) {
        setTexture(textureId);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, u, v, w, h, textureWidth, textureHeight);
    }

    public static void drawTexture(int textureId, Graphics graphics, int x, int y, int w, int h, int textureWidth, int textureHeight) {
        setTexture(textureId);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, 0, 0, w, h, textureWidth, textureHeight);
    }

    public static void drawTexture(int textureId, Graphics graphics, int x, int y, int w, int h) {
        setTexture(textureId);
        GuiComponent.blit(graphics.poseStack(), x, y, w, h, 0, 0, w, h, w, h);
    }

    public static void fill(Graphics graphics, int x, int y, int w, int h, int color) {
        GuiComponent.fill(graphics.poseStack(), x, y, x + w, y + h, color);
    }

    public static void fillGradient(Graphics graphics, int x, int y, int z, int w, int h, int colorA, int colorB) {
        float startAlpha = (float) (colorA >> 24 & 255) / 255.0F;
        float startRed = (float) (colorA >> 16 & 255) / 255.0F;
        float startGreen = (float) (colorA >> 8 & 255) / 255.0F;
        float startBlue = (float) (colorA & 255) / 255.0F;
        float endAlpha = (float) (colorB >> 24 & 255) / 255.0F;
        float endRed = (float) (colorB >> 16 & 255) / 255.0F;
        float endGreen = (float) (colorB >> 8 & 255) / 255.0F;
        float endBlue = (float) (colorB & 255) / 255.0F;

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(graphics.poseStack().last().pose(), x + w, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(graphics.poseStack().last().pose(), x, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(graphics.poseStack().last().pose(), x, y + h, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.vertex(graphics.poseStack().last().pose(), x + w, y + h, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void drawBox(Graphics graphics, GuiAreaDefinition area, int fillColor, int borderColor) {
        fill(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight(), fillColor);

        fill(graphics, area.getLeft(), area.getTop(), area.getWidth(), 1, borderColor);
        fill(graphics, area.getLeft(), area.getBottom() - 1, area.getWidth(), 1, borderColor);
        fill(graphics, area.getLeft(), area.getTop() + 1, 1, area.getHeight() - 2, borderColor);
        fill(graphics, area.getRight() - 1, area.getTop() + 1, 1, area.getHeight() - 2, borderColor);
    }

    public static void drawString(Graphics graphics, Font font, int x, int y, String text, int color, EAlignment alignment, boolean shadow) {
        drawString(graphics, font, x, y, TextUtils.text(text), color, alignment, shadow);
    }

    public static void drawString(Graphics graphics, Font font, int x, int y, FormattedText text, int color, EAlignment alignment, boolean shadow) {
        int width = font.width(text);
        int offset = 0;
        switch (alignment) {
            default:
            case LEFT:
                break;
            case CENTER:
                offset = -width / 2;
                break;
            case RIGHT:
                offset = -width;
                break;
        }

        if (shadow) {
            GuiComponent.drawString(graphics.poseStack(), font, toFormattedCharSequence(text), x + offset, y, color);
        } else {
            font.draw(graphics.poseStack(), toFormattedCharSequence(text), x + offset, y, color);
        }
    }

    public static DLButton createButton(int x, int y, int width, int height, Component text, Consumer<DLButton> onClick) {
        return new DLButton(x, y, width, height, text, onClick);
    }

    public static <T extends Enum<T> & ITranslatableEnum> DLCycleButton<T> createCycleButton(String modid, Class<T> clazz, int x, int y, int width, int height, Component text, T initialValue, BiConsumer<DLCycleButton<?>, T> onValueChanged) {
        DLCycleButton<T> btn = DLCycleButton.<T>builder((p) -> {
            return TextUtils.translate(clazz.cast(p).getValueTranslationKey(modid));
        })
            .withValues(clazz.getEnumConstants()).withInitialValue(initialValue)
            .create(x, y, width, height, text, (b, v) -> onValueChanged.accept(b, v));
        return btn;
    }

    public static DLCycleButton<Boolean> createOnOffButton(int x, int y, int width, int height, Component text, boolean initialValue, BiConsumer<DLCycleButton<?>, Boolean> onValueChanged) {
        DLCycleButton<Boolean> btn = DLCycleButton.onOffBuilder(initialValue).create(x, y, width, height, text, (b, v) -> onValueChanged.accept(b, v));
        return btn;
    }

    public static DLEditBox createEditBox(int x, int h, int width, int height, Font font, String text, Component hint, boolean drawBg, Consumer<String> onValueChanged, BiConsumer<DLEditBox, Boolean> onFocusChanged) {
        DLEditBox box = new DLEditBox(font, x, h, width, height, TextUtils.text(text))
            .withHint(hint)
            .withOnFocusChanged(onFocusChanged);
        box.setResponder(onValueChanged);
        box.setValue(text);
        box.setBordered(drawBg);

        return box;
    }

    public static DLSlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix, double min, double max, double step, double initialValue, boolean drawLabel, BiConsumer<DLSlider, Double> onValueChanged, Consumer<DLSlider> onUpdateMessage) {

        DLSlider slider = new DLSlider(x, y, width, height, prefix, suffix, min, max, initialValue, step, 1, drawLabel, null) {
            @Override
            protected void updateMessage() {
                if (onUpdateMessage == null) {
                    if (this.drawString) {
                        this.setMessage(TextUtils.text("").append(prefix).append(": ").append(this.getValueString()).append(suffix));
                    } else {
                        this.setMessage(TextUtils.empty());
                    }
                    return;
                }
                onUpdateMessage.accept(this);
            }

            @Override
            protected void applyValue() {
                super.applyValue();
                onValueChanged.accept(this, this.getValue());
            }
        };

        return slider;
    }

}
