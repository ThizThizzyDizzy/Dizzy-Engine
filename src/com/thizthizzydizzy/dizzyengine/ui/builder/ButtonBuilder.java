package com.thizthizzydizzy.dizzyengine.ui.builder;
import com.thizthizzydizzy.dizzyengine.ui.component.Button;
public class ButtonBuilder extends ComponentBuilder<Button>{
    public ButtonBuilder(PanelBuilder ui){
        super(ui, new Button());
    }
    public ButtonBuilder label(String text){
        component.label.setLabel(text);
        return this;
    }
    public ButtonBuilder action(Runnable r){
        component.addAction(r);
        return this;
    }
}
