package com.thizthizzydizzy.dizzyengine.ui.builder;
import com.thizthizzydizzy.dizzyengine.ui.layout.BorderLayout;
import java.util.function.Function;
public class BorderLayoutBuilder{
    private final PanelBuilder ui;
    private final BorderLayout layout;
    public BorderLayoutBuilder(PanelBuilder ui){
        layout = ui.component.setLayout(new BorderLayout());
        this.ui = ui;
    }
    @Deprecated
    public BorderLayoutBuilder top(Function<PanelBuilder, ComponentBuilder> comp){
        layout.add(comp.apply(ui).component, BorderLayout.Direction.TOP);
        return this;
    }
    @Deprecated
    public BorderLayoutBuilder bottom(Function<PanelBuilder, ComponentBuilder> comp){
        layout.add(comp.apply(ui).component, BorderLayout.Direction.BOTTOM);
        return this;
    }
    @Deprecated
    public BorderLayoutBuilder left(Function<PanelBuilder, ComponentBuilder> comp){
        layout.add(comp.apply(ui).component, BorderLayout.Direction.LEFT);
        return this;
    }
    @Deprecated
    public BorderLayoutBuilder right(Function<PanelBuilder, ComponentBuilder> comp){
        layout.add(comp.apply(ui).component, BorderLayout.Direction.RIGHT);
        return this;
    }
    @Deprecated
    public BorderLayoutBuilder center(Function<PanelBuilder, ComponentBuilder> comp){
        layout.add(comp.apply(ui).component, BorderLayout.Direction.CENTER);
        return this;
    }
    public BorderLayoutBuilder top(ComponentBuilder comp){
        layout.add(comp.component, BorderLayout.Direction.TOP);
        return this;
    }
    public BorderLayoutBuilder bottom(ComponentBuilder comp){
        layout.add(comp.component, BorderLayout.Direction.BOTTOM);
        return this;
    }
    public BorderLayoutBuilder left(ComponentBuilder comp){
        layout.add(comp.component, BorderLayout.Direction.LEFT);
        return this;
    }
    public BorderLayoutBuilder right(ComponentBuilder comp){
        layout.add(comp.component, BorderLayout.Direction.RIGHT);
        return this;
    }
    public BorderLayoutBuilder center(ComponentBuilder comp){
        layout.add(comp.component, BorderLayout.Direction.CENTER);
        return this;
    }
    public PanelBuilder build(){
        return ui;
    }
}
