package de.mrjulsen.mcdragonlib;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class DragonLibCrossPlatform {    
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
    }
}
