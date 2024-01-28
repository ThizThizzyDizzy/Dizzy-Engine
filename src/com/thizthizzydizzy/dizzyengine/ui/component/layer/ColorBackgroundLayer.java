package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
public class ColorBackgroundLayer extends ComponentLayer{
    public Color color;
    public ColorBackgroundLayer(Color color){
        this.color = color;
    }
    @Override
    public void draw(Component c, double deltaTime){
        Renderer.setColor(color);
        Renderer.fillRect(c.x, c.y, c.x+c.getWidth(), c.y+c.getHeight());
    }
}
