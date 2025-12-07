package com.thizthizzydizzy.dizzyengine;
import org.joml.Vector2i;
public abstract class DizzyLayer{
    public static final int Z_INDEX_OFFSET_UI = 10_000;
    public static final int Z_INDEX_OFFSET_DEBUG = 2_000_000_000;
    public int zIndex = 0;
    public abstract void init();
    public abstract void render(double deltaTime);
    public abstract void cleanup();
    protected void onChar(int id, int codepoint){
    }
    protected void onCharMods(int id, int codepoint, int mods){
    }
    protected void onCursorEnter(int id, boolean entered){
    }
    protected void onCursorPos(int id, double xpos, double ypos){
    }
    protected void onKey(int id, int key, int scancode, int action, int mods){
    }
    protected void onMouseButton(int id, int button, int action, int mods){
    }
    protected void onScroll(int id, double xoffset, double yoffset){
    }
    protected void onDrop(int id, int count, long names){
    }
    protected void onJoystick(int id, int jid, int event){
    }
    protected void onScreenSize(Vector2i screenSize){
    }
}
