package com.thizthizzydizzy.dizzyengine.graphics.batch;
import com.thizthizzydizzy.dizzyengine.debug.performance.PerformanceTracker;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL15.*;
public enum BatchType{
    STATIC_INSTANCED(GL_STATIC_DRAW){
        @Override
        void render(Batch batch){
            batch.drawInstanced();
        }
    },
    DYNAMIC_INSTANCED(GL_DYNAMIC_DRAW){
        @Override
        void render(Batch batch){
            batch.writeModelMatricies();
            batch.drawInstanced();
        }
    },
    INDIVIDUAL(GL_STREAM_DRAW){
        @Override
        void render(Batch batch){
            for(var object : batch.objects){
                Renderer.pushModel(new Matrix4f().translation(object.getPosition()));
                PerformanceTracker.push(object);
                object.render();
                PerformanceTracker.pop();
                Renderer.popModel();
            }
        }
    };
    public final int glDrawHint;
    private BatchType(int glDrawHint){
        this.glDrawHint = glDrawHint;
    }
    abstract void render(Batch batch);
}
