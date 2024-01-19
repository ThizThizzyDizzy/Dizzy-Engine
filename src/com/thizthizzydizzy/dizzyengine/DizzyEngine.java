package com.thizthizzydizzy.dizzyengine;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.Shader;
import com.thizthizzydizzy.dizzyengine.graphics.image.Image;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.util.ArrayList;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.system.MemoryUtil;
public class DizzyEngine{
    private static long window;
    public static final Vector2i screenSize = new Vector2i();
    private static boolean windowSizeChanged;
    private static Framebuffer screenBuffer = null;
    private static final ArrayList<DizzyLayer> layers = new ArrayList<>();
    public static final int CURSOR_LIMIT = 16;//size of the arrays used for cursors/input. Set before running INIT
    public static void init(String title){
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Logger.error("Uncaught Exception in Thread "+thread.getName()+":", ex);
        });
        Logger.push("INIT");
        Logger.info("Initializing GLFW");
        glfwSetErrorCallback(new GLFWErrorCallback(){
            @Override
            public void invoke(int err, long description){
                Logger.error("GLFW ERROR "+err+": "+MemoryUtil.memUTF8(description));
            }
        });
        if(!glfwInit())throw new RuntimeException("Failed to initialize GLFW!");
        Logger.info("Initializing window");
        //window
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        //multisampling //TODO graphics settings (MSAA)
        glfwWindowHint(GLFW_STENCIL_BITS, 4);
        glfwWindowHint(GLFW_SAMPLES, 4);
        //openGL
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        Logger.info("Creating window");
        window = glfwCreateWindow(1200, 700, title, 0, 0);
        if(window==0){
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window!");
        }
        glfwMaximizeWindow(window);
        Logger.info("Initializing OpenGL");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);//TODO graphics settings (VSync)
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetFramebufferSize(window, w, h);
        screenSize.set(w[0], h[0]);
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            if(window!=DizzyEngine.window)return;
            screenSize.set(width, height);
            windowSizeChanged = true;
        });
        GL.createCapabilities(true);
        Logger.info("Initializing Render Engine");
        glClearColor(0f, 0f, 0f, 0f);
        glEnable(GL_MULTISAMPLE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        glEnable(GL_STENCIL_TEST);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        Logger.info("Initializing Elements");
        Renderer.initElements();//TODO I don't like this
        Renderer.setDefaultShader(new Shader("vert.shader", "frag.shader"));
        Logger.info("Initializing Layers");
        synchronized(layers){
            for(var layer : layers)layer.init();
        }
        Logger.info("Initializing Input");
        glfwSetCharCallback(window, (window, codepoint) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onChar(0, codepoint);
        });
        glfwSetCharModsCallback(window, (window, codepoint, mods) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onCharMods(0, codepoint, mods);
        });
        glfwSetCursorEnterCallback(window, (window, entered) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onCursorEnter(0, entered);
        });
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onCursorPos(0, xpos, ypos);
        });
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onKey(0, key, scancode, action, mods);
        });
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onMouseButton(0, button, action, mods);
        });
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onScroll(0, xoffset, yoffset);
        });
        glfwSetDropCallback(window, (window, count, names) -> {
            if(window==DizzyEngine.window)for(var layer : layers)layer.onDrop(0, count, names);
        });
        glfwSetJoystickCallback((jid, event) -> {
            for(var layer : layers)layer.onJoystick(0, jid, event);
        });
        Logger.pop();
    }
    public static void addLayer(DizzyLayer layer){
        synchronized(layers){
            layers.add(layer);
        }
    }
    public static void removeLayer(DizzyLayer layer){
        synchronized(layers){
            layers.remove(layer);
        }
    }
    private static final Matrix4f windowViewMatrix = new Matrix4f().setTranslation(0, 0, -5);
    public static void start(){
        double lastFrame = -1;
        Matrix4f windowProjectionMatrix = new Matrix4f();
        while(true){
            Logger.reset();
            Logger.push(DizzyEngine.class);
            if(glfwWindowShouldClose(window)){
                Logger.info("Window closed!");
                break;
            }
            if(windowSizeChanged||screenBuffer==null){
                windowSizeChanged = false;
                if(screenBuffer!=null){
                    screenBuffer.destroy();
                }
                screenBuffer = new Framebuffer(screenSize.x, screenSize.y);
                synchronized(layers){
                    for(var layer : layers)layer.onScreenSize(screenSize);
                }
                windowProjectionMatrix.setOrtho(0, screenSize.x, screenSize.y, 0, 0.1f, 10f);
            }
            //deltaTime
            double deltaTime = 0;
            double time = glfwGetTime();
            if(lastFrame>-1)deltaTime = time-lastFrame;
            lastFrame = time;
            //reset rendering
            screenBuffer.bind();
            glViewport(0, 0, screenSize.x, screenSize.y);//TODO allow different screen size (letterboxed and/or VR)
            //clear buffers
            glClearColor(0f, 0f, 0f, 0f);
            glStencilMask(0xff);
            glClear(GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
            glStencilMask(0x00);
            glClearColor(0f, 0f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
            synchronized(layers){
                for(var layer : layers){
                    screenBuffer.bind();//just in case it was changed
                    Renderer.reset();
                    Logger.push(layer);
                    layer.render(deltaTime);
                    Logger.pop();
                }
                Renderer.reset();
                glBindFramebuffer(GL_FRAMEBUFFER, 0);
                glDisable(GL_CULL_FACE);
                glDisable(GL_DEPTH_TEST);
                Renderer.view(windowViewMatrix);
                Renderer.projection(windowProjectionMatrix);
                Renderer.fillRect(0, 0, screenSize.x, screenSize.y, screenBuffer.texture);//draw screen buffer to renderbuffer
                glfwSwapBuffers(window);
                glfwPollEvents();
            }
        }
        Logger.info("Shutting down...");
        screenBuffer.destroy();
        Renderer.cleanupElements();
        glfwTerminate();
    }
    public static void setWindowIcon(Image image){
        Logger.push("DizzyEngine");
        Logger.info("Loading window icon");
        try(GLFWImage.Buffer iconBuffer = GLFWImage.create(1); GLFWImage icon = GLFWImage.create()){
            icon.set(image.getWidth(), image.getHeight(), image.getGLData());
            iconBuffer.put(icon);
            iconBuffer.rewind();
            glfwSetWindowIcon(window, iconBuffer);
        }catch(Exception ex){
            Logger.error("Failed to load window icon!", ex);
        }
        Logger.pop();
    }
    public static <T extends DizzyLayer> T getLayer(Class<T> clazz){
        T found = null;
        for(var layer : layers){
            if(layer.getClass()==clazz){
                if(found!=null){
                    throw new UnsupportedOperationException("Found multiple layers of type "+clazz.getName()+"!");
                }
                found = (T)layer;
            }
        }
        return found;
    }
}
