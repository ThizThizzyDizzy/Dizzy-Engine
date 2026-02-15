package com.thizthizzydizzy.dizzyengine.terminal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TaskOutputConsumer implements TaskContext{
    private final Consumer<String> delegate;
    private boolean taskActive = false;
    private String taskName;
    private String taskDescription;
    private String currentStatus;
    private final Map<String, String> metadata = new HashMap<>();
    private final Deque<String> logBuffer = new ArrayDeque<>();
    private static final int MAX_LOG_CHARS = 1500;
    private int currentLogChars = 0;
    private boolean taskSuccess;
    private String finalMessage;
    
    public TaskOutputConsumer(Consumer<String> delegate){
        this.delegate = delegate;
    }
    
    @Override
    public void beginTask(String name, String description){
        this.taskName = name;
        this.taskDescription = description;
        this.currentStatus = "Starting...";
        this.taskActive = true;
        this.metadata.clear();
        this.logBuffer.clear();
        this.currentLogChars = 0;
        this.taskSuccess = false;
        this.finalMessage = null;
    }
    
    @Override
    public void updateStatus(String status){
        this.currentStatus = status;
    }
    
    @Override
    public void setMetadata(String key, String value){
        this.metadata.put(key, value);
    }
    
    @Override
    public void clearMetadata(){
        this.metadata.clear();
    }
    
    @Override
    public void endTask(boolean success, String finalMessage){
        this.taskSuccess = success;
        this.finalMessage = finalMessage;
        this.taskActive = false;
        if(delegate != null){
            delegate.accept("[" + taskName + "] " + finalMessage);
        }
    }
    
    @Override
    public void accept(String message){
        // Always send to delegate (for CLI terminal)
        if(delegate != null){
            delegate.accept(message);
        }
        // Also add to log buffer if task is active (for Discord embed)
        if(taskActive){
            addToLogBuffer(message);
        }
    }
    
    private void addToLogBuffer(String message){
        if(message == null || message.isEmpty()){
            return;
        }
        logBuffer.addLast(message);
        currentLogChars += message.length() + 1;
        while(currentLogChars > MAX_LOG_CHARS && logBuffer.size() > 1){
            String removed = logBuffer.removeFirst();
            currentLogChars -= removed.length() + 1;
        }
        if(!logBuffer.isEmpty() && currentLogChars > MAX_LOG_CHARS){
            String last = logBuffer.removeFirst();
            if(last.length() > MAX_LOG_CHARS){
                String truncated = last.substring(last.length() - MAX_LOG_CHARS + 3) + "...";
                logBuffer.addFirst(truncated);
                currentLogChars = truncated.length();
            }else{
                logBuffer.addFirst(last);
            }
        }
    }
    
    public boolean isTaskActive(){
        return taskActive;
    }
    
    public String getTaskName(){
        return taskName;
    }
    
    public String getTaskDescription(){
        return taskDescription;
    }
    
    public String getCurrentStatus(){
        return currentStatus;
    }
    
    public Map<String, String> getMetadata(){
        return new HashMap<>(metadata);
    }
    
    public Deque<String> getLogBuffer(){
        return new ArrayDeque<>(logBuffer);
    }
    
    public boolean isTaskSuccess(){
        return taskSuccess;
    }
    
    public String getFinalMessage(){
        return finalMessage;
    }
    
    public Consumer<String> getDelegate(){
        return delegate;
    }
}
