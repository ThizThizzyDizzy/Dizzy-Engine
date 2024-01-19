package com.thizthizzydizzy.dizzyengine.gui;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import static org.lwjgl.opengl.GL11.*;
public class FlatGUI extends DizzyLayer{
    public Menu menu;
    public final Vector2f size = new Vector2f();
    public final Vector2d[] cursorPosition = new Vector2d[DizzyEngine.CURSOR_LIMIT];
    private final Matrix4f viewMatrix = new Matrix4f().setTranslation(0, 0, -5);
    private final Matrix4f projectionMatrix = new Matrix4f();
    @Override
    public void init(){
    }
    @Override
    public void render(double deltaTime){
        if(menu==null)return;
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        Renderer.view(viewMatrix);
        Renderer.projection(projectionMatrix);
        menu.render(deltaTime);
    }
    @Override
    public void cleanup(){
    }
    public <T extends Menu> T open(T menu){
        if(this.menu!=null)this.menu.onMenuClosed();
        this.menu = menu;
        if(menu!=null){
            menu.size.set(size);
            menu.onMenuOpened();
        }
        return menu;
    }
    @Override
    protected void onChar(int id, int codepoint){
        if(menu!=null)menu.onChar(id, codepoint);
    }
    @Override
    protected void onCharMods(int id, int codepoint, int mods){
        if(menu!=null)menu.onCharMods(id, codepoint, mods);
    }
    @Override
    protected void onCursorEnter(int id, boolean entered){
        if(entered){
            if(cursorPosition[id]==null)cursorPosition[id] = new Vector2d();
        }else{
            cursorPosition[id] = null;
        }
    }
    @Override
    protected void onCursorPos(int id, double xpos, double ypos){
        onCursorEnter(id, true);
        cursorPosition[id].set(xpos, ypos);
        if(menu!=null)menu.onCursorPos(id, xpos, ypos);
    }
    @Override
    protected void onKey(int id, int key, int scancode, int action, int mods){
        if(menu!=null)menu.onKey(id, key, scancode, action, mods);
    }
    @Override
    protected void onMouseButton(int id, int button, int action, int mods){
        if(menu!=null)menu.onMouseButton(id, cursorPosition[id], button, action, mods);
    }
    @Override
    protected void onScroll(int id, double xoffset, double yoffset){
        if(menu!=null)menu.onScroll(id, cursorPosition[id], xoffset, yoffset);
    }
    @Override
    protected void onDrop(int id, int count, long names){
        if(menu!=null)menu.onDrop(id, cursorPosition[id], count, names);
    }
    @Override
    protected void onJoystick(int id, int jid, int event){
        if(menu!=null)menu.onJoystick(id, jid, event);
    }
    @Override
    protected void onScreenSize(Vector2i screenSize){
        size.set(screenSize);
        if(menu!=null)menu.size.set(size);
        projectionMatrix.setOrtho(0, size.x, size.y, 0, 0.1f, 10f);
    }
}
