package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import org.joml.Vector2f;
public abstract class RectangleLinearHandle extends LinearHandle{
    private double value;
    private double size = .1;
    private boolean horizontal = false;
    //TODO insets
    @Override
    public void setValue(double value){
        this.value = value;
    }
    @Override
    public void setSize(double size){
        this.size = size;
    }
    @Override
    public void setOrientation(boolean horizontal){
        this.horizontal = horizontal;
    }
    @Override
    public void draw(Component c, double deltaTime){
        if(horizontal){
            float width = (float)(c.getWidth()*size);
            float xOff = (float)(value*(c.getWidth()-width));
            drawHandle(c, new Vector2f(xOff, 0), new Vector2f(width, c.getHeight()), deltaTime);
        }else{
            float height = (float)(c.getHeight()*size);
            float yOff = (float)(value*(c.getHeight()-height));
            drawHandle(c, new Vector2f(0, yOff), new Vector2f(c.getWidth(), height), deltaTime);
        }
    }
    public abstract void drawHandle(Component c, Vector2f offset, Vector2f size, double deltaTime);
}
