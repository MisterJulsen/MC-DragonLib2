package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.math.Transformation;

import net.minecraft.core.Direction;

public interface TransformationExtensions {
		default Transformation dragonlib$applyOrigin(Vector3f origin) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default Matrix3f dragonlib$getNormalMatrix() {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default void dragonlib$transformPosition(Vector4f position) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default Direction dragonlib$rotateTransform(Direction facing) {
		throw new RuntimeException("this should be overridden via mixin. what?");
	}

	default boolean dragonlib$isIdentity() {
		return this.equals(Transformation.identity());
	}

	default void dragonlib$transformNormal(Vector3f normal) {
		normal.mul(dragonlib$getNormalMatrix());
		normal.normalize();
	}

	default Transformation dragonlib$blockCenterToCorner() {
		return dragonlib$applyOrigin(new Vector3f(.5f, .5f, .5f));
	}

	default Transformation dragonlib$blockCornerToCenter() {
		return dragonlib$applyOrigin(new Vector3f(-.5f, -.5f, -.5f));
	}
}
