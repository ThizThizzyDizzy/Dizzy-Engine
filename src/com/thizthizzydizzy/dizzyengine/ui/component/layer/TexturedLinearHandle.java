package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
public class TexturedLinearHandle extends LinearHandle{
    private final int texture;
    private double value;
    public TexturedLinearHandle(int texture){//TODO adjustable handle thickness, vertical support, insets (see slider)
        this.texture = texture;
    }
    @Override
    public void setValue(double value){
        this.value = value;
    }
    @Override
    public void draw(Component c, double deltaTime){
        Renderer.setColor(Color.WHITE);
        float width = c.getHeight()/4;
        float xOff = (float)(value*(c.getWidth()-width));
        Renderer.fillRect(c.x+xOff, c.y, c.x+xOff+width, c.y+c.getHeight(), texture);
    }
}