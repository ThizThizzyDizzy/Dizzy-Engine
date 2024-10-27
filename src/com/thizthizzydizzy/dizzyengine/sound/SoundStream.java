package com.thizthizzydizzy.dizzyengine.sound;
public interface SoundStream{
    public Sound getSound();
    public boolean hasNext();
    public SoundBuffer next();
    public void close();
    public float getFrameRate();
}