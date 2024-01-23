package com.thizthizzydizzy.dizzyengine.sound;
import java.nio.ByteBuffer;
import static org.lwjgl.openal.AL10.*;
public class SoundBuffer{
    private final int id;
    private boolean available;
    public SoundBuffer(int alFormat, ByteBuffer data, int frequency){
        id = alGenBuffers();
        setData(alFormat, data, frequency);
        SoundSystem.addBuffer(this);
    }
    public SoundBuffer setData(int alFormat, ByteBuffer data, int frequency){
        alBufferData(id, alFormat, data, frequency);
        available = false;
        return this;
    }
    public void cleanup(){
        alDeleteBuffers(id);
    }
    public int getID(){
        return id;
    }
    public void release(){
        available = true;
    }
    public boolean available(){
        return available;
    }
}