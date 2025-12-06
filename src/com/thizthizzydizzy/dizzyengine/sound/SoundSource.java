package com.thizthizzydizzy.dizzyengine.sound;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.openal.AL10.*;
public class SoundSource{
    private final int id;
    public ArrayList<Sound> soundQueue = new ArrayList<>();
    public SoundStream currentSound = null;
    private int consumedBuffers;
    public SoundSource(){
        SoundSystem.addSource(this);
        id = alGenSources();
        setPosition(new Vector3f());
        setVelocity(new Vector3f());
        setPitch(1);
        setGain(1);
        setLoop(false);
    }

    public void setPosition(Vector3fc pos){
        alSource3f(id, AL_POSITION, pos.x(), pos.y(), pos.z());
    }
    public void setVelocity(Vector3fc vel){
        alSource3f(id, AL_VELOCITY, vel.x(), vel.y(), vel.z());
    }
    public void setPitch(float pitch){
        alSourcef(id, AL_PITCH, pitch);
    }
    public void setGain(float gain){
        alSourcef(id, AL_GAIN, gain);
    }
    public void setLoop(boolean loop){
        alSourcei(id, AL_LOOPING, loop?AL_TRUE:AL_FALSE);
    }

    public void queueSound(Sound sound){
        soundQueue.add(sound);
    }
    /**
     * Stop the current sound and all queued sounds, and immediately start
     * playing the specified sound.
     *
     * @param sound the sound to play
     */
    public void playSound(Sound sound){
        stopPlaying();
        startPlaying(sound);
    }
    /**
     * Stop playing the current sound and clear all queued sounds.
     */
    public void stopPlaying(){
        soundQueue.clear();
        skip();
    }
    /**
     * Stop playing the current sound. This does not clear queued sounds.
     */
    public synchronized void skip(){
        var stream = currentSound;
        currentSound = null;
        consumedBuffers = 0;
        if(getState()!=AL_STOPPED){
            int processed = alGetSourcei(id, AL_BUFFERS_QUEUED);
            alSourceStop(id);
            for(int i = 0; i<processed; i++){
                SoundSystem.releaseBuffer(alSourceUnqueueBuffers(id));
            }
        }
        soundQueue.clear();
        if(stream!=null)stream.close();
    }

    public int getState(){
        return alGetSourcei(id, AL_SOURCE_STATE);
    }
    private void startPlaying(Sound sound){
        try{
            var stream = sound.stream();
            if(!stream.hasNext()){
                Logger.info("Ignored request to play empty song!");
                return;
            }
            Logger.info("Started Playing Sound");
            currentSound = stream;
            consumedBuffers = 0;
            var buffer = currentSound.next();
            if(buffer==null)return;
            alSourceQueueBuffers(id, buffer.getID());
            alSourcePlay(id);
        }catch(IOException|UnsupportedAudioFileException ex){
            Logger.error(ex);
        }
    }

    public void cleanup(){
        stopPlaying();
        alDeleteSources(id);
    }
    public synchronized void update(){
        if(currentSound!=null){
            int processed = alGetSourcei(id, AL_BUFFERS_PROCESSED);
            for(int i = 0; i<processed; i++){
                SoundSystem.releaseBuffer(alSourceUnqueueBuffers(id));
            }
            consumedBuffers += processed;
            if(currentSound.hasNext()){
                int queued = alGetSourcei(id, AL_BUFFERS_QUEUED);
                if(queued<SoundSystem.BUFFER_QUEUE_SIZE){
                    var buf = currentSound.next().getID();
                    alSourceQueueBuffers(id, buf);
                }
            }else if(getState()==AL_STOPPED){
                currentSound.close();
                currentSound = null;
            }
        }
        if(currentSound==null&&!soundQueue.isEmpty()){
            startPlaying(soundQueue.remove(0));
        }
    }
    public void play(){
        alSourcePlay(id);
    }
    public void pause(){
        alSourcePause(id);
    }
    public float getPlayhead(){
        return currentSound==null?-1:consumedBuffers*SoundSystem.FRAMES_PER_BUFFER/currentSound.getFrameRate();
    }
    public float getDuration(){
        return currentSound==null?-1:currentSound.getDurationInFrames()/currentSound.getFrameRate();
    }
}
