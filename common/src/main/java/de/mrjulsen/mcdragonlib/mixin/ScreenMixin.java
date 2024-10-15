package de.mrjulsen.mcdragonlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void onRender(CallbackInfo ci) {
        if (this instanceof IDragonLibWidget) {
            ci.cancel();
        }
    }
}
