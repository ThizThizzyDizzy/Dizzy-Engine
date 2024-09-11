package com.thizthizzydizzy.dizzyengine.ui.builder;
import com.thizthizzydizzy.dizzyengine.ui.component.TextBox;
public class TextBoxBuilder extends ComponentBuilder<TextBox>{
    public TextBoxBuilder(PanelBuilder ui){
        super(ui, new TextBox());
    }
    public TextBoxBuilder text(String text){
        component.setText(text);
        return this;
    }
}
