package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLabel;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
import java.util.ArrayList;
import java.util.function.Predicate;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;
public class TextBox extends Component{
    public ComponentLayer background = getUIContext().getDefaultComponentBackground(TextBox.class);
    public ComponentLabel label = getUIContext().getDefaultComponentLabel(TextBox.class);
    private final ArrayList<Runnable> actions = new ArrayList<>();
    private String text;
    public boolean editable = true;
    public boolean runActionsOnTextChange = false;
    public Predicate<String> filter;
    public TextBox(){
        this("");
    }
    public TextBox(String text){
        this.text = text;
        label.setLabel(text);
    }
    @Override
    public void draw(double deltaTime){
        if(background!=null)background.draw(this, deltaTime);
        if(label!=null)label.draw(this, deltaTime);
    }
    @Override
    public void onChar(int id, int codepoint){
        char c = (char)codepoint;
        if(editable)setText(text+c);//TODO allow cursor moving
    }
    @Override
    public void onKey(int id, int key, int scancode, int action, int mods){
        if(!editable)return;
        if(action==GLFW_PRESS||action==GLFW_REPEAT){
            if(key==GLFW_KEY_BACKSPACE&&!text.isEmpty())setText(text.substring(0, text.length()-1));//TODO allow cursor moving
            if(key==GLFW_KEY_ENTER&&!runActionsOnTextChange)runActions();
        }
    }
    public void runActions(){
        for(Runnable r : actions)r.run();
    }
    public TextBox addAction(Runnable action){
        actions.add(action);
        return this;
    }
    public TextBox addPriorityAction(Runnable action){
        actions.add(0, action);
        return this;
    }
    @Override
    public Vector2f getPreferredSize(){
        return label.getPreferredSize();
    }
    public String getText(){
        return text;
    }
    public void setText(String text){
        if(filter!=null&&!filter.test(text))return;
        this.text = text;
        label.setLabel(text);
        if(runActionsOnTextChange)runActions();
    }
}
