package de.mrjulsen.mcdragonlib.client.compat.shader;

import net.irisshaders.iris.api.v0.IrisApi;

final class IrisShaderPackDetector implements IShaderPackDetector {

    @Override
    public boolean isShaderPackInUse() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
