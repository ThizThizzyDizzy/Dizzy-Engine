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

        drawObjects();
    }
    public abstract void renderWorldBackground(Vector3i chunk, double deltaTime);
    public float getShearFactor(){
        return shearFactor;
    }
}
