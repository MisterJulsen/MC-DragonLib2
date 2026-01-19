package de.mrjulsen.mcdragonlib.mixin.extension;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mrjulsen.mcdragonlib.client.model.extension.DLFaceData;
import de.mrjulsen.mcdragonlib.client.model.extension.IBlockFaceElementExtension;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElementFace$Deserializer")
public class BlockElementFaceDeserializerMixin {

    @Inject(method = "deserialize*", at = @At("RETURN"))
    public void dragonlib$onDeserialize(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockElementFace> cir) {
        if (cir.getReturnValue() instanceof IBlockFaceElementExtension ext) {
            JsonObject jsonobject = json.getAsJsonObject();
            DLFaceData faceData = DLFaceData.read(jsonobject.get("dragonlib_data"), DLFaceData.DEFAULT);
            ext.dragonlib$setExtensionData(faceData);
        }
    }
}
