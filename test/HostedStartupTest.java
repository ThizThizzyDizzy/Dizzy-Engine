import com.thizthizzydizzy.dizzyengine.*;
import com.thizthizzydizzy.dizzyengine.platform.GL;
import com.thizthizzydizzy.dizzyengine.platform.GLBackend;
import java.lang.reflect.Proxy;
import com.thizthizzydizzy.dizzyengine.graphics.batch.*;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Mesh;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
import java.nio.FloatBuffer;
import com.thizthizzydizzy.dizzyengine.graphics.Shader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector2i;

/** Run with java -cp 'build/classes:build/test/classes:lib/*' HostedStartupTest. */
public class HostedStartupTest{
    private static void check(boolean condition, String message){
        if(!condition)throw new AssertionError(message);
    }
    private static class TestInstance extends WorldObject implements Instanceable{
        public boolean canInstance(WorldObject other){return true;}
        public Mesh getMesh(){return new Mesh();}
        public float[] getInstanceData(){return new float[]{7, 9};}
    }
    private static void checkBatchBuffers() throws Exception{
        // Exercise allocation, upload, and cleanup with the fake GL backend and no LWJGL natives.
        for(var type : new BatchType[]{BatchType.STATIC_INSTANCED, BatchType.DYNAMIC_INSTANCED}){
            try(var batch = new Batch(type, null)){
                batch.objects.add(new TestInstance());
                var init = Batch.class.getDeclaredMethod("init");
                init.setAccessible(true);
                init.invoke(batch);
                var write = Batch.class.getDeclaredMethod("writeModelMatricies");
                write.setAccessible(true);
                write.invoke(batch);
                for(String name : new String[]{"modelMatrixBuffer", "extraDataBuffer"}){
                    var field = Batch.class.getDeclaredField(name);
                    field.setAccessible(true);
                    var buffer = (FloatBuffer)field.get(batch);
                    check(buffer.isDirect(), "Batch upload buffer must be direct");
                    check(buffer.remaining()==(name.equals("modelMatrixBuffer")?16:2), "Incorrect upload size");
                }
            }
        }
    }
    private static void await(CountDownLatch latch){
        try{
            check(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for test release");
        }catch(InterruptedException ex){
            throw new AssertionError(ex);
        }
    }
    private static void waitForJoin(Thread host) throws Exception{
        long deadline = System.nanoTime()+TimeUnit.SECONDS.toNanos(2);
        while(System.nanoTime()<deadline){
            for(var frame : host.getStackTrace()){
                if(frame.getMethodName().equals("awaitUpdateThread"))return;
            }
            Thread.sleep(1);
        }
        throw new AssertionError("Host did not wait for update threads");
    }
    public static void main(String[] args) throws Exception{
        Set<Integer> enabled = new HashSet<>();
        int[] blend = new int[2];
        int[] framebuffer = {7};
        boolean[] failRender = {false};
        RuntimeException renderFailure = new RuntimeException("Expected layer failure");
        GL.setBackend((GLBackend)Proxy.newProxyInstance(GLBackend.class.getClassLoader(),
            new Class<?>[]{GLBackend.class}, (proxy, method, values) -> {
                if(method.getName().equals("glEnable"))enabled.add((Integer)values[0]);
                if(method.getName().equals("glBlendFunc")){
                    blend[0] = (Integer)values[0];
                    blend[1] = (Integer)values[1];
                }
                if(method.getName().equals("glGetShaderiv") || method.getName().equals("glGetProgramiv"))
                    ((int[])values[2])[0] = 1;
                if(method.getName().equals("glGetInteger") && (Integer)values[0]==GL.GL_FRAMEBUFFER_BINDING)
                    return framebuffer[0];
                if(method.getName().equals("glBindFramebuffer"))framebuffer[0] = (Integer)values[1];
                if(method.getReturnType()==int.class)return 1;
                if(method.getReturnType()==boolean.class)return false;
                return null;
            }));
        int[] calls = new int[6];
        DizzyEngine.addLayer(new DizzyLayer(){
            public void init(){
                check(!DizzyEngine.isRunning(), "Startup completed before layer initialization");
                check(enabled.contains(GL.GL_BLEND) && enabled.contains(GL.GL_STENCIL_TEST),
                    "Required GL state missing during layer initialization");
                calls[0]++;
            }
            protected void onScreenSize(Vector2i size){
                check(calls[0]==1, "Size dispatched before initialization");
                check(size.x==640 && size.y==480, "Incorrect initial dimensions");
                calls[1]++;
            }
            protected void onChar(int id, int codepoint){
                check(id==3 && codepoint==0x1f600, "Character event lost its cursor or Unicode codepoint");
                calls[2]++;
            }
            protected void onCharMods(int id, int codepoint, int mods){
                check(id==3 && codepoint==65 && mods==2, "Character modifiers lost");
                calls[3]++;
            }
            protected void onScroll(int id, double xoffset, double yoffset){
                check(id==3 && xoffset==-1.25 && yoffset==2.5, "Scroll event lost its cursor or offsets");
                calls[4]++;
            }
            public void render(double deltaTime){
                check(framebuffer[0]==7, "First layer missed host framebuffer");
                GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 42);
                if(failRender[0])throw renderFailure;
            }
            public void cleanup(){}
        });
        DizzyEngine.addLayer(new DizzyLayer(){
            public void init(){}
            public void render(double deltaTime){
                check(framebuffer[0]==7, "Offscreen work redirected the next layer");
                calls[5]++;
                GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
            }
            public void cleanup(){}
        });
        CountDownLatch updates = new CountDownLatch(2);
        CountDownLatch releaseUpdates = new CountDownLatch(1);
        CountDownLatch updateCleanups = new CountDownLatch(2);
        CountDownLatch releaseCleanups = new CountDownLatch(1);
        Runnable blockedUpdate = () -> {
            updates.countDown();
            await(releaseUpdates);
        };
        Runnable blockedCleanup = () -> {
            updateCleanups.countDown();
            await(releaseCleanups);
        };
        var fixed = DizzyEngine.addFixedUpdateThread("test-fixed", tick -> blockedUpdate.run(), blockedCleanup, 100);
        var unmanaged = DizzyEngine.addUnmanagedUpdateThread("test-unmanaged", tick -> blockedUpdate.run(), blockedCleanup, 1);
        int[] cleanups = new int[1];
        DizzyEngine.addShutdownHook(() -> {
            check(!DizzyEngine.isRunning(), "Cleanup ran before updates stopped");
            check(!fixed.thread.isAlive() && !unmanaged.thread.isAlive(), "Resources released before update threads terminated");
            cleanups[0]++;
        });
        try{
            DizzyEngine.initHosted(640, 480, new Shader("void main(){}", "void main(){}"));
            check(calls[0]==1 && calls[1]==1, "Layer startup events missing or duplicated");
            check(blend[0]==GL.GL_SRC_ALPHA && blend[1]==GL.GL_ONE_MINUS_SRC_ALPHA,
                "Incorrect alpha blending");
            DizzyEngine.hostedChar(3, 0x1f600);
            DizzyEngine.hostedCharMods(3, 65, 2);
            check(calls[2]==1 && calls[3]==1, "Hosted character events missing or duplicated");
            DizzyEngine.hostedScroll(3, -1.25, 2.5);
            check(calls[4]==1, "Hosted scroll event missing or duplicated");
            DizzyEngine.renderHosted(0.016);
            check(calls[5]==1 && framebuffer[0]==7, "Host framebuffer not restored on return");
            failRender[0] = true;
            try{
                DizzyEngine.renderHosted(0.016);
                throw new AssertionError("Layer exception did not propagate");
            }catch(RuntimeException ex){
                check(ex==renderFailure, "Unexpected render failure");
            }
            check(framebuffer[0]==7, "Host framebuffer not restored after layer failure");
            checkBatchBuffers();
            check(updates.await(2, TimeUnit.SECONDS), "Registered update threads did not run");
        }finally{
            Thread host = Thread.currentThread();
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread coordinator = new Thread(() -> {
                try{
                    waitForJoin(host);
                    check(cleanups[0]==0, "Resource hooks ran during an update callback");
                    releaseUpdates.countDown();
                    check(updateCleanups.await(2, TimeUnit.SECONDS), "Update cleanup did not start");
                    waitForJoin(host);
                    check(cleanups[0]==0, "Resource hooks ran during update cleanup");
                }catch(Throwable ex){
                    failure.set(ex);
                }finally{
                    releaseUpdates.countDown();
                    releaseCleanups.countDown();
                }
            }, "shutdown-test-coordinator");
            coordinator.start();
            host.interrupt(); // An interrupted host must still wait before destroying resources.
            DizzyEngine.shutdownHosted();
            check(Thread.interrupted(), "Shutdown lost the host interrupt status");
            coordinator.join(3000);
            check(!coordinator.isAlive(), "Shutdown coordinator did not finish");
            if(failure.get()!=null)throw new AssertionError("Shutdown ordering failed", failure.get());
            DizzyEngine.shutdownHosted();
        }
        check(cleanups[0]==1, "Shutdown hook missing or duplicated");
        check(!fixed.thread.isAlive() && !unmanaged.thread.isAlive(), "Update threads did not stop");
        System.out.println("Hosted startup regression checks passed");
    }
}
