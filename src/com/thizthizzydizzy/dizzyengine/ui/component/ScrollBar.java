package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.MathUtil;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentHandle;
import com.thizthizzydizzy.dizzyengine.ui.component.layer.ComponentLayer;
import org.joml.Vector2d;
public class ScrollBar extends Component{
    public ComponentLayer background = getUIContext().getDefaultComponentBackground(ScrollBar.class);
    public ComponentHandle handle = getUIContext().getDefaultComponentHandle(ScrollBar.class);
    private final boolean horizontal;
    private float innerSize;
    private float fullSize;
    private float value;
    public ScrollBar(boolean horizontal){
        this.horizontal = horizontal;
        if(handle!=null)handle.setOrientation(horizontal);
    }
    @Override
    public void draw(double deltaTime){
        if(background!=null)background.draw(this, deltaTime);
        if(handle!=null)handle.draw(this, deltaTime);
        super.draw(deltaTime);
    }
    public void updateScrollbar(float innerSize, float fullSize){
        var position = getPosition();
        this.innerSize = innerSize;
        this.fullSize = fullSize;
        value = MathUtil.clamp(position/getScrollSize());
        if(handle!=null)handle.setValue(value);
        if(handle!=null)handle.setSize(innerSize/fullSize);
    }
    public void scroll(float position){
        value = position/getScrollSize();
        if(handle!=null)handle.setValue(value);
    }
    public float getPosition(){
        return value*getScrollSize();
    }
    public float getValue(){
        return value;
    }
    @Override
    public boolean onScroll(int id, Vector2d pos, double dx, double dy){
        if(super.onScroll(id, pos, dx, dy))return true;
        var position = getPosition();
        double change = -(horizontal?dx:dy);
        if(change<0&&position>0){
            scroll((float)Math.max(0, position+change*Renderer.getPreferredTextHeight()));
            return true;
        }
        if(change>0&&position<(getScrollSize())){
            scroll((float)Math.min(getScrollSize(), position+change*Renderer.getPreferredTextHeight()));
            return true;
        }
        return false;
    }
    /**
     * @return the size of the scrollable portion (fullSize - innerSize)
     */
    private float getScrollSize(){
        return fullSize-innerSize;
    }
}
