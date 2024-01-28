package com.thizthizzydizzy.dizzyengine.ui.layout;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import org.joml.Vector2f;
public abstract class Layout{
    public abstract void arrangeComponents(Component container);
    public abstract Vector2f getPreferredSize(Component container);
}
