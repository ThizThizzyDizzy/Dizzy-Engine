package com.thizthizzydizzy.dizzyengine.ui.component.layer;
public abstract class LinearHandle extends ComponentHandle{
    @Override
    public void setValue(Object value){
        setValue(((Number)value).doubleValue());
    }
    public abstract void setValue(double value);
    @Override
    public void setSize(Object size){
        setSize(((Number)size).doubleValue());
    }
    public abstract void setSize(double size);
    @Override
    public void setOrientation(Object orientation){
        setOrientation((boolean)orientation);
    }
    public abstract void setOrientation(boolean horizontal);
}
