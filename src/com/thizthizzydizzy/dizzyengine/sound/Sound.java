package com.thizthizzydizzy.dizzyengine.sound;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ResourceManager;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
public class Sound{
    private Supplier<InputStream> soundSupplier;
    private ArrayList<SoundBuffer> buffers;
    public Sound(String path){
        this(() -> ResourceManager.getInternalResource(path));
    }
    public Sound(Supplier<InputStream> soundSupplier){
        this.soundSupplier = soundSupplier;
    }
    public synchronized void getBuffers(Consumer<SoundBuffer> bufferConsumer){
        if(buffers!=null){
            if(buffers.get(buffers.size()-1)==null){
                new Thread(() -> {
                    int i = 0;
                    while(i<buffers.size()&&DizzyEngine.isRunning()){
                        var buffer = buffers.get(i);
                        if(buffer!=null){
                            i++;
                            bufferConsumer.accept(buffer);
                        }
                        try{
                            Thread.sleep(10);
                        }catch(InterruptedException ex){
                            break;
                        }
                    }
                }, "Sound Streaming Thread "+UUID.randomUUID().toString()).start();
            }
            for(SoundBuffer buffer : buffers)bufferConsumer.accept(buffer);
        }else{
            buffers = new ArrayList<>();
            buffers.add(null);
            new Thread(() -> {
                try(AudioInputStream stream = AudioSystem.getAudioInputStream(soundSupplier.get())){
                    var format = stream.getFormat();
                    if(format.isBigEndian())
                        throw new UnsupportedAudioFileException("Big Endian formats are not supported yet!");
                    int alFormat = switch(format.getChannels()){
                        case 1 ->
                            switch(format.getSampleSizeInBits()){
                                case 8 ->
                                    AL10.AL_FORMAT_MONO8;
                                case 16 ->
                                    AL10.AL_FORMAT_MONO16;
                                default ->
                                    -1;
                            };
                        case 2 ->
                            switch(format.getSampleSizeInBits()){
                                case 8 ->
                                    AL10.AL_FORMAT_STEREO8;
                                case 16 ->
                                    AL10.AL_FORMAT_STEREO16;
                                default ->
                                    -1;
                            };
                        default ->
                            -1;
                    };
                    byte[] bytes = stream.readAllBytes();//TODO cut it up by SoundSystem.FRAMES_PER_BUFFER frames
                    ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
                    data.flip();
                    buffers.add(buffers.size()-1, new SoundBuffer(alFormat, data, (int)format.getSampleRate()));
                    buffers.remove(buffers.size()-1);//mark done processing
                }catch(UnsupportedAudioFileException|IOException ex){
                    Logger.error(ex);
                }
            }, "Sound Loading Thread "+UUID.randomUUID().toString()).start();
            getBuffers(bufferConsumer);
        }
    }
    public SoundStream stream() throws IOException, UnsupportedAudioFileException{
        return new SoundStream(){
            final AudioInputStream in;
            private AudioFormat format;
            private int alFormat;
            {
                in = AudioSystem.getAudioInputStream(new BufferedInputStream(soundSupplier.get()));
                format = in.getFormat();
                if(format.isBigEndian())
                    throw new UnsupportedAudioFileException("Big Endian audio files are not supported!");
                alFormat = switch(format.getChannels()){
                    case 1 ->
                        switch(format.getSampleSizeInBits()){
                            case 8 ->
                                AL10.AL_FORMAT_MONO8;
                            case 16 ->
                                AL10.AL_FORMAT_MONO16;
                            default ->
                                -1;
                        };
                    case 2 ->
                        switch(format.getSampleSizeInBits()){
                            case 8 ->
                                AL10.AL_FORMAT_STEREO8;
                            case 16 ->
                                AL10.AL_FORMAT_STEREO16;
                            default ->
                                -1;
                        };
                    default ->
                        -1;
                };
            }
            @Override
            public Sound getSound(){
                return Sound.this;
            }
            @Override
            public boolean hasNext(){
                try{
                    return in.available()>0;
                }catch(IOException ex){
                    Logger.error(ex);
                }
                return false;
            }
            @Override
            public SoundBuffer next(){
                try{
                    var bytes = in.readNBytes(SoundSystem.FRAMES_PER_BUFFER*format.getFrameSize());
                    var data = BufferUtils.createByteBuffer(bytes.length).put(bytes).flip();
                    return SoundSystem.getBuffer(alFormat, data, (int)format.getSampleRate());
                }catch(IOException ex){
                    Logger.error(ex);
                }
                return null;
            }
            @Override
            public void close(){
                try{
                    in.close();
                }catch(IOException ex){
                    Logger.error(ex);
                }
            }
            @Override
            public float getFrameRate(){
                return format.getFrameRate();
            }
        };
    }
}
