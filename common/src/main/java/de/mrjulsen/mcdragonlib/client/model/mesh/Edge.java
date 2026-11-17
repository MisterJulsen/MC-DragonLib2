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
