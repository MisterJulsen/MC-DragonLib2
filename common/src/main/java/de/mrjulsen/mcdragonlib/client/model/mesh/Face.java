package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import de.mrjulsen.mcdragonlib.client.model.extension.DLBakedQuad;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.model.ModelUtils;
import de.mrjulsen.mcdragonlib.client.util.DLGraphics;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;

public class Face implements ITransformable<Face> {    

    private final List<FaceVertex> corners = Arrays.asList(new FaceVertex[CornerType.values().length]);
    private final List<Edge> edges = Arrays.asList(new Edge[EdgeType.values().length]);

    private TextureAtlasSprite sprite;
    private ResourceLocation texture;

    private DLColor color = DLColor.WHITE;
    private int tintIndex = -1;
    private Direction normalDirection;
    private Direction cullface = null;
    private boolean isShade = true;
    private RenderType renderType = RenderType.solid();
    private boolean useAlternateSplitLine = false;

    // Custom
    private boolean ambientOcclusion = true;
    private boolean emissive = false;
    private List<String> tags = List.of();

    private Direction overrideNormalDirection;

    public Face(Vector3f[] positions) {
        if (positions.length != CornerType.values().length) {
            throw new IllegalArgumentException("A Face must have exactly " + CornerType.values().length + " vertices!");
        }
        for (int i = 0; i < positions.length; i++) {
            corners.set(i, new FaceVertex(new Vertex(positions[i], new Vector3f(), DLColor.WHITE), CornerType.getByIndex(i).uv(), new int[] { 0, 0 }));
        }
        this.setTexture(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(DLUtils.resourceLocation(DragonLib.MODID, "block/white")));
        createEdges();
        Vector3f normal = recalculateNormals();
        this.normalDirection = Direction.getNearest(normal.x(), normal.y(), normal.z());

    }
    
    public Face(BakedQuad quad, Direction cullface) {
        int[] vertexData = quad.getVertices();

        float[] pos = new float[3];
        float[] normal = new float[3];
        int[] vertexColor = new int[4];
        int[] light = new int[2];
        float[] uv = new float[2];

        float u0 = quad.getSprite().getU0();
        float v0 = quad.getSprite().getV0();
        float u1 = quad.getSprite().getU1();
        float v1 = quad.getSprite().getV1();
        float spriteW = u1 - u0;
        float spriteH = v1 - v0;

        if (quad instanceof DLBakedQuad ext) {
            this.ambientOcclusion = ext.isAmbientOcclusion();
            this.emissive = ext.isEmissive();
            this.tags = new ArrayList<>(ext.getTags());
        }
        
        for (int i = 0; i < corners.size(); i++) {
            ModelUtils.unpackPosition(vertexData, pos, i);
            ModelUtils.unpackNormals(vertexData, normal, i);
            ModelUtils.unpackColor(vertexData, vertexColor, i);
            ModelUtils.unpackLight(vertexData, light, i);
            ModelUtils.unpackUV(vertexData, uv, i);
            float lU = uv[0] - u0;
            float lV = uv[1] - v0;

            FaceVertex corner = new FaceVertex(new Vertex(pos, normal, vertexColor), new float[] { 1f / spriteW * lU, 1f / spriteH * lV}, light);
            this.corners.set(i, corner);
        }

        createEdges();
        recalculateNormals();

        this.normalDirection = quad.getDirection();
        this.cullface = cullface;
        this.isShade = quad.isShade();
        this.sprite = quad.getSprite();
        this.tintIndex = quad.getTintIndex();
    }

    private void createEdges() {
        for (int i = 0; i < corners.size(); i++) {
            CornerType currentCorner = CornerType.getByIndex(i);
            CornerType nextCorner = CornerType.getByIndex((i + 1) % corners.size());
            this.edges.set(i, new Edge(getCorner(currentCorner).getVertex(), getCorner(nextCorner).getVertex()));
        }
    }

    public static Face createFace(Direction dir, Vector3f position, float width, float height) {
        return createFace(dir, position.x(), position.y(), position.z(), width, height);
    }

