package com.thizthizzydizzy.dizzyengine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thizthizzydizzy.dizzyengine.debug.PerformanceTracker;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.Shader;
import com.thizthizzydizzy.dizzyengine.graphics.image.Image;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import com.thizthizzydizzy.dizzyengine.ui.UILayer;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
    public static final Vector2i screenSize = new Vector2i();
    public static final int CURSOR_LIMIT = 16;//size of the arrays used for cursors/input. Set before running INIT
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static long window;
    private static boolean windowSizeChanged;
    private static Framebuffer screenBuffer = null;
    private static final ArrayList<DizzyLayer> layers = new ArrayList<>();
    private static final ArrayList<FixedUpdateThread> fixedUpdateThreads = new ArrayList<>();
    private static final ArrayList<UnmanagedUpdateThread> unmanagedUpdateThreads = new ArrayList<>();
    private static final Matrix4f windowViewMatrix = new Matrix4f().setTranslation(0, 0, -5);
    private static boolean running;
    private static UILayer currentUIContext;
    private static Thread mainThread;
    public static boolean startMaximized = true;

    private static final ArrayList<Runnable> initFuncsGLFW = new ArrayList<>();
    
    public static boolean headless = false;
    public static void onInitGLFW(Runnable func){
        initFuncsGLFW.add(func);
    }

    public static void init(String title){
        headless = Boolean.parseBoolean(System.getProperty("dizzyengine.headless", "false"));
        Logger.init();
        mainThread = Thread.currentThread();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            boolean isCrash = thread==mainThread;
            for(var t : fixedUpdateThreads)if(thread==t.thread)isCrash = true;
            for(var t : unmanagedUpdateThreads)if(thread==t.thread)isCrash = true;
            if(isCrash){
                running = false;
                Logger.setCrashLogFile(new File("crash-reports", "crash-"+LocalDateTime.now().toString().replace(':', '-')+".log"));
                Logger.push(DizzyEngine.class);
                Logger.error("==== CRASH REPORT ====");
                Logger.error(title);
                Logger.error("Layers:");
                for(var layer : layers)Logger.error("- "+layer.getClass().getName());
                Logger.error("Logger Stack:");
                var stack = Logger.getSourceStack();
                for(int i = 0; i<stack.size()-1; i++)Logger.error("- "+stack.get(i));
                Logger.error("===== GAME CRASH =====");
                Logger.pop();
            }
            Logger.error("Uncaught Exception in Thread "+thread.getName()+":", ex);
        });
        Logger.push("INIT");
        if(headless){
            Logger.info("Running headless - skipping window initialization");
        }else{
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
            initFuncsGLFW.forEach(Runnable::run);

            Logger.info("Creating window");
            window = glfwCreateWindow(1200, 700, title, 0, 0);
            if(window==0){
                glfwTerminate();
                throw new RuntimeException("Failed to create GLFW window!");
            }
            if(startMaximized)glfwMaximizeWindow(window);
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
            Renderer.setDefaultShader(Shader.loadInternal("vert.shader", "frag.shader"));
            Logger.info("Initializing Layers");
            synchronized(layers){
                wrapEvent(layers, DizzyLayer::init);
            }
            Logger.info("Initializing Input");
            glfwSetCharCallback(window, (window, codepoint) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onChar(0, codepoint));
            });
            glfwSetCharModsCallback(window, (window, codepoint, mods) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onCharMods(0, codepoint, mods));
            });
            glfwSetCursorEnterCallback(window, (window, entered) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onCursorEnter(0, entered));
            });
            glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onCursorPos(0, xpos, ypos));
            });
            glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onKey(0, key, scancode, action, mods));
            });
            glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onMouseButton(0, button, action, mods));
            });
            glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onScroll(0, xoffset, yoffset));
            });
            glfwSetDropCallback(window, (window, count, names) -> {
                if(window==DizzyEngine.window)wrapEvent(layers, (layer) -> layer.onDrop(0, count, names));
            });
            glfwSetJoystickCallback((jid, event) -> {
                wrapEvent(layers, (layer) -> layer.onJoystick(0, jid, event));
            });
        }
        Logger.pop();
    }
    public static void setWindowIcon(Image image){
        if(headless)return;
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
    public static FixedUpdateThread addFixedUpdateThread(String name, Consumer<Long> updateThread, Runnable cleanupFunc, int updateRate){
        Logger.push("DizzyEngine");
        Logger.info("Adding fixed update thread "+name+" at "+updateRate+" updates per second");
        FixedUpdateThread thread;
        fixedUpdateThreads.add(thread = new FixedUpdateThread(name, updateThread, cleanupFunc, updateRate));
        Logger.pop();
        return thread;
    }
    public static UnmanagedUpdateThread addUnmanagedUpdateThread(String name, Consumer<Long> updateThread, Runnable cleanupFunc, int delayMillis){
        Logger.push("DizzyEngine");
        Logger.info("Adding unmanaged update thread "+name);
        UnmanagedUpdateThread thread;
        unmanagedUpdateThreads.add(thread = new UnmanagedUpdateThread(name, updateThread, cleanupFunc, delayMillis));
        Logger.pop();
        return thread;
    }
    public static <T extends DizzyLayer> T addLayer(T layer){
        synchronized(layers){
            layers.add(layer);
            if(layer instanceof UILayer ui)currentUIContext = ui;//required for opening default menus during initialization
        }
        return layer;
    }
    public static void removeLayer(DizzyLayer layer){
        synchronized(layers){
            layers.remove(layer);
            if(currentUIContext==layer)currentUIContext = null;
        }
    }
    private static void wrapEvent(List<DizzyLayer> layers, Consumer<DizzyLayer> event){
        for(int i = 0; i<layers.size(); i++){
            var layer = layers.get(i);
            wrapEvent(layer, () -> event.accept(layer));
        }
    }
    private static void wrapEvent(DizzyLayer layer, Runnable event){
        Logger.push(layer);
        if(layer instanceof UILayer ui)currentUIContext = ui;
        event.run();
        currentUIContext = null;
        Logger.pop();
    }
    public static void start(){
        running = true;
        for(var thread : unmanagedUpdateThreads)thread.start();
        for(var thread : fixedUpdateThreads)thread.start();
        double lastFrame = -1;
        Matrix4f windowProjectionMatrix = new Matrix4f();
        while(running){
            PerformanceTracker.reset();
            Logger.reset();
            Logger.push(DizzyEngine.class);
            if(DizzyEngine.headless){
                try{
                    Thread.sleep(10);
                }catch(InterruptedException ex){
                    Logger.warn("Interrupted! Shutting down...");
                    stop();
                }
            }else{
                if(glfwWindowShouldClose(window)){
                    Logger.info("Window closed!");
                    running = false;
                    continue;
                }
                if(windowSizeChanged||screenBuffer==null){
                    windowSizeChanged = false;
                    if(screenBuffer!=null){
                        screenBuffer.destroy();
                    }
                    screenBuffer = new Framebuffer(Math.max(1, screenSize.x), Math.max(1, screenSize.y));
                    synchronized(layers){
                        wrapEvent(layers, (layer) -> layer.onScreenSize(screenSize));
                    }
                    windowProjectionMatrix.setOrtho(0, screenSize.x, screenSize.y, 0, 0.1f, 10f);
                }
                //deltaTime
                double time = glfwGetTime();
                double deltaTime = lastFrame>-1?time-lastFrame:0;
                lastFrame = time;
                //reset rendering
                screenBuffer.bind();
                glViewport(0, 0, screenSize.x, screenSize.y);//TODO allow different screen size (letterboxed and/or VR)
                //clear buffers
                glClearColor(0f, 0f, 0f, 0f);
                glStencilMask(0xff);
                glClear(GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
                glStencilMask(0x00);
                glClearColor(0f, 0f, 0f, 0f);
                glClear(GL_COLOR_BUFFER_BIT);
                synchronized(layers){
                    wrapEvent(layers, (layer) -> {
                        screenBuffer.bind();//just in case it was changed
                        Renderer.reset();
                        layer.render(deltaTime);
                    });
                    Renderer.reset();
                    glBindFramebuffer(GL_FRAMEBUFFER, 0);

                    // Clear the main window before drawing; this is required for transparency
                    glClearColor(0f, 0f, 0f, 0f);
                    glClear(GL_COLOR_BUFFER_BIT);

                    glDisable(GL_CULL_FACE);
                    glDisable(GL_DEPTH_TEST);
                    Renderer.view(windowViewMatrix);
                    Renderer.projection(windowProjectionMatrix);
                    Renderer.fillRect(0, 0, screenSize.x, screenSize.y, screenBuffer.texture);//draw screen buffer to renderbuffer
                    glfwSwapBuffers(window);
                    glfwPollEvents();
                }
            }
        }
        Logger.info("Shutting down...");
        screenBuffer.destroy();
        Renderer.cleanupElements();
        glfwTerminate();
        Logger.cleanup();
    }
    public static void stop(){
        running = false;
    }
    public static boolean isRunning(){
        return running;
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
    public static UILayer getUIContext(){
        if(Thread.currentThread()!=mainThread)Logger.warn("UI context was accessed outside the main thread!");
        if(currentUIContext==null)throw new UnsupportedOperationException("No UI context found!");
        return currentUIContext;
    }
    public static boolean isKeyDown(int key){
        return glfwGetKey(window, key)==GLFW_PRESS;
    }
}
