package com.thizthizzydizzy.dizzyengine.world.flat;
import org.joml.Matrix4f;
import org.joml.Vector3i;
public abstract class ThreeQuarterWorldLayer extends FlatWorldLayer{
    private float shearFactor = 1;
    @Override
    public Matrix4f createProjectionMatrix(){
        var projection = super.createProjectionMatrix();
        Matrix4f shear = new Matrix4f().identity();
        shear.m21(-shearFactor); // add shear
        return projection.mul(shear);
    }
    @Override
    public void renderWorld(Vector3i chunk, double deltaTime){
        renderWorldBackground(chunk, deltaTime);
        // sorting is not neccesary if rendering 3D-style
//        objects.sort((o1, o2) -> {
//            float diff = o1.position.y-o2.position.y;
//            if(Math.abs(diff)>0.001f)return 0;
//            return diff>0?1:-1;
//        });
    }
    public abstract void renderWorldBackground(Vector3i chunk, double deltaTime);
    public float getShearFactor(){
        return shearFactor;
    }
}
