package com.thizthizzydizzy.dizzyengine.platform;
import java.util.Objects;
import org.lwjgl.glfw.GLFW;
public final class Input {
    public interface Backend {
        int getKey(long window, int key);
        int getMouseButton(long window, int button);
        void setClipboardString(long window, CharSequence text);
    }
    private static Backend backend;
    private Input() {}
    public static void setBackend(Backend replacement) { backend = Objects.requireNonNull(replacement); }
    public static int getKey(long window, int key) { return backend == null ? GLFW.glfwGetKey(window, key) : backend.getKey(window, key); }
    public static int getMouseButton(long window, int button) { return backend == null ? GLFW.glfwGetMouseButton(window, button) : backend.getMouseButton(window, button); }
    public static void setClipboardString(long window, CharSequence text) { if (backend == null) GLFW.glfwSetClipboardString(window, text); else backend.setClipboardString(window, text); }
}
