package com.thizthizzydizzy.dizzyengine.sound;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.SOFTSourceLatency;
import org.joml.Vector3fc;
import static org.lwjgl.openal.AL10.*;
public class SoundSource{
    private final int id;
    public ArrayList<Sound> soundQueue = new ArrayList<>();
    public volatile SoundStream currentSound = null;
    private volatile int consumedBuffers;
    private final ArrayList<Integer> queuedBufferIds = new ArrayList<>();
    private volatile boolean prepared;
    private volatile boolean cleaned;
    public SoundSource(){
        SoundSystem.bindToCurrentThread();
        id = alGenSources();
        setPosition(new Vector3f());
        setVelocity(new Vector3f());
        setPitch(1);
        setGain(1);
        setLoop(false);
        SoundSystem.addSource(this);
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

    public synchronized void queueSound(Sound sound){
        soundQueue.add(sound);
    }
    /**
     * Stop the current sound and all queued sounds, and immediately start
     * playing the specified sound.
     *
     * @param sound the sound to play
     */
    public synchronized void playSound(Sound sound){
        stopPlaying();
        startPlaying(sound);
    }
    /**
     * Stop playing the current sound and clear all queued sounds.
     */
    public synchronized void stopPlaying(){
        soundQueue.clear();
        skip();
    }
    /**
     * Stop playing the current sound. This does not clear queued sounds.
     */
    public synchronized void skip(){
        if(cleaned)return;
        var stream = currentSound;
        currentSound = null;
        consumedBuffers = 0;
        prepared = false;
        alSourceStop(id);
        // AL_INITIAL sources have unplayed buffers, which cannot be unqueued as processed.
        // Detaching clears the whole queue for either INITIAL or STOPPED sources.
        alSourcei(id, AL_BUFFER, 0);
        for(int buffer : queuedBufferIds)SoundSystem.releaseBuffer(buffer);
        queuedBufferIds.clear();
        soundQueue.clear();
        if(stream!=null)stream.close();
    }

    public int getState(){
        if(cleaned)return AL_STOPPED;
        return alGetSourcei(id, AL_SOURCE_STATE);
    }
    private void startPlaying(Sound sound){
        try{
            var stream = sound.stream();
            if(!stream.hasNext()){
                stream.close();
                Logger.info("Ignored request to play empty song!");
                return;
            }
            Logger.info("Started Playing Sound");
            currentSound = stream;
            consumedBuffers = 0;
            var buffer = currentSound.next();
            if(buffer==null)return;
            queueBuffer(buffer.getID());
            alSourcePlay(id);
        }catch(IOException|UnsupportedAudioFileException ex){
            Logger.error(ex);
        }
    }

    /** Load and queue five seconds (or the whole shorter sound) without starting playback. */
    public synchronized void prepareSound(Sound sound) throws IOException, UnsupportedAudioFileException{
        if(cleaned)throw new IllegalStateException("Source was cleaned up");
        stopPlaying();
        SoundStream stream = sound.stream();
        currentSound = stream;
        prepared = true; // SoundSystem.update must never consume or start a prepared source.
        try{
            int count = 0;
            int preloadBuffers = (int)Math.ceil(5.0*stream.getFrameRate()/SoundSystem.FRAMES_PER_BUFFER);
            while(count<Math.min(preloadBuffers, SoundSystem.BUFFER_QUEUE_SIZE)&&stream.hasNext()){
                SoundBuffer buffer = stream.next();
                if(buffer==null)throw new IOException("Unable to read audio buffer");
                queueBuffer(buffer.getID());
                count++;
            }
            if(count==0)throw new UnsupportedAudioFileException("Empty or unsupported audio file");
            int error = alGetError();
            if(error!=AL_NO_ERROR)throw new IOException("OpenAL preparation error: "+error);
        }catch(IOException|UnsupportedAudioFileException|RuntimeException ex){
            stopPlaying();
            throw ex;
        }
    }
    /** Deadline path: no file I/O, decoding, logging, or allocation. */
    public synchronized void startPrepared(){
        if(cleaned||!prepared)throw new IllegalStateException("Source is not prepared");
        alSourcePlay(id);
        prepared = false;
    }
    public boolean isPrepared(){ return prepared; }
    public long getPlaybackLatencyNanos(){
        if(!AL.getCapabilities().AL_SOFT_source_latency)return -1;
        long[] values = new long[2];
        SOFTSourceLatency.alGetSourcei64vSOFT(id, SOFTSourceLatency.AL_SAMPLE_OFFSET_LATENCY_SOFT, values);
        return values[1];
    }
    public void cleanup(){
        // Never take the sources-list lock while holding the source lock.
        SoundSystem.removeSource(this);
        synchronized(this){
            if(cleaned)return;
            stopPlaying();
            alDeleteSources(id);
            cleaned = true;
        }
    }
    public synchronized void update(){
        if(cleaned||prepared)return;
        if(currentSound!=null){
            int processed = alGetSourcei(id, AL_BUFFERS_PROCESSED);
            for(int i = 0; i<processed; i++){
                int buffer = alSourceUnqueueBuffers(id);
                queuedBufferIds.remove(Integer.valueOf(buffer));
                SoundSystem.releaseBuffer(buffer);
            }
            consumedBuffers += processed;
            if(currentSound.hasNext()){
                int queued = alGetSourcei(id, AL_BUFFERS_QUEUED);
                if(queued<SoundSystem.BUFFER_QUEUE_SIZE){
                    var buf = currentSound.next().getID();
                    queueBuffer(buf);
                }
            }else if(getState()==AL_STOPPED){
                currentSound.close();
                currentSound = null;
            }
            // A streaming read can stall long enough to drain the OpenAL queue.
            // Queueing new data does not restart a stopped source automatically.
            if(currentSound!=null&&getState()==AL_STOPPED&&alGetSourcei(id, AL_BUFFERS_QUEUED)>0)alSourcePlay(id);
        }
        if(currentSound==null&&!soundQueue.isEmpty()){
            startPlaying(soundQueue.remove(0));
        }
    }
    private void queueBuffer(int buffer){
        alSourceQueueBuffers(id, buffer);
        queuedBufferIds.add(buffer);
    }
    public synchronized void play(){
        alSourcePlay(id);
    }
    public synchronized void pause(){
        alSourcePause(id);
    }
    public float getPlayhead(){
        var stream = currentSound;
        return stream==null?-1:consumedBuffers*SoundSystem.FRAMES_PER_BUFFER/stream.getFrameRate();
    }
    /** Playback position including the offset within queued audio, independent of buffer refill cadence. */
    public synchronized float getPrecisePlayhead(){
        var stream=currentSound;
        if(stream==null||cleaned)return -1;
        return Math.min(getDuration(),consumedBuffers*SoundSystem.FRAMES_PER_BUFFER/stream.getFrameRate()+alGetSourcef(id,org.lwjgl.openal.AL11.AL_SEC_OFFSET));
    }
    public float getDuration(){
        var stream = currentSound;
        return stream==null?-1:stream.getDurationInFrames()/stream.getFrameRate();
    }
}
