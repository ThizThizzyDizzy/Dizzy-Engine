package com.thizthizzydizzy.dizzyengine.graphics;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import java.util.HashMap;
import java.util.Stack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
public class Renderer{
    private static Shader defaultShader;
    private static Shader shader;
    private static Stack<Bound> boundStack = new Stack<>();
    private static Matrix4fStack modelMatStack = new Matrix4fStack(64);
    private static final HashMap<String, Element> elements = new HashMap<>();
    static{
        elements.put("rect", new Element(){
            public int vao, vbo, ebo;
            @Override
            public void init(){
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();

                float[] verticies = new float[]{
                    0, 0, 0, 0, 0, 1, 0, 1,//top left
                    0, 1, 0, 0, 0, 1, 0, 0,//bottom left
                    1, 0, 0, 0, 0, 1, 1, 1,//top right
                    1, 1, 0, 0, 0, 1, 1, 0//bottom right
                };
                int[] indicies = new int[]{
                    1, 0, 2,
                    3, 1, 2
                };

                glBindVertexArray(vao);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, verticies, GL_STREAM_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STREAM_DRAW);

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
            }
            @Override
            public void draw(){
                glBindVertexArray(vao);
                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
                glBindVertexArray(0);
            }
            @Override
            public void cleanup(){
                glDeleteBuffers(ebo);
                glDeleteBuffers(vbo);
                glDeleteVertexArrays(vao);
            }
        });
    }
    public static void initElements(){
        for(Element e : elements.values())e.init();
    }
    public static void cleanupElements(){
        for(Element e : elements.values())e.cleanup();
    }
    public static void fillRect(float left, float top, float right, float bottom){
        fillRect(left, top, right, bottom, 0);
    }
    public static void fillRect(float left, float top, float right, float bottom, int texture){
        if(right<left){
            float r = left;
            float l = right;
            right = r;
            left = l;
        }
        if(bottom<top){
            float b = top;
            float t = bottom;
            bottom = b;
            top = t;
        }
        bindTexture(texture);
        drawElement("rect", left, top, right-left, bottom-top);
    }
    public static void setColor(Color c){
        setColor(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, c.getAlpha()/255f);
    }
    public static void setColor(Color c, float alpha){
        setColor(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, c.getAlpha()/255f*alpha);
    }
    public static void setColor(float red, float green, float blue, float alpha){
        shader.setUniform4f("color", red, green, blue, alpha);
    }
    public static void setDefaultShader(Shader shader){
        defaultShader = shader;
    }
    public static void setShader(Shader shader){
        Renderer.shader = shader;
        glUseProgram(shader.shaderID);
    }
    private static Matrix4f createModelMatrix(float x, float y, float scaleX, float scaleY){
        return new Matrix4f().setTranslation(x, y, 0).scaleXY(scaleX, scaleY);
    }
    public static void model(Matrix4f matrix){
        setExactModelMatrix(modelMatStack.mul(matrix, matrix));
    }
    public static void pushModel(Matrix4f matrix){
        modelMatStack.pushMatrix();
        modelMatStack.mul(matrix);
        resetModelMatrix();
    }
    public static void popModel(){
        modelMatStack.popMatrix();
        resetModelMatrix();
    }
    public static void setModel(Matrix4f matrix){
        resetModelMatrix();
        model(matrix);
    }
    private static void setExactModelMatrix(Matrix4f matrix){
        glUniformMatrix4fv(glGetUniformLocation(shader.shaderID, "model"), false, matrix.get(new float[16]));
    }
    public static void resetModelMatrix(){
        model(new Matrix4f());
    }
    public static void view(Matrix4f matrix){
        glUniformMatrix4fv(glGetUniformLocation(shader.shaderID, "view"), false, matrix.get(new float[16]));
    }
    public static void projection(Matrix4f matrix){
        glUniformMatrix4fv(glGetUniformLocation(shader.shaderID, "projection"), false, matrix.get(new float[16]));
    }
    public static void unbindTexture(){
        bindTexture(0);
    }
    public static void bindTexture(int tex){
        glBindTexture(GL_TEXTURE_2D, tex);
        if(tex==0)shader.setUniform4f("noTex", 1f, 1f, 1f, 0f);
        else shader.setUniform4f("noTex", 0f, 0f, 0f, 0f);
    }
    public static void bound(float left, float top, float right, float bottom){
        boundStack.push(new Bound(modelMatStack.get(new Matrix4f())){
            @Override
            void draw(){
                var size = DizzyEngine.screenSize;
                fillRect(-size.x, -size.y, left, size.y);
                fillRect(left, -size.y, right, top);
                fillRect(left, bottom, right, size.y);
                fillRect(right, -size.y, size.x, size.y);
            }
        });
        redrawStencil();
    }
    public static void translate(float x, float y){
        translate(x, y, 1, 1);
    }
    public static void translate(float x, float y, float sx, float sy){
        modelMatStack.pushMatrix();
//        createModelMatrix(x, y, sx, sy).mul(modelMatStack, modelMatStack);
        modelMatStack.mul(createModelMatrix(x, y, sx, sy));
        resetModelMatrix();
    }
    public static void redrawStencil(){
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilFunc(GL_ALWAYS, 1, 0xff);
        glStencilMask(0xff);
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_STENCIL_BUFFER_BIT);
        glColorMask(false, false, false, false);
        glDepthMask(false);
        for(int i = 0; i<boundStack.size(); i++){
            Bound bound = boundStack.get(i);
            setExactModelMatrix(bound.modelMatrix);
            bound.draw();
        }
        glStencilFunc(GL_NOTEQUAL, 1, 0xff);
        glDepthMask(true);
        glColorMask(true, true, true, true);
        glStencilMask(0x00);
        resetModelMatrix();
    }
    public static void unTranslate(){
        modelMatStack.popMatrix();
        resetModelMatrix();
    }
    public static void unBound(){
        boundStack.pop();
        redrawStencil();
    }
    public static void clearTranslationsAndBounds(){
        clearTranslations();
        clearBounds();
    }
    public static void clearTranslations(){
        modelMatStack.clear();
        resetModelMatrix();
    }
    public static void clearBounds(){
        boundStack.clear();
        redrawStencil();
    }
    private static abstract class Bound{
        private final Matrix4f modelMatrix;
        public Bound(Matrix4f modelMatrix){
            this.modelMatrix = modelMatrix;
        }
        abstract void draw();
    }
    public static void drawElement(String name, float x, float y, float width, float height){
        drawElement(name, x, y, 1, width, height, 1);
    }
    public static void drawElement(String name, float x, float y, float z, float width, float height, float depth){
        model(new Matrix4f().setTranslation(x, y, z).scale(width, height, depth));
        drawElement(name);
        resetModelMatrix();
    }
    private static void drawElement(String name){
        if(!elements.containsKey(name))throw new IllegalArgumentException("Cannot draw element: "+name+" does not exist!");
        elements.get(name).draw();
    }
    public static void reset(){
        setShader(defaultShader);
        clearTranslationsAndBounds();
        unbindTexture();
        setColor(1f, 1f, 1f, 1f);
    }
    public static interface Element{
        public void init();
        public void draw();
        public void cleanup();
    }
}