package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.math.Transformation;

import net.minecraft.core.Direction;

public interface TransformationExtensions {
		default Transformation applyOrigin(Vector3f origin) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default Matrix3f getNormalMatrix() {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default void transformPosition(Vector4f position) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default Direction rotateTransform(Direction facing) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default boolean isIdentity() {
		return this.equals(Transformation.identity());
	}

	default void transformNormal(Vector3f normal) {
		normal.mul(getNormalMatrix());
		normal.normalize();
	}

		default Transformation blockCenterToCorner() {
		return applyOrigin(new Vector3f(.5f, .5f, .5f));
	}

		default Transformation blockCornerToCenter() {
		return applyOrigin(new Vector3f(-.5f, -.5f, -.5f));
	}
}
