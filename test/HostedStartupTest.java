import com.thizthizzydizzy.dizzyengine.*;
import com.thizthizzydizzy.dizzyengine.platform.GL;
import com.thizthizzydizzy.dizzyengine.platform.GLBackend;
import java.lang.reflect.Proxy;
import com.thizthizzydizzy.dizzyengine.graphics.batch.*;
import com.thizthizzydizzy.dizzyengine.graphics.mesh.Mesh;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
    public static void main(String[] args) throws Exception{
        Set<Integer> enabled = new HashSet<>();
        int[] blend = new int[2];
        GL.setBackend((GLBackend)Proxy.newProxyInstance(GLBackend.class.getClassLoader(),
            new Class<?>[]{GLBackend.class}, (proxy, method, values) -> {
                if(method.getName().equals("glEnable"))enabled.add((Integer)values[0]);
                if(method.getName().equals("glBlendFunc")){
                    blend[0] = (Integer)values[0];
                    blend[1] = (Integer)values[1];
                }
                if(method.getReturnType()==int.class)return 1;
                if(method.getReturnType()==boolean.class)return false;
                return null;
            }));
        int[] calls = new int[4];
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
            public void render(double deltaTime){}
            public void cleanup(){}
        });
        CountDownLatch updates = new CountDownLatch(2);
        var fixed = DizzyEngine.addFixedUpdateThread("test-fixed", tick -> {
            if(tick==0)updates.countDown();
        }, null, 100);
        var unmanaged = DizzyEngine.addUnmanagedUpdateThread("test-unmanaged", tick -> {
            if(tick==0)updates.countDown();
        }, null, 1);
        int[] cleanups = new int[1];
        DizzyEngine.addShutdownHook(() -> {
            check(!DizzyEngine.isRunning(), "Cleanup ran before updates stopped");
            cleanups[0]++;
        });
        try{
            DizzyEngine.initHosted(640, 480, null);
            check(calls[0]==1 && calls[1]==1, "Layer startup events missing or duplicated");
            check(blend[0]==GL.GL_SRC_ALPHA && blend[1]==GL.GL_ONE_MINUS_SRC_ALPHA,
                "Incorrect alpha blending");
            DizzyEngine.hostedChar(3, 0x1f600);
            DizzyEngine.hostedCharMods(3, 65, 2);
            check(calls[2]==1 && calls[3]==1, "Hosted character events missing or duplicated");
            checkBatchBuffers();
            check(updates.await(2, TimeUnit.SECONDS), "Registered update threads did not run");
        }finally{
            DizzyEngine.shutdownHosted();
            DizzyEngine.shutdownHosted();
            fixed.thread.join(2000);
            unmanaged.thread.join(2000);
        }
        check(cleanups[0]==1, "Shutdown hook missing or duplicated");
        check(!fixed.thread.isAlive() && !unmanaged.thread.isAlive(), "Update threads did not stop");
        System.out.println("Hosted startup regression checks passed");
    }
}
