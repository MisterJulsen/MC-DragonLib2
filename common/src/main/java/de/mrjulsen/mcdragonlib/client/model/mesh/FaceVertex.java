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

package de.mrjulsen.mcdragonlib.client.model.mesh;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class FaceVertex implements IVertexElement {

    private Vertex vertex;

    // Metadata
    private Vector2f uv;
    private Vector2i light;

    public FaceVertex(Vertex vertex, float[] uv, int[] light) {
        this.vertex = vertex;
        this.uv = new Vector2f(uv);
        this.light = new Vector2i(light);
    }

    @Override
    public Vector3f getPos() {
        return vertex.getPos();
    }

    public Vertex getVertex() {
        return vertex;
    }

    public Vector2f getUV() {
        return uv;
    }

    public float getU() {
        return getUV().x;
    }

    public float getV() {
        return getUV().y;
    }

    public float[] getUVAsArray() {
        return new float[] { getU(), getV() };
    }

    public Vector2i getLight() {
        return light;
    }

    public int[] getLightAsArray() {
        return new int[] { getLight().x, getLight().y };
    }

    public int getPackedLight() {
        return (getLight().y << 16) | (getLight().x & 0xFFFF);
    }

    public void updateVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public void setUV(Vector2f uv) {
        this.uv = uv;
    }

    public void setU(float u) {
        this.getUV().set(u, getV());
    }

    public void setV(float v) {
        this.getUV().set(getU(), v);
    }

    public void setLight(Vector2i light) {
        this.light = light;
    }

    public void setLight(int packedLight) {
        int lu = packedLight & 0xFFFF;
        int lv = (packedLight >> 16) & 0xFFFF;
        this.setLight(new Vector2i(lu, lv));
    }
}
