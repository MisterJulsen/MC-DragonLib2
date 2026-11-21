/*
 * DragonLib 3 - 1.21.1
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

package de.mrjulsen.mcdragonlib.fabric.client.model.geometry;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;

import de.mrjulsen.mcdragonlib.fabric.client.model.geometry.extensions.TransformationExtensions;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

public final class QuadTransformers {

	private static final RenderContext.QuadTransform EMPTY = quad -> {
		return true;
	};

	private static final RenderContext.QuadTransform[] EMISSIVE_TRANSFORMERS = Util.make(new RenderContext.QuadTransform[16], array -> {
		Arrays.setAll(array, i -> applyingLightmap(LightTexture.pack(i, i)));
	});

		public static RenderContext.QuadTransform empty() {
		return EMPTY;
	}

		public static RenderContext.QuadTransform applying(Transformation transform) {
		TransformationExtensions transformExt = (TransformationExtensions)(Object)transform;
		if (transformExt.dragonlib$isIdentity())
			return empty();
		return quad -> {
			for (int i = 0; i < 4; i++) {
				float x = quad.x(i);
				float y = quad.y(i);
				float z = quad.z(i);

				Vector4f pos = new Vector4f(x, y, z, 1);
				transformExt.dragonlib$transformPosition(pos);
				pos.div(pos.w);

				quad.pos(i, pos.x(), pos.y(), pos.z());
			}

			for (int i = 0; i < 4; i++) {
				if (quad.hasNormal(i)) {
					float x = quad.normalX(i);
					float y = quad.normalY(i);
					float z = quad.normalZ(i);

					Vector3f pos = new Vector3f(x, y, z);
					transformExt.dragonlib$transformNormal(pos);

					quad.normal(i, pos);
				}
			}
			return true;
		};
	}

		public static RenderContext.QuadTransform applyingLightmap(int packedLight) {
		return quad -> {
			for (int i = 0; i < 4; i++)
				quad.lightmap(i, packedLight);
			return true;
		};
	}

		public static RenderContext.QuadTransform applyingLightmap(int blockLight, int skyLight) {
		return applyingLightmap(LightTexture.pack(blockLight, skyLight));
	}

		public static RenderContext.QuadTransform settingEmissivity(int emissivity) {
		Preconditions.checkArgument(emissivity >= 0 && emissivity < 16, "Emissivity must be between 0 and 15.");
		return EMISSIVE_TRANSFORMERS[emissivity];
	}

		public static RenderContext.QuadTransform settingMaxEmissivity() {
		return EMISSIVE_TRANSFORMERS[15];
	}

		@SuppressWarnings("deprecation")
	public static RenderContext.QuadTransform applyingColor(int color) {
		final int fixedColor = toABGR(color);
		return quad -> {
			for (int i = 0; i < 4; i++)
				quad.spriteColor(i, 0, fixedColor);
			return true;
		};
	}

		public static RenderContext.QuadTransform applyingColor(int red, int green, int blue) {
		return applyingColor(255, red, green, blue);
	}

		public static RenderContext.QuadTransform applyingColor(int alpha, int red, int green, int blue) {
		return applyingColor(alpha << 24 | red << 16 | green << 8 | blue);
	}

		public static int toABGR(int color) {
		return (color & 0xFF00FF00) 
				| ((color >> 16) & 0x000000FF) 
				| ((color << 16) & 0x00FF0000); 
	}

	private QuadTransformers() {}
}
