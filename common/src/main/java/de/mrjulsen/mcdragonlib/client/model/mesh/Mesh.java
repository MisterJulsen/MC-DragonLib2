package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.mrjulsen.mcdragonlib.client.model.ModelUtils;
import net.minecraft.core.Direction;

public abstract class Mesh implements ITransformable<Mesh> {

    protected final List<Vertex> vertices;
    protected final List<Edge> edges;
    protected final List<Face> faces;    

    protected Mesh(List<Vertex> vertices, List<Edge> edges, List<Face> faces) {
        this.vertices = vertices;
        this.edges = edges;
        this.faces = faces;
    }

    @Override
    public List<? extends IVertexElement> getTransformableElements() {
        return vertices;
    }

    public void cleanUp() {
        cleanUp(0.0001f, true, true, true);
    }

    public void cleanUp(float threshold, boolean mergeVertices, boolean mergeEdges, boolean mergeFaces) {
        if (mergeVertices) mergeVertices(threshold);
        if (mergeEdges) mergeEdges(threshold);
        if (mergeFaces) mergeFaces(threshold);
    }

    public Map<Vertex, Vertex> mergeVertices(float threshold) {
        Map<Vertex, Vertex> replaced = new HashMap<>(vertices.size());
        List<Vertex> merged = new ArrayList<>(vertices.size());

        for (Vertex v : vertices) {
            boolean found = false;
            for (Vertex existing : merged) {
                if (ModelUtils.positionsIntersect(v.getPos(), existing.getPos(), threshold)) {
                    replaced.put(v, existing);
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(v);
            }
        }

        replaceVertices(merged);

        // --- Post processing ---

        for (Edge edge : edges) {
            edge.updateVertices(x -> replaceFunc(replaced, x));
        }
        for (Face face : faces) {
            face.updateVertices(x -> replaceFunc(replaced, x));
        }

        return replaced;
    }

    protected void replaceVertices(List<Vertex> other) {
        vertices.clear();
        vertices.addAll(other);
    }

    public Map<Edge, Edge> mergeEdges(float threshold) {
        Map<Edge, Edge> replaced = new HashMap<>(edges.size());
        List<Edge> merged = new ArrayList<>(edges.size());

        for (Edge v : edges) {
            boolean found = false;
            for (Edge existing : merged) {
                if (v.overlaps(existing, threshold)) {
                    replaced.put(v, existing);
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(v);
            }
        }

        replaceEdges(merged);

        // --- Post processing ---

        for (Face face : faces) {
            face.updateEdges(x -> replaceFunc(replaced, x));
        }

        return replaced;
    }

    protected void replaceEdges(List<Edge> other) {
        edges.clear();
        edges.addAll(other);
    }
    
    public int mergeFaces(float threshold) {
        int currentFaces = faces.size();
        List<Face> uniqueFaces = new ArrayList<>(faces.size());

        for (Face face : faces) {
            boolean found = false;
            for (Face u : uniqueFaces) {
                if (face.overlaps(u, threshold)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                uniqueFaces.add(face);
            }
        }

        replaceFaces(uniqueFaces);
        return currentFaces - faces.size();
    }

    protected void replaceFaces(List<Face> other) {
        faces.clear();
        faces.addAll(other);
    }

    protected static <T> T replaceFunc(Map<T, T> replacements, T in) {
        if (replacements.containsKey(in)) {
            return replacements.get(in);
        }
        return in;
    }

    public List<Vertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    public List<Face> getFacesOfDirection(Direction direction) {
        if (direction == null) {
            return getFaces();
        }
        ImmutableList.Builder<Face> builder = ImmutableList.builder();
        for (Face face : faces) {
            if (face.getNormalDirection() == direction) {
                builder.add(face);
            }
        }
        return builder.build();
    }
}


