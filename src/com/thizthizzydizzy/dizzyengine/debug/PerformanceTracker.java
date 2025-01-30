package com.thizthizzydizzy.dizzyengine.debug;
public class PerformanceTracker{//TODO track everything per layer
    public static int drawCalls = 0;

    // Shader Performance
    public static int glUniform1f;
    public static int glUniform2f;
    public static int glUniform3f;
    public static int glUniform4f;
    public static int glUniform1i;
    public static int glUniformMatrix4f;
    public static void reset(){
        drawCalls = 0;
        glUniform1f = 0;
        glUniform2f = 0;
        glUniform3f = 0;
        glUniform4f = 0;
        glUniform1i = 0;
        glUniformMatrix4f = 0;
    }
}