    public static Face createFace(Direction dir, float x, float y, float z, float width, float height) {
        Vector3f[] positions = new Vector3f[4];

        switch (dir) {
            case SOUTH: {
                positions[0] = new Vector3f(x, y + height, z);
                positions[1] = new Vector3f(x, y, z);
                positions[2] = new Vector3f(x + width, y, z);
                positions[3] = new Vector3f(x + width, y + height, z);
                break;
            }
            case NORTH: {
                positions[0] = new Vector3f(x + width, y + height, z);
                positions[1] = new Vector3f(x + width, y, z);
                positions[2] = new Vector3f(x, y, z);
                positions[3] = new Vector3f(x, y + height, z);
                break;
            }
            case EAST: {
                positions[0] = new Vector3f(x, y + height, z + width);
                positions[1] = new Vector3f(x, y, z + width);
                positions[2] = new Vector3f(x, y, z);
                positions[3] = new Vector3f(x, y + height, z);
                break;
            }
            case WEST: {
                positions[0] = new Vector3f(x, y + height, z);
                positions[1] = new Vector3f(x, y, z);
                positions[2] = new Vector3f(x, y, z + width);
                positions[3] = new Vector3f(x, y + height, z + width);
                break;
            }
            case UP: {
                positions[0] = new Vector3f(x, y, z);
                positions[1] = new Vector3f(x, y, z + height);
                positions[2] = new Vector3f(x + width, y, z + height);
                positions[3] = new Vector3f(x + width, y, z);
                break;
            }
            case DOWN: {
                positions[0] = new Vector3f(x, y, z + height);
                positions[1] = new Vector3f(x, y, z);
                positions[2] = new Vector3f(x + width, y, z);
                positions[3] = new Vector3f(x + width, y, z + height);
                break;
            }
        }

        Face face = new Face(positions);
        return face;
    }

