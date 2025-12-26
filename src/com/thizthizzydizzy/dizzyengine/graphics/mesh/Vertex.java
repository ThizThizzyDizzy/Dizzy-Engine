package com.thizthizzydizzy.dizzyengine.graphics.mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
public class Vertex{
    public static final int SIZE = 8;
    private final float[] data = new float[SIZE];
    public void setPosition(Vector3f position){
        data[0] = position.x;
        data[1] = position.y;
        data[2] = position.z;
    }
    public void setNormal(Vector3f normal){
        data[3] = normal.x;
        data[4] = normal.y;
        data[5] = normal.z;
    }
    public void setUV(Vector2f uv){
        data[6] = uv.x;
        data[7] = uv.y;
    }
    public float[] getGLData(){
        return data;
    }
}
