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

package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Override
    public List<Face> getFaces() {
        List<Face> result = new ArrayList<>(faces.size());
        for (Face face : faces) {
            if (face != null) {
                result.add(face);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public Face removeFace(Direction direction) {
        Map<Vertex, Vertex> replacements = new HashMap<>(4);
        Face face = faces.get(direction.get3DDataValue());
        if (face == null) {
            return null;
        }
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
