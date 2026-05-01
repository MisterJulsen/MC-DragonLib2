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
