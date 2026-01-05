package de.mrjulsen.mcdragonlib.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class DLUtils {

    public static ResourceLocation resourceLocation(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation resourceLocation(String path) {
        return new ResourceLocation(path);
    }

    public static void giveAdvancement(ServerPlayer player, String modid, String name, String criteriaKey) {
        Advancement adv = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(modid, name));
        player.getAdvancements().award(adv, criteriaKey);
    }

    public static String textureToBase64(NativeImage image) {
        try {
            return Base64.encodeBase64String(image.asByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static InputStream scaleImage(InputStream inputStream, int width, int height) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(scaledImage, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return new ByteArrayInputStream(imageBytes);
    }

    public static long encode3DCoordinates(int x, int y, int z) {
        long encodedValue = 0;

        encodedValue |= ((long)x & 0xFFFFFFFFL) << 32;
        encodedValue |= ((long)y & 0xFFFFFFFFL) << 16;
        encodedValue |= ((long)z & 0xFFFFFFFFL);

        return encodedValue;
    }    

    @Deprecated
    public static int coordsToInt(byte x, byte y) {
        int coords = ((x & 0xFF) << 16) | (y & 0xFF);
        return coords;
    }
    
    @Deprecated
    public static byte[] intToCoords(int coords) {
        byte x = (byte) ((coords >> 16) & 0xFF);
        byte y = (byte) (coords & 0xFF);
        return new byte[] {x, y};
    }

    public static record Point3D(int x, int y, int z) {}
    public static Point3D decode3DCoordinates(long encodedValue) {
        int x = (int) (encodedValue >> 32);
        int y = (int) ((encodedValue >> 16) & 0xFFFF);
        int z = (int) (encodedValue & 0xFFFF);

        return new Point3D(x, y, z);
    }

    /**
     * @related https://github.com/BluSunrize/ImmersiveEngineering/blob/1.19.2/src/main/java/blusunrize/immersiveengineering/common/util/orientation/RotationUtil.java#L28
     */
    public static boolean rotateBlock(Level world, BlockPos pos, Rotation rotation) {

        BlockState state = world.getBlockState(pos);
        BlockState newState = state.rotate(rotation);
        if (newState != state) {
            world.setBlockAndUpdate(pos, newState);
            for (Direction d : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                final BlockPos otherPos = pos.relative(d);
                final BlockState otherState = world.getBlockState(otherPos);
                final BlockState nextState = newState.updateShape(d, otherState, world, pos, otherPos);
                if (nextState != newState) {
                    if (!nextState.isAir()) {
                        world.setBlockAndUpdate(pos, nextState);
                        newState = nextState;
                    } else {
                        world.setBlockAndUpdate(pos, state);
                        return false;
                    }
                }
            }
            for (Direction d : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                final BlockPos otherPos = pos.relative(d);
                final BlockState otherState = world.getBlockState(otherPos);
                final BlockState nextOther = otherState.updateShape(d.getOpposite(), newState, world, otherPos, pos);
                if (nextOther != otherState)
                    world.setBlockAndUpdate(otherPos, nextOther);
            }
            return true;
        } else
            return false;
    }

    public static <T> void doIfNotNull(T obj, Consumer<T> action) {
        if (obj != null) {
            action.accept(obj);
        }
    }

    public static <T> void doIfNull(T obj, Runnable action) {
        if (obj == null) {
            action.run();
        }
    }

    public boolean isSectionInChunk(SectionPos section, ChunkPos chunk) {
        return section.getX() == chunk.x && section.getZ() == chunk.z;
    }

    public ChunkPos getChunkOfSection(SectionPos section) {
        return new ChunkPos(section.getX(), section.getZ());
    }

    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
