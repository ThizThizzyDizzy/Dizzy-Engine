package com.thizthizzydizzy.dizzyengine.world.flat.object;
import com.thizthizzydizzy.dizzyengine.collision.AxisAlignedBoundingBox;
import com.thizthizzydizzy.dizzyengine.graphics.batch.Instanceable;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Mesh;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.builder.AxialQuadMeshBuilder;
import com.thizthizzydizzy.dizzyengine.world.object.SizedWorldObject;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
public class ThreeQuarterWorldObject extends SizedWorldObject implements Instanceable{
    private Mesh mesh;
    @Override
    public AxisAlignedBoundingBox getAxisAlignedBoundingBox(){
        AxisAlignedBoundingBox bbox = new AxisAlignedBoundingBox();
        bbox.min.set(getSize()).mul(-.5f).add(getPosition()).add(0, 0, getSize().z/2);
        bbox.max.set(getSize()).mul(.5f).add(getPosition()).add(0, 0, getSize().z/2);
        return bbox;
    }
    @Override
    public boolean canInstance(WorldObject other){
        return other instanceof ThreeQuarterWorldObject tqwo&&getSize().equals(tqwo.getSize());
    }
    @Override
    public Mesh getMesh(){
        if(mesh==null)mesh = generateMesh();
        return mesh;
    }
    protected Mesh generateMesh(){
        var builder = new AxialQuadMeshBuilder();
        float totalTextureHeight = getSize().y+getSize().z;
        if(getSize().y>0)builder.quadXY(-getSize().x/2, -getSize().y/2, getSize().x/2, getSize().y/2, getSize().z, true, 0, getSize().z/totalTextureHeight, 1, 1);
        if(getSize().z>0)builder.quadXZ(-getSize().x/2, 0, getSize().x/2, getSize().z, getSize().y/2, true, 0, 0, 1, getSize().z/totalTextureHeight);
        return builder.build();
    }
}
