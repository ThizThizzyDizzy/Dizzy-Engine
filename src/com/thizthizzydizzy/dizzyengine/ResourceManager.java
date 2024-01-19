package com.thizthizzydizzy.dizzyengine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
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
}
