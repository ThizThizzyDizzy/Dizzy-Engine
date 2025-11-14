package com.thizthizzydizzy.dizzyengine;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.util.function.Consumer;
public class UnmanagedUpdateThread{
    public final Thread thread;
    private final Consumer<Long> func;
    private long updateCounter;
    public UnmanagedUpdateThread(String name, Consumer<Long> func, Runnable cleanupFunc, int delayMillis){
        this.func = func;
        thread = new Thread(() -> {
            Logger.push(name);
            while(DizzyEngine.isRunning()){
                func.accept(updateCounter++);
                if(delayMillis>0){
                    try{
                        Thread.sleep(delayMillis);
                    }catch(InterruptedException ex){
                        Logger.error(ex);
                    }
                }
            }
            if(cleanupFunc!=null)cleanupFunc.run();
            Logger.info("Thread Stopped");
            Logger.pop();
        }, name);
        if(DizzyEngine.isRunning())start();
    }
    public void start(){
        thread.start();
    }
}
