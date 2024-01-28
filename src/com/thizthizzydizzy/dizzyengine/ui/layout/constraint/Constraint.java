package com.thizthizzydizzy.dizzyengine.ui.layout.constraint;
import org.joml.Vector2f;
import org.joml.Vector4f;
public abstract class Constraint{
    public abstract void apply(Vector2f size, Vector4f dimensions);
}