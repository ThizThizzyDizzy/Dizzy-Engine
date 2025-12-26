package com.thizthizzydizzy.dizzyengine.graphics.batch;
import com.thizthizzydizzy.dizzyengine.debug.performance.PerformanceTracker;
import com.thizthizzydizzy.dizzyengine.graphics.Material;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Triangle;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Vertex;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL43.*;
import org.lwjgl.system.MemoryUtil;
public class Batch implements AutoCloseable{
    public final BatchType type;
    public final List<WorldObject> objects = new ArrayList<>();
    public final Material material;
    public boolean dirty = false;
    private boolean initialized = false;
    public Batch(BatchType type, Material material){
        this.type = type;
        this.material = material;
    }
    public boolean matches(WorldObject object){
        if(type==BatchType.INDIVIDUAL)return false;

        if(!(object instanceof Instanceable))return false;

        if(type==BatchType.DYNAMIC_INSTANCED&&object.isStatic())return false;
        if(type==BatchType.STATIC_INSTANCED&&!object.isStatic())return false;

        if(!material.equals(object.getMaterial()))return false;

        if(objects.isEmpty())return true;
        return ((Instanceable)object).canInstance(objects.getFirst());
    }
    public void render(){
        if(!initialized)init();
        if(material.shader==null){
            if(type==BatchType.INDIVIDUAL)Renderer.resetShader();
            else Renderer.resetShaderInstanced();
        }
        else{
            if(type==BatchType.INDIVIDUAL)Renderer.setShader(material.shader);
            else Renderer.setShaderInstanced(material.shader);
        }
        
        // ensure the new shader has projection/view matricies
        Renderer.restoreProjection();
        Renderer.restoreView();
        
        Renderer.bindTexture(material.texture);
        Renderer.setColor(Color.WHITE);
        PerformanceTracker.push(type);
        type.render(this);
        PerformanceTracker.pop();
        Renderer.resetShader();
    }

    int vao, vbo, ebo;
    int ssbo;
    int tris;
    FloatBuffer modelMatrixBuffer;
    private void init(){
        initialized = true;
        if(type==BatchType.INDIVIDUAL)return;
        
        // Send mesh data to GPU
        
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        ssbo = glGenBuffers();
        
        var mesh = ((Instanceable)objects.getFirst()).getMesh();
        float[] verticies = new float[mesh.verticies.size()*Vertex.SIZE];
        for(int v = 0; v<mesh.verticies.size(); v++){
            var vertex = mesh.verticies.get(v);
            System.arraycopy(vertex.getGLData(), 0, verticies, v*Vertex.SIZE, Vertex.SIZE);
        }
        
        int[] indicies = new int[mesh.triangles.size()*Triangle.SIZE];
        for(int t = 0; t<mesh.triangles.size(); t++){
            var triangle = mesh.triangles.get(t);
            System.arraycopy(triangle.getGLData(), 0, indicies, t*Triangle.SIZE, Triangle.SIZE);
        }
        tris = mesh.triangles.size();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, verticies, type.glDrawHint);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, type.glDrawHint);

        //pos
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8*4, 0);

        //norm
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8*4, 3*4);

        //tex
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*4, 6*4);
        
        glBindVertexArray(0);
        
        modelMatrixBuffer = MemoryUtil.memAllocFloat(objects.size()*16);
        
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, objects.size()*64, type.glDrawHint);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        if(type==BatchType.STATIC_INSTANCED)writeModelMatricies();
    }
    @Override
    public void close() throws Exception{
        if(!initialized)return;
        MemoryUtil.memFree(modelMatrixBuffer);
        glDeleteBuffers(ebo);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
    void writeModelMatricies(){
        modelMatrixBuffer.clear();
        for(var object : objects){
            new Matrix4f().translate(object.getPosition()).get(modelMatrixBuffer);
            modelMatrixBuffer.position(modelMatrixBuffer.position()+16); // increment it >.>
        }
        modelMatrixBuffer.flip();
        
        // Write to GPU
        
        glBindBuffer(GL_ARRAY_BUFFER, ssbo);
        glBufferData(GL_ARRAY_BUFFER, (long)objects.size()*64, type.glDrawHint);
        glBufferSubData(GL_ARRAY_BUFFER, 0, modelMatrixBuffer);
    }
    void drawInstanced(){
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);
        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, tris*Triangle.SIZE, GL_UNSIGNED_INT, 0, objects.size());
        PerformanceTracker.incrementCounter("glDrawElementsInstanced ("+objects.getFirst().getClass().getSimpleName()+" x"+objects.size()+")");
        glBindVertexArray(0);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }
    void drawIndividual(){
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, tris*Triangle.SIZE, GL_UNSIGNED_INT, 0);
        PerformanceTracker.incrementCounter("glDrawElements");
        glBindVertexArray(0);
    }
}
