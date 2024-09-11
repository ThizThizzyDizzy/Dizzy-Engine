package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLabel;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
import java.util.ArrayList;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
public class Button extends Component{
    public static boolean ACT_ON_PRESS = false;//TODO should this be moved to the ui context?
    public boolean enabled = true;
    public ComponentLayer background = getUIContext().getDefaultComponentBackground(Button.class);
    public ComponentLabel label = getUIContext().getDefaultComponentLabel(Button.class);
    public boolean pressed;
    private final ArrayList<Runnable> actions = new ArrayList<>();
    public Button(){
    }
    public Button(String text){
        this(text, true);
    }
    public Button(String text, boolean enabled){
        label.setLabel(text);
        this.enabled = enabled;
    }
    @Override
    public void draw(double deltaTime){
//        if(background!=null)background.draw(this, deltaTime);
        if(label!=null)label.draw(this, deltaTime);
    }
    @Override
    public void onMouseButton(int id, Vector2d pos, int button, int action, int mods){
        super.onMouseButton(id, pos, button, action, mods);
        if(!enabled)return;
        if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT){
            if(action==GLFW.GLFW_PRESS&&!pressed){
                pressed = true;
                if(ACT_ON_PRESS)runActions();
            }
            if(action==GLFW.GLFW_RELEASE&&pressed){
                pressed = false;
                if(isCursorFocused[id]&&!ACT_ON_PRESS)runActions();
            }
        }
    }
    public void runActions(){
        for(Runnable r : actions)r.run();
    }
    public Button addAction(Runnable action){
        actions.add(action);
        return this;
    }
    public Button addPriorityAction(Runnable action){
        actions.add(0, action);
        return this;
    }
    @Override
    public Vector2f getPreferredSize(){
        return label.getPreferredSize();
    }
}
