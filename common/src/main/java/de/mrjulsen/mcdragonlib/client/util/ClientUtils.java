package de.mrjulsen.mcdragonlib.client.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;

public final class ClientUtils {
    private ClientUtils() {}
    
    public static void setSectionDirty(SectionPos pos) {
        Minecraft.getInstance().execute(() -> {            
            Minecraft.getInstance().levelRenderer.setSectionDirty(pos.getX(), pos.getY(), pos.getZ());
        });
    }

    public static void resetTranslation(PoseStack poseStack) {
        PoseStack.Pose currentPose = poseStack.last();
        Matrix4f matrix = currentPose.pose();
        matrix.m30(0);
        matrix.m31(0);
        matrix.m32(0);
    }

    public static void resetRotation(PoseStack poseStack) {
        PoseStack.Pose currentPose = poseStack.last();
        Matrix4f matrix = currentPose.pose();
    
        Vector3f translation = new Vector3f();
        matrix.getTranslation(translation);
        Vector3f scale = new Vector3f();
        matrix.getScale(scale);
        
        matrix.identity();
        matrix.translate(translation);
        matrix.scale(scale);
    }
    
    public static void resetScale(PoseStack poseStack) {
        PoseStack.Pose currentPose = poseStack.last();
        Matrix4f matrix = currentPose.pose();

        Vector3f translation = new Vector3f();
        matrix.getTranslation(translation);
        Quaternionf rotation = new Quaternionf();
        matrix.getUnnormalizedRotation(rotation);

        matrix.identity();
        matrix.translate(translation);
        matrix.rotate(rotation);
    }
}
