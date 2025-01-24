package com.thizthizzydizzy.dizzyengine.ui.layout;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import java.util.HashMap;
import org.joml.Vector2f;
public class BorderLayout extends Layout{
    public HashMap<Direction, Component> anchors = new HashMap<>();
    public <T extends Component> T add(T component, Direction anchor){
        anchors.put(anchor, component);
        return component;
    }
    @Override
    public void arrangeComponents(Component container){
        var top = anchors.get(Direction.TOP);
        float topSize = top==null?0:top.getPreferredHeight();
        if(top!=null){
            top.x = 0;
            top.y = 0;
            top.setSize(container.getWidth(), topSize);
        }
        var bottom = anchors.get(Direction.BOTTOM);
        float bottomSize = bottom==null?0:bottom.getPreferredHeight();
        if(bottom!=null){
            bottom.x = 0;
            bottom.y = container.getHeight()-bottomSize;
            bottom.setSize(container.getWidth(), bottomSize);
        }
        float innerHeight = container.getHeight()-topSize-bottomSize;
        var left = anchors.get(Direction.LEFT);
        float leftSize = left==null?0:left.getPreferredWidth();
        if(left!=null){
            left.x = 0;
            left.y = topSize;
            left.setSize(leftSize, innerHeight);
        }
        var right = anchors.get(Direction.RIGHT);
        float rightSize = right==null?0:right.getPreferredWidth();
        if(right!=null){
            right.x = container.getWidth()-rightSize;
            right.y = topSize;
            right.setSize(rightSize, innerHeight);
        }
        float innerWidth = container.getWidth()-leftSize-rightSize;
        var center = anchors.get(Direction.CENTER);
        if(center!=null){
            center.x = leftSize;
            center.y = topSize;
            center.setSize(innerWidth, innerHeight);
        }
    }
    @Override
    public Vector2f getPreferredSize(Component container){
        var top = anchors.get(Direction.TOP);
        Vector2f topSize = top==null?new Vector2f():top.getPreferredSize();
        var bottom = anchors.get(Direction.BOTTOM);
        Vector2f bottomSize = bottom==null?new Vector2f():bottom.getPreferredSize();
        var left = anchors.get(Direction.LEFT);
        Vector2f leftSize = left==null?new Vector2f():left.getPreferredSize();
        var right = anchors.get(Direction.RIGHT);
        Vector2f rightSize = right==null?new Vector2f():right.getPreferredSize();
        var center = anchors.get(Direction.CENTER);
        Vector2f centerSize = center==null?new Vector2f():center.getPreferredSize();
        return new Vector2f(
            Math.max(Math.max(topSize.x, bottomSize.x), leftSize.x+centerSize.x+rightSize.x),
            topSize.y+Math.max(Math.max(leftSize.y, rightSize.y), centerSize.y)+bottomSize.y
        );
    }
    public static enum Direction{
        TOP, BOTTOM, LEFT, RIGHT, CENTER;
    }
}
