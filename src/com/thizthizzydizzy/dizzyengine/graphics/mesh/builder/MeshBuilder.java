package com.thizthizzydizzy.dizzyengine.graphics.mesh.builder;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Mesh;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Triangle;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
public class MeshBuilder{
    protected Mesh mesh = new Mesh();
    public MeshBuilder vertex(Vector3f position, Vector3f normal, Vector2f uv){
        Vertex vertex = new Vertex();
        vertex.setPosition(position);
        vertex.setNormal(normal);
        vertex.setUV(uv);
        mesh.verticies.add(vertex);
        return this;
    }
    public int getVertex(Vector3f position, Vector3f normal, Vector2f uv){
        int idx = mesh.verticies.size();
        vertex(position, normal, uv);
        return idx;
    }
    public MeshBuilder triangle(int i1, int i2, int i3){
        Triangle triangle = new Triangle();
        triangle.data[0] = i1;
        triangle.data[1] = i2;
        triangle.data[2] = i3;
        mesh.triangles.add(triangle);
        return this;
    }
    public MeshBuilder quad(int ul, int ll, int ur, int lr){
        return triangle(ll, ul, ur).triangle(lr, ll, ur);
    }
    public Mesh build(){
        return mesh;
    }
}
