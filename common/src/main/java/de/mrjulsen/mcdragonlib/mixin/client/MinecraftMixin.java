/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.mixin.client;

import com.mojang.realmsclient.client.RealmsClient;

import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    /*
    @Inject(method = "setInitialScreen", at = @At(value = "HEAD"), cancellable = true)
    public void dragonlib$showScreen(RealmsClient realmsClient, ReloadInstance reloadInstance, GameConfig.QuickPlayData quickPlayData, CallbackInfo ci) {
        //DLWindow.openWindow(mgr -> new DLTestWindow(mgr));
        //ci.cancel();
    }
    */

    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    public void dragonlib$resizeDisplay(CallbackInfo ci) {
        DLOverlayManager.resizeDisplay();
    }
}
