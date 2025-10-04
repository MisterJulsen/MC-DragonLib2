package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.Objects;

import org.joml.Vector3f;

import de.mrjulsen.mcdragonlib.util.Color;

public class Vertex implements IVertexElement {

    private Vector3f pos = new Vector3f();
    private Vector3f normal = new Vector3f();
    private Color vertexColor = Color.WHITE;

    private Vertex() {}

    public Vertex(float[] pos, float[] normal, int[] color) {
        this.pos = new Vector3f(pos);
        this.normal = new Vector3f(normal);
        setColor(Color.of(color[3], color[0], color[1], color[2]));
    }

    public Vertex(Vector3f pos, Vector3f normal, Color color) {
        this.pos = pos;
        this.normal = normal;
        this.vertexColor = color;
    }

    public Vertex copy() {
        Vertex v = new Vertex();
        v.pos = new Vector3f(pos);
        v.normal = new Vector3f(normal);
        v.vertexColor = vertexColor;
        return v;
    }

    @Override
    public Vector3f getPos() {
        return pos;
    }

    public float[] getPosAsArray() {
        return new float[] { getX(), getY(), getZ() };
    }

    public float getX() {
        return getPos().x;
    }

    public float getY() {
        return getPos().y;
    }

    public float getZ() {
        return getPos().z;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public float[] getNormalAsArray() {
        return new float[] { getNormal().x, getNormal().y, getNormal().z };
    }

    public Color getColor() {
        return vertexColor;
    }

    public int[] getColorRGBAAsArray() {
        return new int[] { getColor().getRed(), getColor().getGreen(), getColor().getBlue(), getColor().getAlpha() };
    }

    public void setPos(Vector3f pos) {
        Objects.requireNonNull(pos);
        this.pos = pos;
    }

    public void setPos(float x, float y, float z) {
        this.setPos(new Vector3f(x, y, z));
    }

    public void setX(float x) {
        this.getPos().set(x, getPos().y, getPos().z);
    }

    public void setY(float y) {
        this.getPos().set(getPos().x, y, getPos().z);
    }

    public void setZ(float z) {
        this.getPos().set(getPos().x, getPos().y, z);
    }

    public void setNormal(Vector3f normal) {
        Objects.requireNonNull(normal);
        this.normal = normal;
    }

    public void setColor(Color color) {
        Objects.requireNonNull(color);
        this.vertexColor = color;
    }
}
