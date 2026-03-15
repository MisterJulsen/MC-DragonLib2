package de.mrjulsen.mcdragonlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

/*
 * Temp Fix for versions older than 1.21 which adds scrollX
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onMove", at = @At(value = "TAIL"))
    private void dragonlib$onMove(long windowPointer, double xpos, double ypos, CallbackInfo ci) {
        if (windowPointer == Minecraft.getInstance().getWindow().getWindow()) {
            DLOverlayManager.mouseMoved(xpos, ypos);
        }
    }

    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private boolean dragonlib$scrollOverlay(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, Operation<Boolean> original) {
        boolean result = original.call(screen, mouseX, mouseY, deltaX, deltaY);
        if (!result) {
            result = DLOverlayManager.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }
        return result;
    }
}