    public void autoSetCullface(float threshold) {
        Vector3f n = recalculateNormals();
        Direction direction = Direction.getNearest(n.x, n.y, n.z);
        if (direction == null) return;

    // Eckenpositionen
        Vector3f p0 = getVertexPos(CornerType.TOP_LEFT);
        Vector3f p1 = getVertexPos(CornerType.BOTTOM_LEFT);
        Vector3f p2 = getVertexPos(CornerType.BOTTOM_RIGHT);
        Vector3f p3 = getVertexPos(CornerType.TOP_RIGHT);

        float minX = Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x));
        float maxX = Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x));
        float minY = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y));
        float maxY = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));
        float minZ = Math.min(Math.min(p0.z, p1.z), Math.min(p2.z, p3.z));
        float maxZ = Math.max(Math.max(p0.z, p1.z), Math.max(p2.z, p3.z));

        float width = 0, height = 0, expectedPlane = 0;
        float planeValue = 0;

        switch (direction) {
            case UP -> {
                width = maxX - minX;
                height = maxZ - minZ;
                planeValue = maxY;
                expectedPlane = 1.0f;
            }
            case DOWN -> {
                width = maxX - minX;
                height = maxZ - minZ;
                planeValue = minY;
                expectedPlane = 0.0f;
            }
            case NORTH -> {
                width = maxX - minX;
                height = maxY - minY;
                planeValue = minZ;
                expectedPlane = 0.0f;
            }
            case SOUTH -> {
                width = maxX - minX;
                height = maxY - minY;
                planeValue = maxZ;
                expectedPlane = 1.0f;
            }
            case EAST -> {
                width = maxZ - minZ;
                height = maxY - minY;
                planeValue = maxX;
                expectedPlane = 1.0f;
            }
            case WEST -> {
                width = maxZ - minZ;
                height = maxY - minY;
                planeValue = minX;
                expectedPlane = 0.0f;
            }
        }

        boolean isFullSize = Math.abs(width - 1.0f) <= threshold && Math.abs(height - 1.0f) <= threshold;
        boolean isOnBlockBorder = Math.abs(planeValue - expectedPlane) <= threshold;

        // Zusätzlich: Face muss vollständig im Block liegen (alle Ecken in [0, 1])
        boolean withinBlock = minX >= -threshold && maxX <= 1.0f + threshold &&
                            minY >= -threshold && maxY <= 1.0f + threshold &&
                            minZ >= -threshold && maxZ <= 1.0f + threshold;

        if (isFullSize && isOnBlockBorder && withinBlock) {
            setCullface(direction);
        }
    }
        
    public List<BakedQuad> build() {
        boolean isPlanar = checkIsPlanar();

        if (isPlanar) {
            BakedQuad singleQuad = buildSingleQuad(this.corners.toArray(FaceVertex[]::new));
            return List.of(singleQuad);
        }
        else {
            List<BakedQuad> resultQuads = new ArrayList<>(2);
            
            FaceVertex v0 = corners.get(0);
            FaceVertex v1 = corners.get(1);
            FaceVertex v2 = corners.get(2);
            FaceVertex v3 = corners.get(3);

            FaceVertex[] corners1, corners2;

            if (!useAlternateSplitLine) {
                corners1 = new FaceVertex[] { v0, v1, v2, v2 };
                corners2 = new FaceVertex[] { v0, v0, v2, v3 };
            } else {
                corners1 = new FaceVertex[] { v0, v0, v1, v3 };
                corners2 = new FaceVertex[] { v1, v1, v2, v3 };
            }

            resultQuads.add(buildSingleQuad(corners1));
            resultQuads.add(buildSingleQuad(corners2));

            return resultQuads;
        }
    }

    private boolean checkIsPlanar() {
        if (corners.size() < 4) {
            return true;
        }
        final float EPSILON = 1e-5f;

        Vector3f p0 = new Vector3f(corners.get(0).getVertex().getPosAsArray());
        Vector3f p1 = new Vector3f(corners.get(1).getVertex().getPosAsArray());
        Vector3f p2 = new Vector3f(corners.get(2).getVertex().getPosAsArray());
        Vector3f p3 = new Vector3f(corners.get(3).getVertex().getPosAsArray());

        Vector3f vecA = new Vector3f(p1).sub(p0);
        Vector3f vecB = new Vector3f(p2).sub(p0);
        Vector3f planeNormal = new Vector3f(vecA).cross(vecB);
        Vector3f vecToP3 = new Vector3f(p3).sub(p0);
        return Math.abs(planeNormal.dot(vecToP3)) < EPSILON;
    }


    private BakedQuad buildSingleQuad(FaceVertex[] specificCorners) {
        if (getSprite().isEmpty()) {
            throw new IllegalStateException("Cannot create BakedQuad without a texture sprite.");
        }

        Vector3f normal = ModelUtils.fillNormal(specificCorners);
        Direction normalDir = Direction.getNearest(normal.x, normal.y, normal.z);
        int[] vertexData = new int[specificCorners.length * 8];
        
        float spriteW = sprite.getU1() - sprite.getU0();
        float spriteH = sprite.getV1() - sprite.getV0();

        for (int i = 0; i < specificCorners.length; i++) { 
            FaceVertex corner = specificCorners[i];
            DLColor col = DLColor.mixTint(getColor(), corner.getVertex().getColor());
            int[] colorArray = new int[] { col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha() };
            float[] uv = new float[] { sprite.getU0() + spriteW * corner.getU(), sprite.getV0() + spriteH * corner.getV() };

            ModelUtils.packPosition(corner.getVertex().getPosAsArray(), vertexData, i);
            ModelUtils.packUV(uv, vertexData, i);
            ModelUtils.packNormals(corner.getVertex().getNormalAsArray(), vertexData, i);
            ModelUtils.packColor(colorArray, vertexData, i);
            ModelUtils.packLight(corner.getLightAsArray(), vertexData, i);
        }

        return DLBakedQuad.create(
            vertexData,
            getTintIndex(),
            hasOverrideNormalDirection() ? overrideNormalDirection : normalDir,
            getSprite().orElse(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getTextureLocation())),
            isShade(),
            ambientOcclusion,
            emissive,
            List.copyOf(tags)
        );
    }





    

    @Override
    public List<? extends FaceVertex> getTransformableElements() {
        return corners;
    }


    public FaceVertex getCorner(CornerType corner) {
        return corners.get(corner.index());
    }

    public ImmutableList<FaceVertex> getCorners() {
        return ImmutableList.copyOf(corners);
    }

    public Edge getEdge(EdgeType edge) {
        return edges.get(edge.index());
    }

    public ImmutableList<Edge> getEdges() {
        return ImmutableList.copyOf(edges);
    }

    public Pair<Edge, Edge> getEdgesAtCorner(CornerType corner) {
        return new Pair<>(
            edges.get((corner.index() + 1) % EdgeType.values().length), // behind
            edges.get(corner.index()) // in front
        );
    }

    void updateVertices(UnaryOperator<Vertex> replaceFunc) {
        for (FaceVertex wrapper : corners) {
            wrapper.updateVertex(replaceFunc.apply(wrapper.getVertex()));
        }
    }

    void updateEdges(UnaryOperator<Edge> replaceFunc) {
        for (int i = 0; i < edges.size(); i++) {
            edges.set(i, replaceFunc.apply(edges.get(i)));
        }
    }



    // --- FACE APPERANCE ---

    public Optional<TextureAtlasSprite> getSprite() {
        return Optional.ofNullable(sprite);
    }

    public ResourceLocation getTextureLocation() {
        return getSprite().map(x -> x.contents().name()).orElse(texture);
    }

    public Direction getCullface() {
        return cullface;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public int getTintIndex() {
        return tintIndex;
    }

    public Direction getNormalDirection() {
        return hasOverrideNormalDirection() ? overrideNormalDirection : normalDirection;
    }

    public boolean hasOverrideNormalDirection() {
        return overrideNormalDirection != null;
    }

    public boolean isShade() {
        return isShade;
    }

    public boolean useAlternateSplitLine() {
        return useAlternateSplitLine;
    }

    public boolean isEmissive() {
        return emissive;
    }

    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    public void setTexture(TextureAtlasSprite sprite) {
        Objects.requireNonNull(sprite);
        this.sprite = sprite;
        this.texture = null;
    }

    public void setTexture(ResourceLocation texture) {
        Objects.requireNonNull(texture);
        this.texture = texture;
        this.sprite = null;
    }

    public void setRenderType(RenderType type) {
        this.renderType = type;
    } 

    public void setCullface(Direction direction) {
        this.cullface = direction;
    }

    public void setTintIndex(int index) {
        this.tintIndex = index;
    }

    public void setOverwriteNormalDirection(Direction direction) {
        this.overrideNormalDirection = direction;
    }

    public void setShade(boolean b) {
        this.isShade = b;
    }

    public void setUseAlternateSplitLine(boolean b) {
        this.useAlternateSplitLine = b;
    }

    public void setEmissive(boolean b) {
        this.emissive = b;
    }

    public void setAmbientOcclusion(boolean b) {
        this.ambientOcclusion = b;
    }

    public List<String> getTags() {
        return tags;
    }

    public DLColor getColor() {
        return color;
    }


    public Vector3f getVertexPos(CornerType corner) {
        return getCorner(corner).getVertex().getPos();
    }

    public Vector3f[] getVertexPositionArray() {
        Vector3f[] array = new Vector3f[CornerType.values().length];
        for (CornerType type : CornerType.values()) {
            array[type.index()] = getVertexPos(type);
        }
        return array;
    }

    public Vector2f getTextureUV(CornerType corner) {
        return getCorner(corner).getUV();
    }

    public Vector2i getLight(CornerType corner) {
        return getCorner(corner).getLight();
    }

    public void setLight(int packedLight) {
        for (FaceVertex vertex : corners) {
            vertex.setLight(packedLight);
        }
    }

    public void setColor(DLColor color) {
        this.color = color;
    }

    public Vector3f center() {
        Vector3f center = new Vector3f();
        for (FaceVertex v : corners) {
            center.add(v.getVertex().getPos());
        }
        center.div(4.0f);
        return center;
    }

    public Vector3f getNormal() {
        Vector3f edge1 = new Vector3f(corners.get(1).getVertex().getPos()).sub(corners.get(0).getVertex().getPos());
        Vector3f edge2 = new Vector3f(corners.get(2).getVertex().getPos()).sub(corners.get(0).getVertex().getPos());
        Vector3f normal = edge1.cross(edge2).normalize();
        return normal;
    }

    public Vector3f recalculateNormals() {
        Vector3f normal = getNormal();
        for (FaceVertex v : corners) {
            v.getVertex().getNormal().set(normal);
        }
        return normal;
    }

    public Direction getFacingDirection() {
        Vector3f normal = recalculateNormals();
        return Direction.getNearest(normal.x(), normal.y(), normal.z());
    }

    public void autoUV() {
        autoUV(CornerType.TOP_LEFT, 1);
    }

    public void autoUV(CornerType align, float scale) {
        float leftLen = getEdge(EdgeType.LEFT).length();
        float rightLen = getEdge(EdgeType.RIGHT).length();
        float topLen = getEdge(EdgeType.TOP).length();
        float bottomLen = getEdge(EdgeType.BOTTOM).length();

        float width = Math.min(Math.max(Math.min(topLen, bottomLen) / scale, 0f), 1f);
        float height = Math.min(Math.max(Math.min(leftLen, rightLen) / scale, 0f), 1f);

        float u0, v0, u1, v1;

        switch (align) {
            case TOP_LEFT -> {
                u0 = 0f; v0 = 0f;
                u1 = width; v1 = height;
            }
            case TOP_RIGHT -> {
                u1 = 1f; v0 = 0f;
                u0 = 1f - width; v1 = height;
            }
            case BOTTOM_LEFT -> {
                u0 = 0f; v1 = 1f;
                u1 = width; v0 = 1f - height;
            }
            case BOTTOM_RIGHT -> {
                u1 = 1f; v1 = 1f;
                u0 = 1f - width; v0 = 1f - height;
            }
            default -> {
                u0 = 0f; v0 = 0f;
                u1 = width; v1 = height;
            }
        }

        for (CornerType corner : CornerType.values()) {
            float u = switch (corner) {
                case TOP_LEFT, BOTTOM_LEFT -> u0;
                case TOP_RIGHT, BOTTOM_RIGHT -> u1;
            };

            float v = switch (corner) {
                case TOP_LEFT, TOP_RIGHT -> v0;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> v1;
            };

            getCorner(corner).setUV(new Vector2f(u, v));
        }
    }

    public void rotateTexture(Rotation rotation) {
        final int VERTEX_COUNT = corners.size();

        int shift = ((rotation.getIterations() % VERTEX_COUNT) + VERTEX_COUNT) % VERTEX_COUNT;
        FaceVertex[] original = new FaceVertex[VERTEX_COUNT];
        for (int i = 0; i < VERTEX_COUNT; i++) {
            original[i] = corners.get(i);
        }
        for (int i = 0; i < VERTEX_COUNT; i++) {
            int newIndex = (i + shift) % VERTEX_COUNT;
            corners.set(newIndex, original[i]);
        }
    }




    private static int getBlockLight(int packedLight) {
        return packedLight & 0xFFFF;
    }

    private static int getSkyLight(int packedLight) {
        return (packedLight >> 16) & 0xFFFF;
    }

    public void render(DLGraphics graphics) {
        render(graphics, graphics.packedLight(), OverlayTexture.NO_OVERLAY, isShade(), graphics instanceof BERGraphics);
    }

    public void render(DLGraphics graphics, int light, boolean ambientOcclusion) {
        render(graphics, light, OverlayTexture.NO_OVERLAY, ambientOcclusion, graphics instanceof BERGraphics);
    }

    public void render(DLGraphics graphics, int packedLight, int packedOverlay, boolean ambientOcclusion, boolean transformForBER) {
        if (getSprite().isEmpty() && texture == null) {
            return;
        }

        TextureAtlasSprite sprite = getSprite().orElse(null);
        VertexConsumer consumer;
        
        if (sprite != null) {
            consumer = graphics.multiBufferSource().getBuffer(this.renderType);
        } else {
            consumer = graphics.vertexConsumer(getTextureLocation());
        }

        if (checkIsPlanar()) {
            FaceVertex[] quadVertices = corners.toArray(FaceVertex[]::new);
            renderQuad(consumer, graphics, packedLight, packedOverlay, ambientOcclusion, sprite, quadVertices, transformForBER);
        } else {
            FaceVertex v0 = corners.get(0);
            FaceVertex v1 = corners.get(1);
            FaceVertex v2 = corners.get(2);
            FaceVertex v3 = corners.get(3);

            if (!useAlternateSplitLine) {
                renderQuad(consumer, graphics, packedLight, packedOverlay, ambientOcclusion, sprite, new FaceVertex[] { v0, v1, v2, v2 }, transformForBER);
                renderQuad(consumer, graphics, packedLight, packedOverlay, ambientOcclusion, sprite, new FaceVertex[] { v0, v2, v3, v3 }, transformForBER);
            } else {
                renderQuad(consumer, graphics, packedLight, packedOverlay, ambientOcclusion, sprite, new FaceVertex[] { v0, v1, v3, v3 }, transformForBER);
                renderQuad(consumer, graphics, packedLight, packedOverlay, ambientOcclusion, sprite, new FaceVertex[] { v1, v2, v3, v3 }, transformForBER);
            }
        }
    }

    private void renderQuad(VertexConsumer consumer, DLGraphics graphics, int packedLight, int packedOverlay, boolean ambientOcclusion, @Nullable TextureAtlasSprite sprite, FaceVertex[] quadVertices, boolean transformForBER) {
        if (quadVertices.length != 4) {
            return;
        }

        PoseStack.Pose lastPose = graphics.poseStack().last();
        Matrix4f poseMatrix = lastPose.pose();

        float atlasU0 = (sprite != null) ? sprite.getU0() : 0.0f;
        float atlasV0 = (sprite != null) ? sprite.getV0() : 0.0f;
        float atlasUWidth = (sprite != null) ? (sprite.getU1() - sprite.getU0()) : 1.0f;
        float atlasVHeight = (sprite != null) ? (sprite.getV1() - sprite.getV0()) : 1.0f;

        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        float[] brightness = { 1.0f, 1.0f, 1.0f, 1.0f };
        int[] lightmap = { packedLight, packedLight, packedLight, packedLight };
        
        if (ambientOcclusion && Minecraft.getInstance().options.ambientOcclusion().get()) {
            if (graphics instanceof BERGraphics<?> berGraphics && berGraphics.blockEntity() != null && berGraphics.blockEntity().getLevel() != null && berGraphics.blockEntity().getBlockPos() != null) {
                try {
                    ModelBlockRenderer.AmbientOcclusionFace ao = new ModelBlockRenderer.AmbientOcclusionFace();
                    BlockAndTintGetter batg = berGraphics.blockEntity().getLevel();
                    BlockPos pos = berGraphics.blockEntity().getBlockPos();
                    
                    float[] tempAfloat = new float[Direction.values().length * 2];
                    BitSet tempBitSet = new BitSet(3);
                    
                    ao.calculate(batg, berGraphics.blockEntity().getBlockState(), pos, getFacingDirection(), tempAfloat, tempBitSet, true);

                    for (int i = 0; i < 4; i++) {
                        brightness[i] = ao.brightness[i];
                        lightmap[i] = ao.lightmap[i];
                    }
                } catch (Exception e) {
                }
            }
        }
        
        final float scaleXZ = transformForBER ? 16.0f : 1.0f;
        final float scaleY = transformForBER ? -16.0f : 1.0f; 
        
        for (int i = 0; i < 4; i++) {
            FaceVertex fv = quadVertices[i];
            Vertex v = fv.getVertex();

            DLColor vColor = v.getColor();
            float finalR = r * brightness[i] * vColor.getRedF();
            float finalG = g * brightness[i] * vColor.getGreenF();
            float finalB = b * brightness[i] * vColor.getBlueF();
            float finalA = a * vColor.getAlphaF();
            
            float finalU = atlasU0 + (fv.getU() * atlasUWidth);
            float finalV = atlasV0 + (fv.getV() * atlasVHeight);

            Vector3f pos = v.getPos();
            
            float correctedX = pos.x() * scaleXZ;
            float correctedZ = pos.z() * scaleXZ;
            float correctedY = pos.y() * scaleY;
            
            consumer.addVertex(poseMatrix, correctedX, correctedY, correctedZ);
            consumer.setColor(finalR, finalG, finalB, finalA);
            consumer.setUv(finalU, finalV);
            consumer.setUv2(getBlockLight(lightmap[i]), getSkyLight(lightmap[i]));
            consumer.setOverlay(packedOverlay);
            
            Vector3f normal = v.getNormal();
            consumer.setNormal(lastPose, normal.x(), normal.y(), normal.z());
            
            //consumer.endVertex();
        }
    }

}
