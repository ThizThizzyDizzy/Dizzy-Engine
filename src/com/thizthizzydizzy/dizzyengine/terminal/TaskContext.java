package com.thizthizzydizzy.dizzyengine.terminal;
import java.util.function.Consumer;

public interface TaskContext extends Consumer<String>{
    void beginTask(String name, String description);
    void updateStatus(String status);
    void setMetadata(String key, String value);
    void clearMetadata();
    void endTask(boolean success, String finalMessage);
}
