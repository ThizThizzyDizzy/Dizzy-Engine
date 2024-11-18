package com.thizthizzydizzy.dizzyengine.debug;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
public class NoclipProcessor extends DizzyLayer{
    private static NoclipProcessor layer;
    public static float speedMultiplier = 1f;
    public static float rotationSpeedMultiplier = 1f;
    public static void initialize(){
        if(layer==null)DizzyEngine.addLayer(layer = new NoclipProcessor());
    }
    public static void transformMatrix(){
        transformMatrix(new Matrix4f());
    }
    public static void transformMatrix(Matrix4f base){
        initialize();
        Renderer.view(base.rotate(layer.rotation.conjugate(new Quaternionf())).translate(layer.position.mul(-1, new Vector3f())));
    }
    public static void reset(){
        initialize();
        layer.init();
    }
    private Vector3f position;
    private Quaternionf rotation;
    {
        init();
    }
    @Override
    public void init(){
        position = new Vector3f();
        rotation = new Quaternionf();
    }
    @Override
    public void render(double deltaTime){
        Vector3f direction = new Vector3f(0, 0, 0);
        float multiplierMultiplier = 1;
        boolean control = DizzyEngine.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)||DizzyEngine.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean shift = DizzyEngine.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)||DizzyEngine.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
        if(control&&shift){
            multiplierMultiplier *= 100;
        }else{
            if(control)multiplierMultiplier /= 10;
            if(shift)multiplierMultiplier *= 10;
        }
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_W))direction.z--;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_S))direction.z++;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_A))direction.x--;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_D))direction.x++;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_F))direction.y--;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_R))direction.y++;
        Vector3f rotationDirection = new Vector3f(0, 0, 0);
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_UP))rotationDirection.x++;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_DOWN))rotationDirection.x--;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_LEFT))rotationDirection.y++;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_RIGHT))rotationDirection.y--;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_Q))rotationDirection.z++;
        if(DizzyEngine.isKeyDown(GLFW.GLFW_KEY_E))rotationDirection.z--;
        rotationDirection.mul((float)(deltaTime*rotationSpeedMultiplier*Math.sqrt(multiplierMultiplier))); // affect rotation speed a lot less
        rotation.mul(new Quaternionf().rotateXYZ(rotationDirection.x, rotationDirection.y, rotationDirection.z));
        position.add(rotation.transform(direction.mul((float)(deltaTime*speedMultiplier*multiplierMultiplier))));
    }
    @Override
    public void cleanup(){
    }
}
