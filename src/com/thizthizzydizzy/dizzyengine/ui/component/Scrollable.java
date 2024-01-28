package com.thizthizzydizzy.dizzyengine.ui.component;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.ui.layout.Layout;
import org.joml.Vector2f;
public class Scrollable extends Component{
    public Panel content;
    public ScrollBar vertScrollBar;
    public ScrollBar horizScrollBar;
    public float scrollMagnitude = getUIContext().getUnitScale()*50;
    private float xScroll;
    private float yScroll;
    public Scrollable(){
        this(getUIContext().getUnitScale()*25);
    }
    public Scrollable(float horizScrollbarSize){
        this(getUIContext().getUnitScale()*25, horizScrollbarSize);
    }
    public Scrollable(float vertScrollbarSize, float horizScrollbarSize){
        content = super.add(new Panel());
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
        content.setSize(Math.max(preferred.x, size.x-getVertScrollbarSize()), Math.max(preferred.y, size.y-getHorizScrollbarSize()));
        if(vertScrollBar!=null){
            vertScrollBar.x = getWidth()-getVertScrollbarSize();
            vertScrollBar.y = 0;
            vertScrollBar.setHeight(getHeight());
        }
        if(horizScrollBar!=null){
            horizScrollBar.x = 0;
            horizScrollBar.y = getHeight()-getHorizScrollbarSize();
            horizScrollBar.setWidth(getWidth());
        }
        updateScroll();
    }
    private void updateScroll(){
        float maxScrollX = getWidth()-getVertScrollbarSize()-content.getWidth();
        float maxScrollY = getHeight()-getHorizScrollbarSize()-content.getHeight();
        xScroll = Math.max(0, Math.min(maxScrollX, xScroll));
        yScroll = Math.max(0, Math.min(maxScrollY, yScroll));
        content.x = -xScroll;
        content.y = -yScroll;
    }
    private float getVertScrollbarSize(){
        if(vertScrollBar==null)return 0;
        return vertScrollBar.getWidth();
    }
    private float getHorizScrollbarSize(){
        if(horizScrollBar==null)return 0;
        return horizScrollBar.getHeight();
    }
}
