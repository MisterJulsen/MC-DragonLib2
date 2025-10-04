package de.mrjulsen.mcdragonlib.mixin;

import com.mojang.realmsclient.client.RealmsClient;

import de.mrjulsen.mcdragonlib.client.newgui.test.DLTestWindow;
import de.mrjulsen.mcdragonlib.internal.DLScreenWrapper;
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
    public void onShowScreen(RealmsClient realmsClient, ReloadInstance reloadInstance, GameConfig.QuickPlayData quickPlayData, CallbackInfo ci) {
        
        //Minecraft.getInstance().setScreen(new DLScreenWrapper(root -> new DLTestWindow(root)));
        //ci.cancel();
    }
}
