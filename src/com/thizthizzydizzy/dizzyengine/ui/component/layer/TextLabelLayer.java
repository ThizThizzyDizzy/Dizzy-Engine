package com.thizthizzydizzy.dizzyengine.ui.component.layer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
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
    public void draw(Component c, double deltaTime){
        if(text==null)return;
        Renderer.setColor(color);
        float length = Renderer.getStringWidth(text, c.getHeight()-labelInset*2);
        if(length<0)return;
        float scale = Math.min(1, (c.getWidth()-labelInset*2)/length);
        float textHeight = (c.getHeight()-labelInset*2)*scale;
        Renderer.drawCenteredText(c.x, c.y+c.getHeight()/2-textHeight/2, c.x+c.getWidth(), c.y+c.getHeight()/2+textHeight/2, text);
    }
}
