package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ui.UILayer;
import org.joml.Vector2f;
public abstract class ComponentLayer{
    public abstract void draw(float x, float y, Vector2f size, double deltaTime);
    protected static UILayer getUIContext(){
        return DizzyEngine.getUIContext();
    }
}
