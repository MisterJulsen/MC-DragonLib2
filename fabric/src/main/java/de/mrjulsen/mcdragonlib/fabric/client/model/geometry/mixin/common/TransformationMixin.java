/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.fabric.client.model.geometry.mixin.common;

import com.mojang.math.Transformation;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions.TransformationExtensions;
import net.minecraft.core.Direction;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Transformation.class)
public abstract class TransformationMixin implements TransformationExtensions {
	@Shadow
	@Final
	private Matrix4f matrix;

	@Shadow
	public abstract Matrix4f getMatrix();

	@Unique
	private Matrix3f dragonlib$normalTransform = null;

	@Override
	public Matrix3f dragonlib$getNormalMatrix() {
		dragonlib$checkNormalTransform();
		return dragonlib$normalTransform;
	}

	@Unique
	private void dragonlib$checkNormalTransform() {
		if (dragonlib$normalTransform == null) {
			dragonlib$normalTransform = new Matrix3f(this.matrix);
			dragonlib$normalTransform.invert();
			dragonlib$normalTransform.transpose();
		}
	}

	@Override
	public void dragonlib$transformPosition(Vector4f position) {
		position.mul(this.getMatrix());
	}

	@Override
	public Direction dragonlib$rotateTransform(Direction facing) {
		return Direction.rotate(getMatrix(), facing);
	}

	@Override
	public Transformation dragonlib$applyOrigin(Vector3f origin) {
		if (dragonlib$isIdentity()) return Transformation.identity();

		Matrix4f ret = this.getMatrix();
		Matrix4f tmp = new Matrix4f().translation(origin.x(), origin.y(), origin.z());
		tmp.mul(ret, ret);
		tmp.translation(-origin.x(), -origin.y(), -origin.z());
		ret.mul(tmp);
		return new Transformation(ret);
	}
}
