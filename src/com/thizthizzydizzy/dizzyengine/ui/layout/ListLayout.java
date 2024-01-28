package com.thizthizzydizzy.dizzyengine.ui.layout;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import org.joml.Vector2f;
public class ListLayout extends Layout{
    @Override
    public void arrangeComponents(Component container){
        float Y = 0;
        for(int i = 0; i<container.components.size(); i++){
            var component = container.components.get(i);
            component.x = 0;
            component.y = Y;
            component.setSize(new Vector2f(container.getWidth(), component.getPreferredHeight()));
            Y+=component.getHeight();
        }
    }
    @Override
    public Vector2f getPreferredSize(Component container){
        float width = 0, height = 0;
        for(int i = 0; i<container.components.size(); i++){
            var component = container.components.get(i);
            width = Math.max(width, component.getPreferredWidth());
            height+=component.getPreferredHeight();
        }
        return new Vector2f(width, height);
    }
}
