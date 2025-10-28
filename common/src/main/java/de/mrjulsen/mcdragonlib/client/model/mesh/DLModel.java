package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.mcdragonlib.client.model.DLBlockModelRegistry;
import de.mrjulsen.mcdragonlib.client.model.IDynamicBakedModel;
import de.mrjulsen.mcdragonlib.client.model.ModelCacheKey;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.util.Cache;
import de.mrjulsen.mcdragonlib.util.DLColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DLModel {

    public static enum ModelType {
        BLOCK(false),
        ITEM(true);

        final boolean isItem;

        private ModelType(boolean b) {
            this.isItem = b;
        }

        public static ModelType isItem(boolean b) {
            return b ? ITEM : BLOCK;
        }
    }

    private final Map<ModelCacheKey, Map<DirectionKey, ListMultimap<RenderType, BakedQuad>>> cachedBlockQuads = Collections.synchronizedMap(new HashMap<>());

    /**
     * Creates the key for the cache of this model. This method is not called for item models.
     * @param state
     * @param random
     * @param contex
     * @return
     */
    protected ModelCacheKey createCacheKey(BlockState state, RandomSource random, ModelContext contex) {
        return new ModelCacheKey(contex, state);
    }

    protected boolean invalidateCacheFor(ModelType type, BlockState state, RandomSource random, ModelContext context) {
        return false;
    }

    /**
     * Enabled or disables ambient occlusion for this model.
     * 
     * @return {@code true} if ambient occlusion should be used, {@code false}
     *         otherwise. If the model's default behaviour should be kept, return
     *         {@code null}.
     */
    public @Nullable Boolean useAmbientOcclusion() {
        return null;
    }

    protected abstract Mesh getMesh(ModelType type, BakedModel originalModel, BlockState state, RandomSource random, ModelContext context);

    private final Cache<List<RenderType>> renderTypesCache = new Cache<>(() -> {
        Set<RenderType> types = new HashSet<>(RenderType.chunkBufferLayers().size());
        for (Map<DirectionKey, ListMultimap<RenderType, BakedQuad>> m : cachedBlockQuads.values()) {
            for (ListMultimap<RenderType, BakedQuad> k : m.values()) {
                for (RenderType t : k.keySet()) {
                    types.add(t);
                }
            }
        }
        return new ArrayList<>(types);
    });
    

    private static class DirectionKey {
        private static final HashMap<Direction, DirectionKey> keys;
        static {
            keys = new HashMap<>(Direction.values().length + 1);
            for (Direction dir : Direction.values()) {
                keys.put(dir, new DirectionKey());
            }
            keys.put(null, new DirectionKey());
        }

        private DirectionKey() {
        }

        public static DirectionKey of(Direction direction) {
            return keys.get(direction);
        }
    }

    public final List<BakedQuad> getQuads(ModelType type, BakedModel originalModel, BlockState state, RandomSource random, RenderType renderType, Direction cullface, ModelContext context) {
        boolean isItem = type == ModelType.ITEM;
        ModelCacheKey key = !isItem ? createCacheKey(state, random, context) : new ModelCacheKey(context);

        if (invalidateCacheFor(type, state, random, context) && cachedBlockQuads.containsKey(key)) {
            cachedBlockQuads.remove(key);
        }

        Map<DirectionKey, ListMultimap<RenderType, BakedQuad>> quadsByCullface = cachedBlockQuads.computeIfAbsent(key,
            c -> {
                boolean clearRenderTypeCache = false;
                Mesh mesh = getMesh(type, originalModel, state, random, c.context());

                Map<DirectionKey, ListMultimap<RenderType, BakedQuad>> map = new HashMap<>(6);
                for (Face face : mesh.getFaces()) {
                    DirectionKey cullfaceKey = DirectionKey.of(face.getCullface());
                    ListMultimap<RenderType, BakedQuad> mm = map.computeIfAbsent(cullfaceKey,
                            x -> ListMultimapBuilder.hashKeys(RenderType.chunkBufferLayers().size())
                                    .arrayListValues().build());
                    if (!clearRenderTypeCache && !mm.containsKey(face.getRenderType())) {
                        clearRenderTypeCache = true;
                    }
                    mm.putAll(face.getRenderType(), face.build());
                }

                if (clearRenderTypeCache)
                    clearCaches();
                return map;
            });

        DirectionKey requestedCullfaceKey = DirectionKey.of(cullface);
        if (!quadsByCullface.containsKey(requestedCullfaceKey)) {
            return List.of();
        }

        ListMultimap<RenderType, BakedQuad> quadsByRenderType = quadsByCullface.get(requestedCullfaceKey);
        if (isItem) {
            return new ArrayList<>(quadsByRenderType.values());
        }

        if (!quadsByRenderType.containsKey(renderType)) {
            return List.of();
        }
        return quadsByRenderType.get(renderType);
    }

    public final List<RenderType> getSupportedRenderTypes() {
        return cachedBlockQuads.isEmpty() ? RenderType.chunkBufferLayers() : renderTypesCache.get();
    }

    private final void clearCaches() {
        renderTypesCache.clear();
    }





    public static BakedModel getModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
    }

    public static boolean hasDynamicModel(BlockState state) {
        BakedModel model = getModel(state);
        return model instanceof IDynamicBakedModel;
    }

    public static Optional<DLModel> of(BlockState state) {
        if (!hasDynamicModel(state)) return Optional.empty();
        return Optional.ofNullable(((IDynamicBakedModel)getModel(state)).getModel());
    }

    public static void renderModel(PoseStack.Pose pose, VertexConsumer consumer, @Nullable BlockState state, ModelContext context) {
        of(state).ifPresent(x -> x.render(pose, consumer, state, context));
    }

    public static void renderModel(PoseStack.Pose pose, VertexConsumer consumer, ModelType type, @Nullable BlockState state, ModelContext context, DLColor color, int packedLight, int packedOverlay) {
        of(state).ifPresent(x -> x.render(pose, consumer, type, state, context, color, packedLight, packedOverlay));
    }

    public void render(PoseStack.Pose pose, VertexConsumer consumer, @Nullable BlockState state, ModelContext context) {
        render(pose, consumer, ModelType.BLOCK, state, context, DLColor.WHITE, 0, 0);
    }

    public void render(PoseStack.Pose pose, VertexConsumer consumer, ModelType type, @Nullable BlockState state, ModelContext context, DLColor color, int packedLight, int packedOverlay) {
        RandomSource randomSource = RandomSource.create();
        long seed = 42L;
        for (RenderType renderType : getSupportedRenderTypes()) {
            for (Direction direction : Direction.values()) {
                randomSource.setSeed(seed);
                renderQuadList(pose, consumer, color, getQuads(type, DLBlockModelRegistry.getOriginalModel(state), state, randomSource, renderType, direction, context), packedLight, packedOverlay);
            }
            randomSource.setSeed(seed);
            renderQuadList(pose, consumer, color, getQuads(type, DLBlockModelRegistry.getOriginalModel(state), state, randomSource, renderType, null, context), packedLight, packedOverlay);
        }
        
    }

    private static void renderQuadList(PoseStack.Pose pose, VertexConsumer consumer, DLColor color, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        BakedQuad bakedQuad;
        float r;
        float g;
        float b;
        for (Iterator<BakedQuad> iterator = quads.iterator(); iterator.hasNext(); consumer.putBulkData(pose, bakedQuad, r, g, b, packedLight, packedOverlay)) {
            bakedQuad = (BakedQuad)iterator.next();
            if (bakedQuad.isTinted()) {
                r = Mth.clamp(color.getRedF(), 0.0F, 1.0F);
                g = Mth.clamp(color.getGreenF(), 0.0F, 1.0F);
                b = Mth.clamp(color.getBlueF(), 0.0F, 1.0F);
            } else {
                r = 1.0F;
                g = 1.0F;
                b = 1.0F;
            }
        }

    }
}
