package com.thizthizzydizzy.dizzyengine.ui.layout.constraint;
import org.joml.Vector2f;
import org.joml.Vector4f;
public class BoundsConstraint extends Constraint{
    private final float left;
    private final float leftOffset;
    private final float right;
    private final float rightOffset;
    private final float top;
    private final float topOffset;
    private final float bottom;
    private final float bottomOffset;
    public BoundsConstraint(float left, float right, float top, float bottom){
        this(left, 0, right, 0, top, 0, bottom, 0);
    }
    public BoundsConstraint(float left, float leftOffset, float right, float rightOffset, float top, float topOffset, float bottom, float bottomOffset){
        this.left = left;
        this.leftOffset = leftOffset;
        this.right = right;
        this.rightOffset = rightOffset;
        this.top = top;
        this.topOffset = topOffset;
        this.bottom = bottom;
        this.bottomOffset = bottomOffset;
    }
    @Override
    public void apply(Vector2f size, Vector4f dimensions){
        dimensions.set(left*size.x+leftOffset, top*size.y+topOffset, left*size.x+leftOffset-(right*size.x+rightOffset), top*size.y+topOffset-(bottom*size.y-bottomOffset));
    }
}
