package com.thizthizzydizzy.dizzyengine.ui.builder;
import com.thizthizzydizzy.dizzyengine.ui.component.Label;
public class LabelBuilder extends ComponentBuilder<Label>{
    public LabelBuilder(PanelBuilder ui){
        super(ui, new Label());
    }
    public LabelBuilder label(String text){
        component.label.setLabel(text);
        return this;
    }
}
