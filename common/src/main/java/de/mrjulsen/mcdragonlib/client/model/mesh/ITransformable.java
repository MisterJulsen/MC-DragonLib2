package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import de.mrjulsen.mcdragonlib.client.model.ModelUtils;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ITransformable<T extends ITransformable<T>> {
    List<? extends IVertexElement> getTransformableElements();

    default void translate(float x, float y, float z) {
        this.translate(new Vector3f(x, y, z));
    }

    default void translate(Vector3f delta) {
        for (IVertexElement vertex : getTransformableElements()) {
            vertex.getPos().add(delta);
        }
    }

    default void scale(float x, float y, float z, Vector3f pivot) {
        this.scale(new Vector3f(x, y, z), pivot);
    }

    default void scale(Vector3f factor, Vector3f pivot) {
        for (IVertexElement v : getTransformableElements()) {
            v.getPos().sub(pivot).mul(factor).add(pivot);
        }
    }

    default void rotate(Quaternionf rotation, Vector3f pivot) {
        for (IVertexElement v : getTransformableElements()) {
            Vector3f relative = new Vector3f(v.getPos()).sub(pivot);
            rotation.transform(relative);
            v.getPos().set(relative.add(pivot));
        }
    }

    default void rotateTo(Vector3f direction, Vector3f pivot) {
        Vector3f dir = new Vector3f(direction).normalize();
        float yaw = (float) Math.atan2(-dir.x, -dir.z);
        float pitch = (float) Math.asin(dir.y);
        this.rotate(new Quaternionf().rotateY(yaw).rotateX(pitch), pivot);
    }



    default void transform(Matrix4f matrix) {
        for (IVertexElement v : getTransformableElements()) {
            Vector4f temp = new Vector4f(v.getPos(), 1.0f);
            matrix.transform(temp);
            v.getPos().set(temp.x(), temp.y(), temp.z());
        }
    }

    default boolean overlaps(T other, float threshold) {
        if (other == this) {
            return false;
        }

        for (int i = 0; i < getTransformableElements().size(); i++) {
            IVertexElement s = getTransformableElements().get(i);
            IVertexElement o = other.getTransformableElements().get(i);
            if (!ModelUtils.positionsIntersect(s.getPos(), o.getPos(), threshold)) {
                return false;
            }
        }
        return true;
    }

    default Vector3f center() {
        Vector3f center = new Vector3f();
        List<? extends IVertexElement> elements = getTransformableElements();
        if (elements.isEmpty()) {
            return center;
        }
        
        for (IVertexElement v : elements) {
            center.add(v.getPos());
        }
        center.div(elements.size());
        return center;
    }
    
    default void centerTo(Vector3f target) {
        Vector3f currentCenter = this.center();
        Vector3f delta = new Vector3f(target).sub(currentCenter);
        this.translate(delta);
    }

    default void projectOntoPlane(Vector3f planeNormal, Vector3f planePoint) {
        Vector3f n = new Vector3f(planeNormal).normalize();

        for (IVertexElement v : getTransformableElements()) {
            Vector3f p = v.getPos();
            Vector3f toPoint = new Vector3f(p).sub(planePoint);
            float distance = toPoint.dot(n);
            Vector3f projection = new Vector3f(n).mul(distance);
            p.sub(projection);
        }
    }

    default void mirror(Axis axis, float pivot) {
        for (IVertexElement v : getTransformableElements()) {
            Vector3f pos = v.getPos();
            switch (axis) {
                case X -> pos.x = pivot - (pos.x - pivot);
                case Y -> pos.y = pivot - (pos.y - pivot);
                case Z -> pos.z = pivot - (pos.z - pivot);
            }
        }
    }
    
    default void alignToAxis(Vector3f targetAxis, Vector3f up) {
        Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0, 0, 1), targetAxis);
        Vector3f pivot = this.center();
        this.rotate(rotation, pivot);
    }

    default AABB getBoundingBox() {
        List<? extends IVertexElement> elements = getTransformableElements();
        if (elements.isEmpty()) return new AABB(0, 0, 0, 0, 0, 0);

        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        for (IVertexElement v : elements) {
            Vector3f pos = v.getPos();
            min.min(pos);
            max.max(pos);
        }

        return new AABB(new Vec3(min), new Vec3(max));
    }

    default void normalize() {
        AABB box = getBoundingBox();
        Vector3f min = new Vector3f((float)box.minX, (float)box.minY, (float)box.minZ);
        Vector3f max = new Vector3f((float)box.maxX, (float)box.maxY, (float)box.maxZ);
        Vector3f size = new Vector3f(max).sub(min);

        for (IVertexElement v : getTransformableElements()) {
            Vector3f pos = v.getPos();
            pos.sub(min).div(size);
        }
    }
    
    default void invert() {
        for (IVertexElement v : getTransformableElements()) {
            v.getPos().negate();
        }
    }
    
    default boolean contains(Vector3f point, float threshold) {
        for (IVertexElement v : getTransformableElements()) {
            if (v.getPos().distance(point) < threshold) {
                return true;
            }
        }
        return false;
    }

}
