package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import org.joml.Vector2f;
public class TextLabelLayer extends ComponentLabel{
    public String text;
    public Color color;
    public TextLabelLayer(String text, Color color){
        this.text = text;
        this.color = color;
    }
    @Override
    public void setLabel(Object label){
        if(label==null)text = null;
        else if(label instanceof String str)this.text = str;
        else throw new IllegalArgumentException("Invalid label object! Expected String, found "+label.getClass().getName());
    }
    @Override
    public void draw(float x, float y, Vector2f size, double deltaTime){
        if(text==null)return;
        Renderer.setColor(color);
        float length = Renderer.getStringWidth(text, size.y-labelInset*2);
        if(length<0)return;
        float scale = Math.min(1, (size.x-labelInset*2)/length);
        float textHeight = (size.y-labelInset*2)*scale;
        Renderer.drawCenteredText(x, y+size.y/2-textHeight/2, x+size.x, y+size.y/2+textHeight/2, text);
    }
}