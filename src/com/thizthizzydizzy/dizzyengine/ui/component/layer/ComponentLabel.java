package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import org.joml.Vector2f;
public abstract class ComponentLabel extends ComponentLayer{
    public float labelInset = getUIContext().getUnitScale()*5;
    public abstract void setLabel(Object label);
    public abstract Vector2f getPreferredSize();
}
