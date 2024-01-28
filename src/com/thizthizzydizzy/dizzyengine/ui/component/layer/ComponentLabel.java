package com.thizthizzydizzy.dizzyengine.ui.component.layer;
public abstract class ComponentLabel extends ComponentLayer{
    public float labelInset = getUIContext().getUnitScale()*5;
    public abstract void setLabel(Object label);
}
