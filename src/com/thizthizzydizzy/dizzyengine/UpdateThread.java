package com.thizthizzydizzy.dizzyengine;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.util.function.Consumer;
public class UpdateThread{
    public final Thread thread;
    private final Consumer<Long> func;
    private final long deltaTimeNanos;
    public long lastUpdate;
    private long updateCounter;
    public UpdateThread(String name, Consumer<Long> func, Runnable cleanupFunc, int updateRate){
        this.func = func;
        deltaTimeNanos = 1_000_000_000l/updateRate;
        thread = new Thread(() -> {
            Logger.push(name);
            lastUpdate = System.nanoTime();
            while(DizzyEngine.isRunning()){
                long timeSinceLastUpdate = System.nanoTime()-lastUpdate;
                if(timeSinceLastUpdate>deltaTimeNanos){
                    lastUpdate+=deltaTimeNanos;
                    func.accept(updateCounter++);
                }else{
                    long nanos = deltaTimeNanos-timeSinceLastUpdate;
                    long millis = nanos/1_000_000;
                    try{
                        Thread.sleep(millis, (int)(nanos-millis*1_000_000));
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
    public double getUpdateProgress(){
        return Math.min(1, (System.nanoTime()-lastUpdate)/(double)deltaTimeNanos);
    }
    public void start(){
        thread.start();
    }
}