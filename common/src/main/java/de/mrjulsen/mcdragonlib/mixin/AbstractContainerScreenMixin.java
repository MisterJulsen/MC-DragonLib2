package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.mcdragonlib.client.gui.DLContainerScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"))
    private boolean changeCondition(Screen screen, double a, double b, int c) {
        return !(screen instanceof IDragonLibWidget) && super.mouseClicked(a, b, c);
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if ((AbstractContainerScreen<?>)(Object)this instanceof DLContainerScreen screen) {
            Graphics graphics = new Graphics(guiGraphics, guiGraphics.pose());
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0, 0, 100);
            screen.renderFrontLayer(graphics, mouseX, mouseY, partialTick);
            graphics.poseStack().popPose();
        }
    }
}
