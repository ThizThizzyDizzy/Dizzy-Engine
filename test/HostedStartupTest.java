import com.thizthizzydizzy.dizzyengine.*;
import com.thizthizzydizzy.dizzyengine.platform.GL;
import com.thizthizzydizzy.dizzyengine.platform.GLBackend;
import java.lang.reflect.Proxy;
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
        int[] calls = new int[2];
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
        try{
            DizzyEngine.initHosted(640, 480, null);
            check(calls[0]==1 && calls[1]==1, "Layer startup events missing or duplicated");
            check(blend[0]==GL.GL_SRC_ALPHA && blend[1]==GL.GL_ONE_MINUS_SRC_ALPHA,
                "Incorrect alpha blending");
            check(updates.await(2, TimeUnit.SECONDS), "Registered update threads did not run");
        }finally{
            DizzyEngine.stop();
            fixed.thread.join(2000);
            unmanaged.thread.join(2000);
        }
        check(!fixed.thread.isAlive() && !unmanaged.thread.isAlive(), "Update threads did not stop");
        System.out.println("Hosted startup regression checks passed");
    }
}
