package com.thizthizzydizzy.dizzyengine.logging;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Stack;
public class Logger{
    private static final HashMap<Thread, Stack<String>> sourceStacks = new HashMap<>();
    private static PrintStream logStream;
    private static PrintStream crashStream;
    public static void init(){
        try{
            new File("logs").mkdir();
            logStream = new PrintStream(new File("logs", "latest.log"));
        }catch(FileNotFoundException ex){
            error("Failed to initialize log file!", ex);
        }
    }
    public static void cleanup(){
        if(logStream!=null)logStream.close();
        if(crashStream!=null)crashStream.close();
    }
    public static Stack<String> getSourceStack(){
        var thread = Thread.currentThread();
        if(!sourceStacks.containsKey(thread)){
            sourceStacks.put(thread, new Stack<>());
        }
        return sourceStacks.get(thread);
    }
    public static void push(Object source){
        push(source.getClass());
    }
    public static void push(Class source){
        push(source.getSimpleName());
    }
    public static void push(String source){
        getSourceStack().push(source);
    }
    public static void pop(){
        var stack = getSourceStack();
        if(stack.isEmpty())warn("Tried to pop empty logger source stack!", new RuntimeException());
        else getSourceStack().pop();
    }
    public static void reset(){
        getSourceStack().clear();
    }
    public static void log(MessageType type, String message, Throwable t){//TODO log to a file
        PrintStream out = type==MessageType.ERROR?System.err:System.out;
        var stack = getSourceStack();
        String source = stack.isEmpty()?null:stack.peek();
        String err = "";
        if(t!=null){
            err = "\n"+t.getClass().getName()+": "+t.getMessage();
            for(var stackTrace : t.getStackTrace())err += "\n"+stackTrace.toString();
        }
        String line = LocalDateTime.now().toString()+" "+Thread.currentThread().getName()+" "+type.toString()+": "+(source!=null?"["+source+"] ":"")+message+err;
        out.println(line);
        if(logStream!=null)logStream.println(line);
        if(crashStream!=null)crashStream.println(line);
    }
    public static void info(String message, Throwable t){
        log(MessageType.INFO, message, t);
    }
    public static void info(String message){
        info(message, null);
    }
    public static void warn(String message, Throwable t){
        log(MessageType.WARN, message, t);
    }
    public static void warn(String message){
        warn(message, null);
    }
    public static void error(String message, Throwable t){
        log(MessageType.ERROR, message, t);
    }
    public static void error(String message){
        error(message, null);
    }
    public static void setCrashLogFile(File file){
        try{
            file.getParentFile().mkdir();
            crashStream = new PrintStream(file);
        }catch(FileNotFoundException ex){
            error("Failed to set crash log file!", ex);
        }
    }
    public static enum MessageType{
        INFO,
        WARN,
        ERROR;
    }
}
