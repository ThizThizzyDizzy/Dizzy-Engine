package com.thizthizzydizzy.dizzyengine.ui.component.layer;
public abstract class LinearHandle extends ComponentHandle{
    @Override
    public void setValue(Object value){
        setValue((double)value);
    }
    public abstract void setValue(double value);
}