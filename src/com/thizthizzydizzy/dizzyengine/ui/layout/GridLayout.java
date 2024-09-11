package com.thizthizzydizzy.dizzyengine.ui.layout;
import com.thizthizzydizzy.dizzyengine.ui.component.Component;
import org.joml.Vector2f;
public class GridLayout extends Layout{
    private int cols;
    private int rows;
    public GridLayout(int cols, int rows){
        this.cols = cols;
        this.rows = rows;
    }
    @Override
    public void arrangeComponents(Component container){
        int X = 0;
        int Y = 0;
        int cols = this.cols;
        int rows = this.rows;
        if(cols==0)cols = container.components.size()/rows+(container.components.size()%rows>0?1:0);
        if(rows==0)rows = container.components.size()/cols+(container.components.size()%cols>0?1:0);
        float width = container.getWidth()/cols;
        float height = container.getHeight()/rows;
        for(int i = 0; i<Math.min(width*height, container.components.size()); i++){
            var component = container.components.get(i);
            component.x = X*width;
            component.y = Y*height;
            component.setSize(width, height);
            X++;
            if(X>=cols&&cols>0){
                X = 0;
                Y++;
            }
        }
    }
    @Override
    public Vector2f getPreferredSize(Component container){
        int cols = this.cols;
        int rows = this.rows;
        if(cols==0)cols = container.components.size()/rows+(container.components.size()%rows>0?1:0);
        if(rows==0)rows = container.components.size()/cols+(container.components.size()%cols>0?1:0);
        var maxPreferred = new Vector2f(0, 0);
        for(var comp : container.components){
            maxPreferred.max(comp.getPreferredSize());
        }
        return maxPreferred.mul(cols, rows);
    }
    public static enum Direction{
        TOP, BOTTOM, LEFT, RIGHT, CENTER;
    }
}
