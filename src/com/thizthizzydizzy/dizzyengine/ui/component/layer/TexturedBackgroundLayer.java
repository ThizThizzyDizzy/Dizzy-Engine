package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import org.joml.Vector2f;
public class TexturedBackgroundLayer extends ComponentLayer{
    private final int texture;
    public TexturedBackgroundLayer(int texture){//TODO non-stretch sizing, including dynamic tiling for buttons
        this.texture = texture;
    }
    @Override
    public void draw(float x, float y, Vector2f size, double deltaTime){
        Renderer.setColor(Color.WHITE);
        Renderer.fillRect(x, y, x+size.x, y+size.y, texture);
    }

}
