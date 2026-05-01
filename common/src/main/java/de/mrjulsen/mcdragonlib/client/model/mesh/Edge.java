package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;


public class Edge implements ITransformable<Edge> {
    private final List<Vertex> vertices = Arrays.asList(new Vertex[2]);

    public Edge(Vertex a, Vertex b) {
        this.vertices.set(0, a);
        this.vertices.set(1, b);
    }

    @Override
    public List<? extends IVertexElement> getTransformableElements() {
        return vertices;
    }

    void updateVertices(UnaryOperator<Vertex> replaceFunc) {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, replaceFunc.apply(vertices.get(i)));
        }
    }

    public Edge copy(UnaryOperator<Vertex> replaceFunc) {
        return new Edge(replaceFunc.apply(getFirstVertex()), replaceFunc.apply(getSecondVertex()));
    }

    public Vertex getFirstVertex() {
        return vertices.get(0);
    }

    public Vertex getSecondVertex() {
        return vertices.get(1);
    }

    public float length() {
        return vertices.get(0).getPos().distance(vertices.get(1).getPos());
    }
}
