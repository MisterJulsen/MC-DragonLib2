package de.mrjulsen.mcdragonlib.client.newgui.widgets.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public record Graphics(GuiGraphics graphics, PoseStack poseStack, float partialTick) {}
