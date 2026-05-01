package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import net.minecraft.core.Direction;

public class CubeMesh extends Mesh {

    public CubeMesh(Vector3f position) {
        this(position, new Vector3f(1, 1, 1));
    }

    public CubeMesh(Vector3f position, Vector3f size) {
        super(new ArrayList<>(), new ArrayList<>(), Arrays.asList(new Face[6]));
        for (int i = 0; i < Direction.values().length; i++) {
            Direction side = Direction.from3DDataValue(i);
            Vector3f facePos = new Vector3f(side.getNormal().getX(), side.getNormal().getY(), side.getNormal().getZ()).max(new Vector3f());
            Face face = Face.createFace(side, facePos, 1, 1);
            setFace(side, face);
        }
        cleanUp();
        scale(size, new Vector3f());
        translate(position);
    }

    @Override
    protected void replaceFaces(List<Face> other) {
        for (int i = 0; i < other.size() && i < faces.size(); i++) {
            faces.set(i, other.get(i));
        }
        
    }

    protected void setFace(Direction direction, Face face) {
        faces.set(direction.get3DDataValue(), face);
        edges.addAll(face.getEdges());
        for (FaceVertex wrapper : face.getCorners()) {
            vertices.add(wrapper.getVertex());
        }
    }

    public Face removeFace(Direction direction) {
        Map<Vertex, Vertex> replacements = new HashMap<>(4);
        Face face = faces.get(direction.get3DDataValue());
        faces.set(direction.get3DDataValue(), null);
        for (FaceVertex wrapper : face.getCorners()) {
            Vertex current = wrapper.getVertex();
            Vertex copy = current.copy();
            wrapper.updateVertex(copy);
            replacements.put(current, copy);
        }        
        face.updateEdges(x -> x.copy(y -> replaceFunc(replacements, y)));
        return face;
    }

    public Face getFaceOnSide(Direction direction) {
        return faces.get(direction.get3DDataValue());
    }
}
