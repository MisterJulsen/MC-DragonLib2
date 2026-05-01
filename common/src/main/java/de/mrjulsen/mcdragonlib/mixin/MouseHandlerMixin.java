package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.mcdragonlib.internal.DLScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

/*
 * Temp Fix for versions older than 1.21 which adds scrollX
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Unique
    private double xScrollOffset;
    
    @Inject(method = "onScroll", at = @At(value = "HEAD"))
    private void dragonlib$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if (windowPointer == Minecraft.getInstance().getWindow().getWindow()) {
            boolean discreteMouseScroll = (Boolean)Minecraft.getInstance().options.discreteMouseScroll().get();
            double sensitiviy = (Double)Minecraft.getInstance().options.mouseWheelSensitivity().get();
            xScrollOffset = (discreteMouseScroll ? Math.signum(xOffset) : xOffset) * sensitiviy;
        }
    }

    @Redirect(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDD)Z"))
    private boolean dragonlib$onScroll(Screen screen, double mouseX, double mouseY, double delta) {
        if (screen instanceof DLScreenWrapper dlScreen) {
            return dlScreen.onScroll(mouseX, mouseY, xScrollOffset, delta);
        }
        return screen.mouseScrolled(mouseX, mouseY, delta);
    }
}
