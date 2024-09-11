package com.thizthizzydizzy.dizzyengine.ui.builder;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import java.util.function.Consumer;
public abstract class ComponentBuilder<T extends Component>{
    private final PanelBuilder ui;
    T component;
    public ComponentBuilder(PanelBuilder ui, T base){
        this.ui = ui;
        component = base;
        if(ui!=null)ui.component.add(component);
    }
    public PanelBuilder build(){
        return ui;
    }
    public ComponentBuilder<T> saveReference(Consumer<T> reference){
        reference.accept(component);
        return this;
    }
}
