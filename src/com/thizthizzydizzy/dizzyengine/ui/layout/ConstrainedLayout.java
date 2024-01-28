package com.thizthizzydizzy.dizzyengine.ui.layout;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import com.thizthizzydizzy.dizzyengine.ui.layout.constraint.Constraint;
import java.util.ArrayList;
import java.util.HashMap;
import org.joml.Vector2f;
import org.joml.Vector4f;
public class ConstrainedLayout extends Layout{
    public HashMap<Component, ArrayList<Constraint>> constraints = new HashMap<>();
    public void constrain(Component component, Constraint constraint){
        var list = constraints.getOrDefault(component, new ArrayList<>());
        list.add(constraint);
        if(!constraints.containsKey(component))constraints.put(component, list);
    }
    @Override
    public void arrangeComponents(Component container){
        for(int i = 0; i<container.components.size(); i++){
            var component = container.components.get(i);
            Vector4f dimensions = new Vector4f(component.x, component.y, component.getWidth(), component.getHeight());
            for(var constraint : constraints.getOrDefault(component, new ArrayList<>())){
                constraint.apply(container.getSize(), dimensions);
            }
            component.x = dimensions.x;
            component.y = dimensions.y;
            component.setSize(dimensions.z, dimensions.w);
        }
    }
    @Override
    public Vector2f getPreferredSize(Component container){
        return container.getPreferredSize();
    }
}
