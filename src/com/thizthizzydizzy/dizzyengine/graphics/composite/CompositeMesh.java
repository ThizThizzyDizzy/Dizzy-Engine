package com.thizthizzydizzy.dizzyengine.graphics.composite;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Mesh;
public class CompositeMesh extends Mesh{
    private final Mesh baseMesh;
    public CompositeMesh(Mesh baseMesh){
        this.baseMesh = baseMesh;
        
        //TODO combine triangles/quads in the same plane and build a composite texture
        verticies.addAll(baseMesh.verticies);
        triangles.addAll(baseMesh.triangles);
    }
}
