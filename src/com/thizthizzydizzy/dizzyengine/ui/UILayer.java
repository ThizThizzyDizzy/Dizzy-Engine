package com.thizthizzydizzy.dizzyengine.ui;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLabel;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
import java.util.HashMap;
import java.util.function.Supplier;
import org.joml.Vector2f;
public abstract class UILayer extends DizzyLayer{
    public Menu menu;
    public final Vector2f size = new Vector2f();
    private HashMap<Class<? extends Component>, Supplier<ComponentLayer>> defaultComponentBackgrounds = new HashMap<>();
    private HashMap<Class<? extends Component>, Supplier<ComponentLabel>> defaultComponentLabels = new HashMap<>();
    public <T extends Menu> T open(T menu){
        if(this.menu!=null)this.menu.onMenuClosed();
        this.menu = menu;
        if(menu!=null){
            menu.setSize(size);
            menu.onMenuOpened();
        }
        return menu;
    }
    /**
     * Get the default unit scale for this UI context. This is the reference
     * scale used for default settings such as text insets, scroll distance, or
     * scrollbar size.
     *
     * @return The UI Context unit scale
     */
    public abstract float getUnitScale();
    public ComponentLayer getDefaultComponentBackground(){
        return getDefaultComponentBackground(null);
    }
    public ComponentLabel getDefaultComponentLabel(){
        return getDefaultComponentLabel(null);
    }
    public ComponentLayer getDefaultComponentBackground(Class<? extends Component> clazz){
        var supplier = defaultComponentBackgrounds.get(clazz);
        if(supplier==null)supplier = defaultComponentBackgrounds.get(null);
        if(supplier==null)return null;
        return supplier.get();
    }
    public ComponentLabel getDefaultComponentLabel(Class<? extends Component> clazz){
        var supplier = defaultComponentLabels.get(clazz);
        if(supplier==null)supplier = defaultComponentLabels.get(null);
        if(supplier==null)return null;
        return supplier.get();
    }
    public void setDefaultComponentBackground(Supplier<ComponentLayer> layer){
        setDefaultComponentBackground(null, layer);
    }
    public void setDefaultComponentLabel(Supplier<ComponentLabel> layer){
        setDefaultComponentLabel(null, layer);
    }
    public void setDefaultComponentBackground(Class<? extends Component> clazz, Supplier<ComponentLayer> layer){
        defaultComponentBackgrounds.put(clazz, layer);
    }
    public void setDefaultComponentLabel(Class<? extends Component> clazz, Supplier<ComponentLabel> layer){
        defaultComponentLabels.put(clazz, layer);
    }
}
