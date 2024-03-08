package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentHandle;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLabel;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
import java.util.ArrayList;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
public class Slider extends Component{
    public ComponentLayer background = getUIContext().getDefaultComponentBackground(Slider.class);
    public ComponentLabel label = getUIContext().getDefaultComponentLabel(Slider.class);
    public ComponentHandle handle = getUIContext().getDefaultComponentHandle(Slider.class);
    private final double min;
    private final double max;
    private double value;
    public boolean enabled = true;
    public boolean pressed;
    private final ArrayList<Runnable> actions = new ArrayList<>();
    public Slider(double min, double max, double value){
        this(min, max, value, null);
    }
    public Slider(double min, double max, double value, String text){
        this.min = min;
        this.max = max;
        this.value = value;
        handle.setValue(value);
        label.setLabel(text);
    }
    @Override
    public void draw(double deltaTime){
        if(background!=null)background.draw(this, deltaTime);
        if(label!=null)label.draw(this, deltaTime);
        if(handle!=null)handle.draw(this, deltaTime);
    }
    @Override
    public void onMouseButton(int id, Vector2d pos, int button, int action, int mods){
        super.onMouseButton(id, pos, button, action, mods);
        if(!enabled)return;
        if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT){
            if(action==GLFW.GLFW_PRESS&&!pressed){
                pressed = true;
            }
            if(action==GLFW.GLFW_RELEASE&&pressed){
                pressed = false;
            }
        }
    }
    @Override
    public void onCursorPos(int id, double xpos, double ypos){
        super.onCursorPos(id, xpos, ypos);
        if(pressed&&isCursorFocused[id]){
            double normalizedValue = Math.max(0, Math.min(1, xpos/getWidth()));//TODO insets
            var newValue = Math.max(min, Math.min(max, normalizedValue*(max-min)+min));
            if(newValue!=value){
                value = newValue;
                handle.setValue(normalizedValue);
                runActions();
            }
        }
    }
    public double getValue(){
        return value;
    }
    public float getValueF(){
        return (float)value;
    }
    public void runActions(){
        for(Runnable r : actions)r.run();
    }
    public Slider addAction(Runnable action){
        actions.add(action);
        return this;
    }
    public Slider addPriorityAction(Runnable action){
        actions.add(0, action);
        return this;
    }
}