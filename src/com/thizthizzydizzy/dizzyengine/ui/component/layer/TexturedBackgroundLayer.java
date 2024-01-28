package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
public class TexturedBackgroundLayer extends ComponentLayer{
    private final int texture;
    public TexturedBackgroundLayer(int texture){//TODO non-stretch sizing, including dynamic tiling for buttons
        this.texture = texture;
    }
    @Override
    public void draw(Component c, double deltaTime){
        Renderer.setColor(Color.WHITE);
        Renderer.fillRect(c.x, c.y, c.x+c.getWidth(), c.y+c.getHeight(), texture);
    }
}
