package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.ui.layout.Layout;
import org.joml.Vector2d;
import org.joml.Vector2f;
public class Scrollable extends Component{
    public Panel content;
    public ScrollBar vertScrollBar;
    public ScrollBar horizScrollBar;
    public float scrollMagnitude = getUIContext().getUnitScale()*50;
    public float vertScrollbarSize;
    public float horizScrollbarSize;
    public boolean allowVerticalScrolling = true;
    public boolean allowHorizontalScrolling = true;
    public Scrollable(){
        this(getUIContext().getUnitScale()*25);
    }
    public Scrollable(float horizScrollbarSize){
        this(getUIContext().getUnitScale()*25, horizScrollbarSize);
    }
    public Scrollable(float vertScrollbarSize, float horizScrollbarSize){
        content = super.add(new Panel());
        this.vertScrollbarSize = vertScrollbarSize;
        this.horizScrollbarSize = horizScrollbarSize;
    }
    public void setLayout(Layout layout){
        content.setLayout(layout);
    }
    @Override
    public <T extends Component> T add(T component){
        return content.add(component);
    }
    @Override
    public void render(double deltaTime){
        Renderer.bound(x, y, x+getWidth(), y+getHeight());
        Renderer.bound(x, y, x+getWidth()-getVertScrollbarSize(), y+getHeight()-getHorizScrollbarSize());
        draw(deltaTime);
        Renderer.translate(x, y);
        for(var c : components){
            c.render(deltaTime);
            if(c==content)Renderer.unBound();
        }
        Renderer.unTranslate();
        Renderer.unBound();
    }
    @Override
    public void onResize(Vector2f size){
        var preferred = content.getPreferredSize();
        if(!allowHorizontalScrolling)preferred.x = 0;
        if(!allowVerticalScrolling)preferred.y = 0;
        if(vertScrollBar==null&&size.y-getHorizScrollbarSize()<preferred.y&&allowVerticalScrolling){
            vertScrollBar = super.add(new ScrollBar(false));
            vertScrollBar.setWidth(vertScrollbarSize);
        }
        if(horizScrollBar==null&&size.x-getVertScrollbarSize()<preferred.x&&allowHorizontalScrolling){
            horizScrollBar = super.add(new ScrollBar(true));
            horizScrollBar.setHeight(horizScrollbarSize);
        }
        if(vertScrollBar!=null&&(size.y-getHorizScrollbarSize()>=preferred.y||!allowVerticalScrolling)){
            components.remove(vertScrollBar);
            vertScrollBar = null;
        }
        if(horizScrollBar!=null&&(size.x-getVertScrollbarSize()>=preferred.x||!allowHorizontalScrolling)){
            components.remove(horizScrollBar);
            horizScrollBar = null;
        }
        content.setSize(Math.max(preferred.x, size.x-getVertScrollbarSize()), Math.max(preferred.y, size.y-getHorizScrollbarSize()));
        if(vertScrollBar!=null){
            vertScrollBar.setSize(vertScrollbarSize, size.y-getHorizScrollbarSize());
            vertScrollBar.x = size.x-getVertScrollbarSize();
            vertScrollBar.y = 0;
            vertScrollBar.updateScrollbar(size.y-getHorizScrollbarSize(), content.getHeight());
        }
        if(horizScrollBar!=null){
            horizScrollBar.setSize(size.x-getVertScrollbarSize(), horizScrollbarSize);
            horizScrollBar.x = 0;
            horizScrollBar.y = size.y-getHorizScrollbarSize();
            horizScrollBar.updateScrollbar(size.x-getVertScrollbarSize(), content.getWidth());
        }
        updateScroll();
    }
    private void updateScroll(){
        content.x = horizScrollBar==null?0:-horizScrollBar.getPosition();
        content.y = vertScrollBar==null?0:-vertScrollBar.getPosition();
    }
    private float getVertScrollbarSize(){
        if(vertScrollBar==null)return 0;
        return vertScrollBar.getWidth();
    }
    private float getHorizScrollbarSize(){
        if(horizScrollBar==null)return 0;
        return horizScrollBar.getHeight();
    }
    @Override
    public boolean onScroll(int id, Vector2d pos, double dx, double dy){
        if(cursorFocusedComponent[id]==content&&super.onScroll(id, pos, dx, dy))return true;
        if((vertScrollBar!=null&&vertScrollBar.onScroll(id, pos, dx, dy))
            ||(horizScrollBar!=null&&horizScrollBar.onScroll(id, pos, dx, dy))){
            updateScroll();
            return true;
        }
        return false;
    }
}
