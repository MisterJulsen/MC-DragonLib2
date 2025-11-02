package de.mrjulsen.mcdragonlib.mixin;

import com.mojang.realmsclient.client.RealmsClient;

import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import de.mrjulsen.mcdragonlib.client.gui.test.DLTestWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLScreenWrapper;
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
        
        DLScreenWrapper wrapper = new DLScreenWrapper(null, root -> new DLTestWindow(root));
        //wrapper.getWindowManager().createWindow(RedWindow::new);
        Minecraft.getInstance().setScreen(wrapper);
        ci.cancel();
    }

    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    public void dragonlib$resizeDisplay(CallbackInfo ci) {
        DLOverlayManager.resizeDisplay();
    }
}
