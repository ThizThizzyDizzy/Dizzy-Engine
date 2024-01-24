package com.thizthizzydizzy.dizzyengine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
public class ResourceManager{
    public static InputStream getInternalResource(String path){
        if(!path.startsWith("/")){
            path = "/"+path;
        }
        return ResourceManager.class.getResourceAsStream(path);
    }
    public static ByteBuffer loadData(InputStream input){
        try(ByteArrayOutputStream output = new ByteArrayOutputStream()){
            int b;
            while((b = input.read())!=-1){
                output.write(b);
            }
            output.close();
            byte[] data = output.toByteArray();
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(data);
            buffer.flip();
            return buffer;
        }catch(IOException ex){
            throw new RuntimeException(ex);//TODO handle error properly!
        }
    }
    public static int loadGLTexture(int width, int height, ByteBuffer imageData){
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
        glGenerateMipmap(GL_TEXTURE_2D);
        return texture;
    }
}
