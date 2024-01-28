package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import org.joml.Vector2f;
public class ColorBackgroundLayer extends ComponentLayer{
    public Color color;
    public ColorBackgroundLayer(Color color){
        this.color = color;
    }
    @Override
    public void draw(float x, float y, Vector2f size, double deltaTime){
        Renderer.setColor(color);
        Renderer.fillRect(x, y, x+size.x, y+size.y);
    }

}
