package com.thizthizzydizzy.dizzyengine.graphics;
import com.thizthizzydizzy.dizzyengine.ResourceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL20.*;
public class Shader{
    private static int vertexShader;
    private static int fragmentShader;
    private static int program;
    public static Shader loadInternal(String vertexShaderPath, String fragmentShaderPath){
        return new Shader(loadFile("/shaders/"+vertexShaderPath), loadFile("/shaders/"+fragmentShaderPath));
    }
    public Shader(String vertexShader, String fragmentShader){
        createShaderProgram(vertexShader, fragmentShader);
    }
    private static int compileShader(int shaderType, String src){
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, src);
        glCompileShader(shader);

        int[] success = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, success);

        if(success[0]==0){
            String typeS = shaderType+"";
            if(shaderType==GL_VERTEX_SHADER)typeS = "VERTEX";
            if(shaderType==GL_FRAGMENT_SHADER)typeS = "FRAGMENT";
            throw new RuntimeException(typeS+" Shader compilation failed: "+glGetShaderInfoLog(shader));
        }
        return shader;
    }
    private static int createShaderProgram(int vertexShader, int fragmentShader){
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        int[] success = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, success);

        if(success[0]==0){
            throw new RuntimeException("Shader program link failed: "+glGetProgramInfoLog(program));
        }
        return program;
    }
    private static void createShaderProgram(String vertex, String fragment){
        vertexShader = compileShader(GL_VERTEX_SHADER, vertex);
        fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragment);
        program = createShaderProgram(vertexShader, fragmentShader);
    }
    private static String loadFile(String path){
        try{
            String s = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceManager.getInternalResource(path)));
            String line;
            while((line = reader.readLine())!=null){
                s += "\n"+line;
            }
            reader.close();
            return s.isEmpty()?s:s.substring(1);
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
    public void setUniform4f(String varName, float x, float y, float z, float w){
        glUniform4f(glGetUniformLocation(program, varName), x, y, z, w);
    }
    public void setUniform1i(String varName, int x){
        glUniform1i(glGetUniformLocation(program, varName), x);
    }
    public void setUniform1f(String varName, float x){
        glUniform1f(glGetUniformLocation(program, varName), x);
    }
    public void setUniformMatrix4fv(String model, Matrix4f matrix){
        glUniformMatrix4fv(glGetUniformLocation(program, model), false, matrix.get(new float[16]));
    }
    public void use(){
        glUseProgram(program);
    }
    public void cleanup(){
        glDeleteProgram(program);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }
}
