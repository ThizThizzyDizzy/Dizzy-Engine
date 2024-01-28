package com.thizthizzydizzy.dizzyengine.ui.layout.constraint;
import org.joml.Vector2f;
import org.joml.Vector4f;
public class PositionAnchorConstraint extends Constraint{
    private final float componentX;
    private final float componentY;
    private final float containerX;
    private final float containerY;
    private final float offsetX;
    private final float offsetY;
    public PositionAnchorConstraint(float componentX, float componentY, float containerX, float containerY){
        this(componentX, componentY, containerX, containerY, 0, 0);
    }
    public PositionAnchorConstraint(float componentX, float componentY, float containerX, float containerY, float offsetX, float offsetY){
        this.componentX = componentX;
        this.componentY = componentY;
        this.containerX = containerX;
        this.containerY = containerY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    @Override
    public void apply(Vector2f size, Vector4f dimensions){
        dimensions.x = size.x*containerX-dimensions.z*componentX+offsetX;
        dimensions.y = size.y*containerY-dimensions.w*componentY+offsetY;
    }
}
