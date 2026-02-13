package de.mrjulsen.mcdragonlib.fabric.client.model.obj;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import org.joml.Vector4f;

import com.google.common.collect.Maps;

import joptsimple.internal.Strings;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.resources.ResourceLocation;

public class ObjMaterialLibrary {
	public static final ObjMaterialLibrary EMPTY = new ObjMaterialLibrary();
	final Map<String, Material> materials = Maps.newHashMap();

	private ObjMaterialLibrary() {
	}

	public ObjMaterialLibrary(ObjTokenizer reader) throws IOException {
		Material currentMaterial = null;

		String[] line;
		while ((line = reader.readAndSplitLine(true)) != null) {
			switch (line[0]) {
				case "newmtl": {
					String name = Strings.join(Arrays.copyOfRange(line, 1, line.length), " ");
					currentMaterial = new Material(name);
					materials.put(name, currentMaterial);
					break;
				}
				case "texture":
					currentMaterial.texture = DLUtils.resourceLocation(line[1]);
					break;

				case "Ka":
					currentMaterial.ambientColor = ObjParser.parseVector4(line);
					break;

				case "map_Ka":
					
					currentMaterial.ambientColorMap = line[line.length - 1];
					break;

				case "Kd":
					currentMaterial.diffuseColor = ObjParser.parseVector4(line);
					break;

				case "forge_TintIndex":
					currentMaterial.diffuseTintIndex = Integer.parseInt(line[1]);
					break;

				case "map_Kd":
					
					currentMaterial.diffuseColorMap = line[line.length - 1];
					break;

				case "Ks":
					currentMaterial.specularColor = ObjParser.parseVector4(line);
					break;

				case "Ns":
					currentMaterial.specularHighlight = Float.parseFloat(line[1]);
					break;

				case "map_Ks":
					
					currentMaterial.specularColorMap = line[line.length - 1];
					break;

				case "d":
					
					currentMaterial.dissolve = Float.parseFloat(line[1]);
					break;
				case "Tr":
					
					currentMaterial.transparency = Float.parseFloat(line[1]);
					break;
			}
		}
	}

	public Material getMaterial(String mat) {
		if (!materials.containsKey(mat)) {
			throw new NoSuchElementException("The material was not found in the library: " + mat);
		}
		return materials.get(mat);
	}

	public static class Material {
		public final String name;
		private RenderMaterial material;
		public ResourceLocation texture;
		public Vector4f ambientColor = new Vector4f(0, 0, 0, 1);
		public String ambientColorMap;
		public Vector4f diffuseColor = new Vector4f(1, 1, 1, 1);
		public String diffuseColorMap;
		public Vector4f specularColor = new Vector4f(0, 0, 0, 1);
		public float specularHighlight = 0;
		public String specularColorMap;

		public float dissolve = 1.0f;
		public float transparency = 0.0f;

		
		public int diffuseTintIndex = 0;

		public RenderMaterial getMaterial(Renderer renderer) {
			if (this.material == null) {
				MaterialFinder finder = renderer.materialFinder();

				this.material = finder.find();
			}

			return this.material;
		}

		public Material(String name) {
			this.name = name;
		}
	}
}
