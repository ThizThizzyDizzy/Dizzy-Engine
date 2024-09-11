package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLabel;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
public class Label extends Component{
    public ComponentLayer background = getUIContext().getDefaultComponentBackground(Label.class);
    public ComponentLabel label = getUIContext().getDefaultComponentLabel(Label.class);
    public Label(){
    }
    public Label(String text){
        label.setLabel(text);
    }
    @Override
    public void draw(double deltaTime){
        if(background!=null)background.draw(this, deltaTime);
        if(label!=null)label.draw(this, deltaTime);
    }
}
