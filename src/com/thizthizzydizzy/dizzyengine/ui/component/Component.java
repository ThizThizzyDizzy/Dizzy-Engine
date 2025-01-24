package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.ui.UILayer;
import java.util.ArrayList;
import org.joml.Vector2d;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;
public class Component{
    public Component parent;
    public float x, y;
    private final Vector2f size = new Vector2f(0, 0);
    public boolean[] isFocused = new boolean[DizzyEngine.CURSOR_LIMIT];
    public boolean[] isCursorFocused = new boolean[DizzyEngine.CURSOR_LIMIT];
    public Component[] focusedComponent = new Component[DizzyEngine.CURSOR_LIMIT];
    public Component[] cursorFocusedComponent = new Component[DizzyEngine.CURSOR_LIMIT];
    public boolean focusable = true;
    public ArrayList<Component> components = new ArrayList<>(){
        @Override
        public void clear(){
            super.clear();
            for(int i = 0; i<focusedComponent.length; i++){
                focusedComponent[i] = cursorFocusedComponent[i] = null;
            }
        }
        @Override
        public boolean remove(Object o){
            for(int i = 0; i<focusedComponent.length; i++){
                if(o==focusedComponent[i])focusedComponent[i] = null;
                if(o==cursorFocusedComponent[i])cursorFocusedComponent[i] = null;
            }
            return super.remove(o);
        }
    };
    public void render(double deltaTime){
//        Renderer.bound(x, y, x+size.x, y+size.y); //TODO fix bound- this doesn't work with BorderLayout for some reason
        draw(deltaTime);
        Renderer.translate(x, y);
        for(var c : components)c.render(deltaTime);
        Renderer.unTranslate();
//        Renderer.unBound();
    }
    public void draw(double deltaTime){
    }
    public <T extends Component> T add(T component){
        components.add(component);
        component.parent = this;
        component.onAdded();
        return component;
    }
    public void onAdded(){
    }
    public void onChar(int id, int codepoint){
        if(focusedComponent[id]!=null)focusedComponent[id].onChar(id, codepoint);
    }
    public void onCharMods(int id, int codepoint, int mods){
        if(focusedComponent[id]!=null)focusedComponent[id].onCharMods(id, codepoint, mods);
    }
    public void onCursorPos(int id, double xpos, double ypos){
        boolean foundFocus = false;
        for(var c : components){
            if(c.focusable&&xpos>=c.x&&ypos>=c.y&&xpos<c.x+c.size.x&&ypos<c.y+c.size.y){
                foundFocus = true;
                if(cursorFocusedComponent[id]!=c){
                    if(cursorFocusedComponent[id]!=null){
                        cursorFocusedComponent[id].isCursorFocused[id] = false;
                        cursorFocusedComponent[id].onCursorFocusLost(id);
                    }
                    cursorFocusedComponent[id] = c;
                    cursorFocusedComponent[id].isCursorFocused[id] = true;
                    cursorFocusedComponent[id].onCursorFocusGained(id);
                }
            }
        }
        if(!foundFocus){
            if(cursorFocusedComponent[id]!=null){
                cursorFocusedComponent[id].isCursorFocused[id] = false;
                cursorFocusedComponent[id].onCursorFocusLost(id);
            }
            cursorFocusedComponent[id] = null;
        }
        if(cursorFocusedComponent[id]!=null)cursorFocusedComponent[id].onCursorPos(id, xpos-cursorFocusedComponent[id].x, ypos-cursorFocusedComponent[id].y);
    }
    public void onKey(int id, int key, int scancode, int action, int mods){
        if(focusedComponent[id]!=null)focusedComponent[id].onKey(id, key, scancode, action, mods);
    }
    public void onMouseButton(int id, Vector2d pos, int button, int action, int mods){
        if(cursorFocusedComponent[id]!=null){
            //adjust focus
            if(action==GLFW_RELEASE&&focusedComponent[id]!=null){//send focused component release event, even if no longer cursor focused
                focusedComponent[id].onMouseButton(id, new Vector2d(pos.x-focusedComponent[id].x, pos.y-focusedComponent[id].y), button, action, mods);
            }
            if(action==GLFW_PRESS){
                if(focusedComponent[id]!=cursorFocusedComponent[id]){
                    if(focusedComponent[id]!=null){
                        focusedComponent[id].isFocused[id] = false;
                        focusedComponent[id].onFocusLost(id);
                    }
                    focusedComponent[id] = cursorFocusedComponent[id];
                    focusedComponent[id].isFocused[id] = true;
                    focusedComponent[id].onFocusGained(id);
                }
            }
            //focus is adjusted, now pass along mouse button
            cursorFocusedComponent[id].onMouseButton(id, new Vector2d(pos.x-cursorFocusedComponent[id].x, pos.y-cursorFocusedComponent[id].y), button, action, mods);
        }else if(action==GLFW_RELEASE&&focusedComponent[id]!=null){
            focusedComponent[id].onMouseButton(id, new Vector2d(pos.x-focusedComponent[id].x, pos.y-focusedComponent[id].y), button, action, mods);
        }
    }
    public boolean onScroll(int id, Vector2d pos, double dx, double dy){
        if(cursorFocusedComponent[id]!=null)return cursorFocusedComponent[id].onScroll(id, pos==null?null:new Vector2d(pos.x-cursorFocusedComponent[id].x, pos.y-cursorFocusedComponent[id].y), dx, dy);
        return false;
    }
    public void onDrop(int id, Vector2d pos, int count, long names){
        if(cursorFocusedComponent[id]!=null)cursorFocusedComponent[id].onDrop(id, new Vector2d(pos.x-cursorFocusedComponent[id].x, pos.y-cursorFocusedComponent[id].y), count, names);
    }
    public void onJoystick(int id, int jid, int event){
        if(focusedComponent[id]!=null)focusedComponent[id].onJoystick(id, jid, event);
    }
    public void onResize(Vector2f size){
    }
    public void onCursorFocusGained(int id){
    }
    public void onCursorFocusLost(int id){
        if(cursorFocusedComponent[id]!=null){
            cursorFocusedComponent[id].isCursorFocused[id] = false;
            cursorFocusedComponent[id].onCursorFocusLost(id);
            cursorFocusedComponent[id] = null;
        }
    }
    public void onFocusGained(int id){
    }
    public void onFocusLost(int id){
        if(focusedComponent[id]!=null){
            focusedComponent[id].isFocused[id] = false;
            focusedComponent[id].onFocusLost(id);
            focusedComponent[id] = null;
        }
    }
    public Vector2f getSize(){
        return new Vector2f(size);
    }
    public float getWidth(){
        return size.x;
    }
    public float getHeight(){
        return size.y;
    }
    public void setSize(Vector2f size){
        setSize(size.x, size.y);
    }
    public void setSize(float width, float height){
        size.set(width, height);
        onResize(size);
    }
    public void setWidth(float width){
        size.y = width;
        onResize(size);
    }
    public void setHeight(float height){
        size.y = height;
        onResize(size);
    }
    public Vector2f getPreferredSize(){
        return size;
    }
    public float getPreferredWidth(){
        return getPreferredSize().x;
    }
    public float getPreferredHeight(){
        return getPreferredSize().y;
    }
    public boolean isCursorFocused(){
        for(boolean b : isCursorFocused)if(b)return true;
        return false;
    }
    public boolean isFocused(){
        for(boolean b : isFocused)if(b)return true;
        return false;
    }
    protected static UILayer getUIContext(){
        return DizzyEngine.getUIContext();
    }
}
