package com.thizthizzydizzy.dizzyengine.graphics.batch;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Mesh;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
public interface Instanceable{
    public boolean canInstance(WorldObject other);
    public Mesh getMesh();
}
