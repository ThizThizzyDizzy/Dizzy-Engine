package com.thizthizzydizzy.dizzyengine.sound;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ResourceManager;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import static org.lwjgl.openal.AL10.*;
import org.lwjgl.openal.ALC;
import static org.lwjgl.openal.ALC10.*;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.EXTEfx;
public class SoundSystem{
    public static int FRAMES_PER_BUFFER = 65536;//Number of frames per buffer for streaming audio
    private static final ArrayList<SoundSource> sources = new ArrayList<>();
    private static final ArrayList<SoundBuffer> buffers = new ArrayList<>();
    public static int BUFFER_QUEUE_SIZE = 8;
    public static void init(){
        DizzyEngine.addFixedUpdateThread("SoundSystem", SoundSystem::updateSounds, SoundSystem::cleanup, 60);
        long device = alcOpenDevice((ByteBuffer)null);//grab default device
        ALCCapabilities deviceCapabilites = ALC.createCapabilities(device);
        IntBuffer contextAttributes = BufferUtils.createIntBuffer(16);
        contextAttributes.put(ALC_REFRESH);
        contextAttributes.put(60);
        contextAttributes.put(ALC_SYNC);
        contextAttributes.put(ALC_FALSE);
        contextAttributes.put(EXTEfx.ALC_MAX_AUXILIARY_SENDS);
        contextAttributes.put(2);
        contextAttributes.put(0);
        contextAttributes.flip();
        long newContext = alcCreateContext(device, contextAttributes);
        if(!alcMakeContextCurrent(newContext))throw new RuntimeException("Failed to make context current");
        AL.createCapabilities(deviceCapabilites);
        
        //define listener
        alListener3f(AL_VELOCITY, 0f, 0f, 0f);
        alListener3f(AL_ORIENTATION, 0f, 0f, -1f);
        checkALError();
    }
    public static void updateSounds(long updateCounter){
        synchronized(sources){
            for(var source : sources){
                source.update();
                checkALError();
            }
        }
    }
    public static void cleanup(){
        synchronized(sources){
            for(var source : sources){
                source.cleanup();
            }
        }
        synchronized(buffers){
            for(var buffer : buffers){
                buffer.cleanup();
            }
        }
    }
    public static void releaseBuffer(int id){
        for(var buffer : buffers)if(buffer.getID()==id)buffer.release();
    }
    public static SoundBuffer getBuffer(int format, ByteBuffer data, int frequency){
        for(var buffer : buffers){
            if(buffer.available())return buffer.setData(format, data, frequency);
        }
        return new SoundBuffer(format, data, frequency);
    }
    private static void checkALError(){
        int err = alGetError();
        if(err!=0)Logger.error("OpenAL Error "+err);
    }
    static void addSource(SoundSource source){
        synchronized(sources){
            sources.add(source);
        }
    }
    static void addBuffer(SoundBuffer buffer){
        synchronized(buffers){
            buffers.add(buffer);
        }
    }
}