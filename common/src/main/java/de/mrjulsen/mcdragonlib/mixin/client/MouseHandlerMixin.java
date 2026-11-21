/*
 * DragonLib 3 - 1.21.1
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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mrjulsen.mcdragonlib.client.DLOverlayManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    /*
     * Since 1.20.5 the screen receives mouse movement in handleAccumulatedMovement instead of onMove.
     * The gui scaled coordinates are stored in the third and fourth double local.
     */
    @Inject(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = Shift.BEFORE, ordinal = 0))
    private void dragonlib$onMove(CallbackInfo ci, @Local(ordinal = 2) double mouseX, @Local(ordinal = 3) double mouseY) {
        DLOverlayManager.mouseMoved(mouseX, mouseY);
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
