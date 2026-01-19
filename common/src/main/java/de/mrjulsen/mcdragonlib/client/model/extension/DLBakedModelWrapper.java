package de.mrjulsen.mcdragonlib.client.model.extension;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.resources.model.BakedModel;

public class DLBakedModelWrapper {

    @ExpectPlatform
    public static BakedModel wrap(BakedModel original) {
        throw new AssertionError();
    }
}
