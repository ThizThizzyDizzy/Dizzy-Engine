package com.thizthizzydizzy.dizzyengine.debug;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
public class PerformanceOverlay extends DizzyLayer{
    {
        zIndex = Z_INDEX_OFFSET_DEBUG; // NOTHING should ever render over the debug performance overlay.
    }
    private static PerformanceOverlay layer;
    private final Matrix4f viewMatrix = new Matrix4f().setTranslation(0, 0, -4);
    private final Matrix4f projectionMatrix = new Matrix4f().ortho(0, 100, 100, 0, 10, -10);
    public static void initialize(){
        if(layer==null)DizzyEngine.addLayer(layer = new PerformanceOverlay());
    }
    private int currentPage = 0;
    private static final int numPages = 1;
    @Override
    public void init(){
    }
    @Override
    public void render(double deltaTime){
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        Renderer.view(viewMatrix);
        Renderer.projection(new Matrix4f().ortho(0, DizzyEngine.screenSize.x, DizzyEngine.screenSize.y, 0, 10, -10));
        float lineHeight = 20;
        Renderer.setColor(Color.WHITE);
        if(currentPage>0)Renderer.drawText(0, 0, "DizzyEngine Performance Overlay - Page "+currentPage+"/"+numPages, lineHeight);
        switch(currentPage){
            case 1 -> {
                Renderer.drawText(0, lineHeight, "Draw Calls: "+PerformanceTracker.drawCalls, lineHeight);
                Renderer.drawText(0, lineHeight*2, "Shader Performance", lineHeight);
                Renderer.drawText(0, lineHeight*3, "- glUniform1f: "+PerformanceTracker.glUniform1f, lineHeight);
                Renderer.drawText(0, lineHeight*4, "- glUniform2f: "+PerformanceTracker.glUniform2f, lineHeight);
                Renderer.drawText(0, lineHeight*5, "- glUniform3f: "+PerformanceTracker.glUniform3f, lineHeight);
                Renderer.drawText(0, lineHeight*6, "- glUniform4f: "+PerformanceTracker.glUniform4f, lineHeight);
                Renderer.drawText(0, lineHeight*7, "- glUniform1i: "+PerformanceTracker.glUniform1i, lineHeight);
                Renderer.drawText(0, lineHeight*8, "- glUniformMatrix4f: "+PerformanceTracker.glUniformMatrix4f, lineHeight);
            }
        }
    }
    @Override
    public void cleanup(){
    }
    @Override
    protected void onKey(int id, int key, int scancode, int action, int mods){
        if(action==GLFW.GLFW_PRESS&&key==GLFW.GLFW_KEY_F11){
            currentPage++;
            if(currentPage>numPages)currentPage = 0;
        }
    }
}
