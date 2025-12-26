package com.thizthizzydizzy.dizzyengine.graphics;
import java.util.Objects;
public class Material{
    public final Shader shader;
    public final int texture;
    public Material(Shader shader, int texture){
        this.shader = shader;
        this.texture = texture;
    }
    @Override
    public boolean equals(Object obj){
        if(obj instanceof Material m){
            return Objects.equals(shader, m.shader)&&texture==m.texture;
        }
        return false;
    }
    @Override
    public int hashCode(){
        int hash = 5;
        hash = 37*hash+Objects.hashCode(this.shader);
        hash = 37*hash+this.texture;
        return hash;
    }
}
