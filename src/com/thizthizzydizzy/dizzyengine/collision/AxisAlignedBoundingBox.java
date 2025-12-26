package com.thizthizzydizzy.dizzyengine.collision;
import java.util.ArrayList;
import org.joml.Vector3f;
public class AxisAlignedBoundingBox{
    public static AxisAlignedBoundingBox enclosing(ArrayList<? extends BoundedObject> objects){
        if(objects.isEmpty())
            throw new IllegalArgumentException("Cannot create a bounding box enclosing zero objects!");
        AxisAlignedBoundingBox bbox = objects.getFirst().getAxisAlignedBoundingBox();
        for(var object : objects){
            var box = object.getAxisAlignedBoundingBox();
            bbox.min.min(box.min);
            bbox.max.max(box.max);
        }
        return bbox;
    }
    public Vector3f min = new Vector3f();
    public Vector3f max = new Vector3f();
    public AxisAlignedBoundingBox(){
    }
    public AxisAlignedBoundingBox(AxisAlignedBoundingBox basis){
        this(basis.min, basis.max);
    }
    public AxisAlignedBoundingBox(Vector3f min, Vector3f max){
        this.min = new Vector3f(min);
        this.max = new Vector3f(max);
    }
    public boolean contains(Vector3f pos){
        return pos.x>=min.x&&pos.y>=min.y&&pos.z>=min.z&&pos.x<=max.x&&pos.y<=max.y&&pos.z<=max.z;
    }
    public AxisAlignedBoundingBox expand(float border){
        var bbox = new AxisAlignedBoundingBox(this);
        bbox.min.sub(border, border, border);
        bbox.max.add(border, border, border);
        return this;
    }
    public boolean intersects(AxisAlignedBoundingBox other){
        return intersection(other)!=null;
    }
    public AxisAlignedBoundingBox intersection(AxisAlignedBoundingBox other){
        Vector3f newMin = new Vector3f(min).max(other.min);
        Vector3f newMax = new Vector3f(max).min(other.max);
        if(newMin.x>=newMax.x||newMin.y>=newMax.y||newMin.z>newMax.z)return null;
        return new AxisAlignedBoundingBox(newMin, newMax);
    }
    
    public float getWidth(){
        return max.x-min.x;
    }
    public float getHeight(){
        return max.y-min.y;
    }
    public float getArea(){
        return getWidth()*getHeight();
    }
}