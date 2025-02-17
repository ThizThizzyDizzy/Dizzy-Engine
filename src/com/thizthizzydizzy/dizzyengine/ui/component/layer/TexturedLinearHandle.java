package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import org.joml.Vector2f;
public class TexturedLinearHandle extends RectangleLinearHandle{
    private final int texture;
    public TexturedLinearHandle(int texture){
        this.texture = texture;
    }
    @Override
    public void drawHandle(Component c, Vector2f offset, Vector2f size, double deltaTime){
        Renderer.setColor(Color.WHITE);
        Renderer.fillRect(c.x+offset.x, c.y+offset.y, c.x+offset.x+size.x, c.y+offset.x+size.y, texture);
    }
}
