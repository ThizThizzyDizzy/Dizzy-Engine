package com.thizthizzydizzy.dizzyengine;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.graphics.image.Image;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;
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
    private static HashMap<String, Integer> texturesCache = new HashMap<>();
    private static Image missingTexture = new Image(1, 1);
    static{
        missingTexture.setRGB(0, 0, Color.MAGENTA.getRGB());
    }
    public static int getTexture(String path){
        if(path==null)return getTexture(missingTexture);
        if(texturesCache.containsKey(path))return texturesCache.get(path);
        //read image
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer imageData = null;
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        try(InputStream input = getInternalResource(path)){
            if(input==null){
                Logger.error("Could not find texture: "+path+"!");
                return getTexture(missingTexture);
            }
            imageData = stbi_load_from_memory(loadData(input), width, height, BufferUtils.createIntBuffer(1), 4);
        }catch(IOException ex){
            Logger.error(ex);
        }
        if(imageData==null)throw new RuntimeException("Failed to load image: "+stbi_failure_reason());
        //finish read image
        int texture = loadGLTexture(width.get(0), height.get(0), imageData);
        stbi_image_free(imageData);
        texturesCache.put(path, texture);
        stbi_set_flip_vertically_on_load(false);
        return texture;
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
    private static final HashMap<Image, Integer> imgs = new HashMap<>();
    public static int getTexture(Image image){
        if(image==null)return 0;
        if(!imgs.containsKey(image)){
            imgs.put(image, loadGLTexture(image.getWidth(), image.getHeight(), image.getGLData()));
        }
        return imgs.get(image);
    }
    public static void deleteTexture(Image image){
        imgs.remove(image);
    }
}
