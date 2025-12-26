package com.thizthizzydizzy.dizzyengine.world.object;
import com.thizthizzydizzy.dizzyengine.collision.BoundedObject;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import org.joml.Vector3f;
public abstract class SizedWorldObject extends WorldObject implements BoundedObject{
    private final Vector3f size = new Vector3f();
    public Vector3f getSize(){
        return size.get(new Vector3f());
    }
    public void setSize(Vector3f size){
        if(!this.size.equals(size)&&isStatic()){
            Logger.warn("A static object of type "+getClass().getName()+" just resized!");
            markDirty();
        }
        this.size.set(size);
    }
}
