package com.thizthizzydizzy.dizzyengine.debug.performance;
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
    public void cleanup(){
    }
    @Override
    protected void onKey(int id, int key, int scancode, int action, int mods){
        if(action!=GLFW.GLFW_PRESS)return;
        if(key==GLFW.GLFW_KEY_F11){
            currentPage++;
            if(currentPage>numPages)currentPage = 0;
        }
    }
    @Override
    public void render(double deltaTime){
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        Renderer.view(viewMatrix);
        Renderer.projection(new Matrix4f().ortho(0, DizzyEngine.screenSize.x, DizzyEngine.screenSize.y, 0, 10, -10));
        Renderer.setColor(Color.WHITE);
        lineOffset = 0;
        if(currentPage>0)text("DizzyEngine Performance Overlay - Page "+currentPage+"/"+numPages);
        switch(currentPage){
            case 1 -> {
                text("=== Performance Tracker: Counters ===");
                drawPerfTrackerGroup("GLOBAL", PerformanceTracker.rootGroup, 0);
            }
        }
    }
    private float lineHeight = 20;
    private int lineOffset = 0;
    private void text(String str){
        Renderer.drawText(0, lineHeight*lineOffset, str, lineHeight);
        lineOffset++;
    }
    private void drawPerfTrackerGroup(String name, PerformanceTrackerGroup group, int depth){
        String prefix = "";
        for(int i = 0; i<depth; i++)prefix+=" | ";
        text(prefix+"-- "+name+" --");
        prefix+=" | ";
        for(var counter : group.counters.entrySet().stream().sorted((e1,e2)->e1.getValue()-e2.getValue()).toList()){
            text(prefix+counter.getKey()+": "+counter.getValue());
        }
        for(var subgroup : group.subgroups.entrySet().stream().sorted((e1,e2)->e1.getKey().compareTo(e2.getKey())).toList()){
            drawPerfTrackerGroup(subgroup.getKey(), subgroup.getValue(), depth+1);
        }
    }
}
