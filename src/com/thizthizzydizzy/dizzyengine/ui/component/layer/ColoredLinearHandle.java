package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import org.joml.Vector2f;
public class ColoredLinearHandle extends RectangleLinearHandle{
    private final Color color;
    public ColoredLinearHandle(Color color){
        this.color = color;
    }
    @Override
    public void drawHandle(Component c, Vector2f offset, Vector2f size, double deltaTime){
        Renderer.setColor(color);
        Renderer.fillRect(c.x+offset.x, c.y+offset.y, c.x+offset.x+size.x, c.y+offset.y+size.y, 0);
    }
}
