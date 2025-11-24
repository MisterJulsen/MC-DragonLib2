package de.mrjulsen.mcdragonlib.mixin;

import com.mojang.realmsclient.client.RealmsClient;

import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.internal.DLTestWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setInitialScreen", at = @At(value = "HEAD"), cancellable = true)
    public void dragonlib$showScreen(RealmsClient realmsClient, ReloadInstance reloadInstance, GameConfig.QuickPlayData quickPlayData, CallbackInfo ci) {
        //DLWindow.openWindow(mgr -> new DLTestWindow(mgr));
        //ci.cancel();
    }

    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    public void dragonlib$resizeDisplay(CallbackInfo ci) {
        DLOverlayManager.resizeDisplay();
    }
}
