package com.thizthizzydizzy.dizzyengine.graphics.mesh.builder;
import org.joml.Vector2f;
import org.joml.Vector3f;
public class AxialQuadMeshBuilder extends MeshBuilder{
    public AxialQuadMeshBuilder quadXY(float x1, float y1, float x2, float y2, float z, boolean normalDirection, float u1, float v1, float u2, float v2){
        Vector3f normalVec = new Vector3f(0, 0, normalDirection?1:-1);
        int ul = getVertex(new Vector3f(x1, y1, z), normalVec, new Vector2f(u1, v2));
        int ll = getVertex(new Vector3f(x1, y2, z), normalVec, new Vector2f(u1, v1));
        int ur = getVertex(new Vector3f(x2, y1, z), normalVec, new Vector2f(u2, v2));
        int lr = getVertex(new Vector3f(x2, y2, z), normalVec, new Vector2f(u2, v1));
        quad(ul, ll, ur, lr);
        return this;
    }
    public AxialQuadMeshBuilder quadXZ(float x1, float z1, float x2, float z2, float y, boolean normalDirection, float u1, float v1, float u2, float v2){
        Vector3f normalVec = new Vector3f(0, normalDirection?1:-1, 0);
        int ul = getVertex(new Vector3f(x1, y, z1), normalVec, new Vector2f(u1, v1));
        int ll = getVertex(new Vector3f(x1, y, z2), normalVec, new Vector2f(u1, v2));
        int ur = getVertex(new Vector3f(x2, y, z1), normalVec, new Vector2f(u2, v1));
        int lr = getVertex(new Vector3f(x2, y, z2), normalVec, new Vector2f(u2, v2));
        quad(ul, ll, ur, lr);
        return this;
    }
    public AxialQuadMeshBuilder quadYZ(float y1, float z1, float y2, float z2, float x, boolean normalDirection, float u1, float v1, float u2, float v2){
        Vector3f normalVec = new Vector3f(normalDirection?1:-1, 0, 0);
        int ul = getVertex(new Vector3f(x, y1, z1), normalVec, new Vector2f(u1, v1));
        int ll = getVertex(new Vector3f(x, y1, z2), normalVec, new Vector2f(u1, v2));
        int ur = getVertex(new Vector3f(x, y2, z1), normalVec, new Vector2f(u2, v1));
        int lr = getVertex(new Vector3f(x, y2, z2), normalVec, new Vector2f(u2, v2));
        quad(ul, ll, ur, lr);
        return this;
    }
}
