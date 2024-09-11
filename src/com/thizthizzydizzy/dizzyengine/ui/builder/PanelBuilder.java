package com.thizthizzydizzy.dizzyengine.ui.builder;
import com.thizthizzydizzy.dizzyengine.ui.Menu;
import com.thizthizzydizzy.dizzyengine.ui.component.Panel;
import com.thizthizzydizzy.dizzyengine.ui.layout.GridLayout;
import java.util.function.Consumer;
public class PanelBuilder extends ComponentBuilder<Panel>{
    public PanelBuilder(Menu panel){
        super(null, panel);
    }
    public PanelBuilder(PanelBuilder ui){
        super(ui, new Panel());
    }
    @Deprecated
    private PanelBuilder(Panel panel){
        super(null, panel);
    }
    @Deprecated
    public PanelBuilder ui(Consumer<PanelBuilder> func){
        func.accept(new PanelBuilder(component));
        return this;
    }
    public BorderLayoutBuilder borderLayout(){
        return new BorderLayoutBuilder(this);
    }
    public PanelBuilder gridLayout(int cols, int rows){
        component.setLayout(new GridLayout(cols, rows));
        return this;
    }
    public ButtonBuilder button(){
        return new ButtonBuilder(this);
    }
    public LabelBuilder label(){
        return new LabelBuilder(this);
    }
    public PanelBuilder panel(){
        return new PanelBuilder(this);
    }
    public TextBoxBuilder textBox(){
        return new TextBoxBuilder(this);
    }
    @Override
    public PanelBuilder saveReference(Consumer<Panel> reference){
        return (PanelBuilder)super.saveReference(reference);
    }
}
