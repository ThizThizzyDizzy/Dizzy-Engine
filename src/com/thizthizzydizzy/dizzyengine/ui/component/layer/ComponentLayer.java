package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ui.UILayer;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
public abstract class ComponentLayer{
    public abstract void draw(Component c, double deltaTime);
    protected static UILayer getUIContext(){
        return DizzyEngine.getUIContext();
    }
}
