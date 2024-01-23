package com.thizthizzydizzy.dizzytest;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.image.Image;
import com.thizthizzydizzy.dizzyengine.gui.Component;
import com.thizthizzydizzy.dizzyengine.gui.FlatGUI;
import com.thizthizzydizzy.dizzyengine.gui.Menu;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import com.thizthizzydizzy.dizzyengine.sound.Sound;
import com.thizthizzydizzy.dizzyengine.sound.SoundSource;
import com.thizthizzydizzy.dizzyengine.sound.SoundSystem;
import org.lwjgl.glfw.GLFW;
public class Main{
    private static boolean tickCrash;
    public static void main(String[] args){
        DizzyEngine.init("DizzyEngine Test");
        var tick = DizzyEngine.addFixedUpdateThread("Tick", Main::tick, null, 20);
        DizzyEngine.setWindowIcon(new Image(48, 48));
        DizzyEngine.addLayer(new FlatGUI());
        new Menu(){
            {
                add(new Component(100, 100, 100, 100){
                    @Override
                    public void draw(double deltaTime){
                        Renderer.setColor(1f,1f,1f,1f);
                        Renderer.fillRect((float)(x+tick.getUpdateProgress()*size.x/2), y, x+size.x, y+size.y, 0);
                    }
                });
            }
            @Override
            protected void onKey(int id, int key, int scancode, int action, int mods){
                super.onKey(id, key, scancode, action, mods);
                if(action==GLFW.GLFW_PRESS&&key==GLFW.GLFW_KEY_C&&mods==(GLFW.GLFW_MOD_CONTROL|GLFW.GLFW_MOD_SHIFT|GLFW.GLFW_MOD_ALT)){
                    throw new RuntimeException("Main Thread Crash Test");
                }
                if(action==GLFW.GLFW_PRESS&&key==GLFW.GLFW_KEY_C&&mods==(GLFW.GLFW_MOD_CONTROL|GLFW.GLFW_MOD_SHIFT)){
                    new Thread(()->{
                        throw new RuntimeException("Random Thread Crash Test");
                    }).start();
                }
                if(action==GLFW.GLFW_PRESS&&key==GLFW.GLFW_KEY_C&&mods==(GLFW.GLFW_MOD_CONTROL)){
                    tickCrash = true;
                }
            }
        }.open();
        SoundSystem.init();
        DizzyEngine.start();
    }
    private static void tick(long t){
        new SoundSource().playSound(new Sound("Vibing Over Venus.wav"));
        Logger.info(t+" SOUND SOURCES");
        if(tickCrash)throw new RuntimeException("Tick Thread Crash Test");
        if(t%10==0){
            try{
                Thread.sleep(200);
            }catch(InterruptedException ex){
                Logger.error(ex);
            }
        }
    }
}
