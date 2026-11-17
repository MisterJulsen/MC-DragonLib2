package de.mrjulsen.mcdragonlib.client.util;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.compat.shader.ShaderCompat;
import de.mrjulsen.mcdragonlib.config.ModClientConfig;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

@ApiStatus.Experimental
public final class SmoothTextRenderTypes {

    public static final ResourceLocation SHADER_ID = new ResourceLocation(DragonLib.MODID, "rendertype_smooth_text");
    public static final ResourceLocation INTENSITY_SHADER_ID = new ResourceLocation(DragonLib.MODID, "rendertype_smooth_text_intensity");

    private static final Map<RenderType, RenderType> TYPES = new IdentityHashMap<>();
    private static ShaderInstance shader;
    private static ShaderInstance intensityShader;
    private static final RenderStateShard.ShaderStateShard SHADER_STATE = new RenderStateShard.ShaderStateShard(() -> shader);
    private static final RenderStateShard.ShaderStateShard INTENSITY_SHADER_STATE = new RenderStateShard.ShaderStateShard(() -> intensityShader);

    private SmoothTextRenderTypes() {}

    @ApiStatus.Internal
    public static void setShader(ShaderInstance instance) {
        shader = instance;
    }

    @ApiStatus.Internal
    public static void setIntensityShader(ShaderInstance instance) {
        intensityShader = instance;
    }

    @ApiStatus.Internal
    public static void onShaderLoadFailed(ResourceLocation id, Throwable error) {
        shader = null;
        intensityShader = null;
        DragonLib.LOGGER.error("Unable to load the shader '" + id + "'. Smooth text rendering is disabled.", error);
    }

    public static boolean isEnabled() {
        if (shader == null || intensityShader == null) {
            return false;
        }
        return switch (ModClientConfig.getSmoothTextModeSafe()) {
            case ON -> true;
            case OFF -> false;
            default -> !ShaderCompat.isShaderPackActive();
        };
    }

    public static RenderType getFor(RenderType type) {
        return TYPES.computeIfAbsent(type, SmoothTextRenderTypes::createVariant);
    }

    private static RenderType createVariant(RenderType type) {
        if (!(type instanceof RenderType.CompositeRenderType composite)) {
            return type;
        }
        RenderType.CompositeState state = composite.state();
        RenderStateShard.ShaderStateShard shaderState;
        if (state.shaderState == RenderStateShard.RENDERTYPE_TEXT_SHADER) {
            shaderState = SHADER_STATE;
        } else if (state.shaderState == RenderStateShard.RENDERTYPE_TEXT_INTENSITY_SHADER) {
            shaderState = INTENSITY_SHADER_STATE;
        } else {
            return type;
        }
        Optional<ResourceLocation> texture = state.textureState.cutoutTexture();
        if (texture.isEmpty()) {
            return type;
        }
        return RenderType.create("dragonlib_smooth_" + (shaderState == SHADER_STATE ? "text_" : "text_intensity_") + texture.get(), type.format(), type.mode(), type.bufferSize(), false, true,
            RenderType.CompositeState.builder()
                .setShaderState(shaderState)
                .setTextureState(new RenderStateShard.TextureStateShard(texture.get(), false, false))
                .setTransparencyState(state.transparencyState)
                .setDepthTestState(state.depthTestState)
                .setLightmapState(state.lightmapState)
                .setLayeringState(state.layeringState)
                .setWriteMaskState(state.writeMaskState)
                .createCompositeState(false));
    }
}
