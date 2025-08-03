package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import java.util.Objects;
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
        this.text = Objects.toString(label);
    }
    @Override
    public void draw(Component c, double deltaTime){
        if(text==null)return;
        Renderer.setColor(color);
        float length = Renderer.getStringWidth(text, c.getHeight()-labelInset*2);
        if(length<0)return;
        float scale = Math.min(1, (c.getWidth()-labelInset*2)/length);
        float textHeight = (c.getHeight()-labelInset*2)*scale;
        Renderer.drawCenteredText(c.x, c.y+c.getHeight()/2-textHeight/2, c.x+c.getWidth(), c.y+c.getHeight()/2+textHeight/2, text);
    }
    @Override
    public Vector2f getPreferredSize(){
        var preferredSize = new Vector2f();
        if(text==null)return preferredSize;
        float height = Renderer.getPreferredTextHeight();
        float length = Renderer.getStringWidth(text, height);
        return preferredSize.set(length+labelInset*2, height+labelInset*2);
    }
}
