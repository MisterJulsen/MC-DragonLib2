package de.mrjulsen.mcdragonlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

/*
 * Temp Fix for versions older than 1.21 which adds scrollX
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Unique
    private double dragonlib$xScrollOffset;

    @Inject(method = "onScroll", at = @At(value = "HEAD"))
    private void dragonlib$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if (windowPointer == Minecraft.getInstance().getWindow().getWindow()) {
            boolean discreteMouseScroll = Minecraft.getInstance().options.discreteMouseScroll().get();
            double sensitiviy = Minecraft.getInstance().options.mouseWheelSensitivity().get();
            dragonlib$xScrollOffset = (discreteMouseScroll ? Math.signum(xOffset) : xOffset) * sensitiviy;
            if (Minecraft.getInstance().screen instanceof DLScreen<?> dlScreen) {
                dlScreen.xScrollDelta = dragonlib$xScrollOffset;
            }
        }
    }
    
    @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void dragonlib$onMove(long windowPointer, double x, double y, CallbackInfo ci, Screen screen, double mx, double my) {
        if (windowPointer == Minecraft.getInstance().getWindow().getWindow()) {
            DLOverlayManager.mouseMoved(mx, my);
        }
    }

    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDD)Z"))
    private boolean dragonlib$scrollOverlay(Screen screen, double mouseX, double mouseY, double delta, Operation<Boolean> original) {
        boolean result = original.call(screen, mouseX, mouseY, delta);
        if (!result) {
            DLOverlayManager.mouseScrolled(mouseX, mouseY, dragonlib$xScrollOffset, delta);
        }
        return result;
    }
}
