package com.thizthizzydizzy.dizzyengine.debug;
import com.thizthizzydizzy.dizzyengine.debug.performance.PerformanceTrackerGroup;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
public class PerformanceTracker{
    public static final PerformanceTrackerGroup rootGroup = new PerformanceTrackerGroup(null);
    private static PerformanceTrackerGroup currentGroup = rootGroup;
    
    public static void reset(){
        currentGroup = rootGroup;
        rootGroup.reset();
    }
    public static void push(Object source){
        push(source.getClass());
    }
    public static void push(Class source){
        push(source.getSimpleName());
    }
    public static void push(String source){
        currentGroup = currentGroup.subgroup(source);
    }
    public static void pop(){
        if(currentGroup.parent==null)Logger.warn("Tried to pop empty performance tracker stack!", new RuntimeException());
        else
            currentGroup = currentGroup.parent;
    }
    public static void incrementCounter(String counter){
        currentGroup.addCounter(counter, 1);
    }
}
