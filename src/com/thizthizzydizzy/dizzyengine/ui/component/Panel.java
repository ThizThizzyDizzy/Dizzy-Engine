package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
import com.thizthizzydizzy.dizzyengine.ui.layout.Layout;
import org.joml.Vector2f;
public class Panel extends Component{
    public ComponentLayer background = getUIContext().getDefaultComponentBackground();
    public Layout layout;
    @Override
    public void draw(double deltaTime){
        if(background!=null)background.draw(this, deltaTime);
    }
    public <T extends Layout> T setLayout(T layout){
        this.layout = layout;
        layout.arrangeComponents(this);
        return layout;
    }
    @Override
    public Vector2f getPreferredSize(){
        if(layout!=null)return layout.getPreferredSize(this);
        return super.getPreferredSize();
    }
    @Override
    public void onResize(Vector2f size){
        if(layout!=null)layout.arrangeComponents(this);
    }
    @Override
    public <T extends Component> T add(T component){
        var ret = super.add(component);
        if(layout!=null)layout.arrangeComponents(this);
        return ret;
    }
    public void rearrange(){
        if(layout!=null)layout.arrangeComponents(this);
    }
}
