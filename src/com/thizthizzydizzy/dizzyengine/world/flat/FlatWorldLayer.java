package com.thizthizzydizzy.dizzyengine.world.flat;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import com.thizthizzydizzy.dizzyengine.graphics.batch.Batcher;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import static org.lwjgl.opengl.GL11.*;
public abstract class FlatWorldLayer extends DizzyLayer{
    public float panX;
    public float panY;
    public Vector2f screenSnap = new Vector2f();
    public float zoom = 1;
    private final Vector3f chunkSize;

    private final Batcher batcher = new Batcher();

    private final List<WorldObject> objects = new ArrayList();
    private final HashMap<Class<? extends WorldObject>, List<? extends WorldObject>> indicies = new HashMap<>();

    /**
     * FlatWorldLayer is a layer made for any flat world (2D/2.5D) with support
     * for 2D
     * pan/zoom.
     */
    //TODO add support for chunks
//     * chunks and pan/zoom.
//     * @param chunkSize The size of each chunk. Set any axis to negative to
//     * disable chunking in that axis. (If null, the entire world will be one
//     * chunk)
//    */
//    public WorldLayer(Vector3f chunkSize){
//        this.chunkSize = chunkSize;
//    }
    public FlatWorldLayer(){
        this.chunkSize = null;
    }

    public List<WorldObject> getObjects(){
        return Collections.unmodifiableList(objects);
    }
    public <T extends WorldObject> List<T> createObjectIndex(Class<T> type){
        indicies.put(type, new ArrayList<T>());
        return getObjectIndex(type);
    }
    public <T extends WorldObject> List<T> getObjectIndex(Class<T> type){
        return Collections.unmodifiableList((List<T>)indicies.get(type));
    }

    public <T extends WorldObject> void addObject(T object){
        objects.add(object);
        for(var indexKey : indicies.keySet()){
            if(indexKey.isAssignableFrom(object.getClass())){
                List list = indicies.get(indexKey);
                list.add(object);
            }
        }
    }

    @Override
    public void init(){
    }
    @Override
    public void cleanup(){
    }
    public Matrix4f createProjectionMatrix(){
        float xOff = -(screenSnap.x+1)/2;
        float yOff = -(screenSnap.y+1)/2;
        return new Matrix4f().setOrtho(DizzyEngine.screenSize.x*xOff, DizzyEngine.screenSize.x*(xOff+1), DizzyEngine.screenSize.y*(yOff+1), DizzyEngine.screenSize.y*yOff, -10000f, 10000f);
    }
    @Override
    public void render(double deltaTime){
//        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        var projection = createProjectionMatrix();
        Renderer.projection(projection);
        var view = new Matrix4f();
        view.scale(zoom, zoom, zoom);
        view.translate(panX, panY, 0);
        Renderer.view(view);
        Renderer.setModel(new Matrix4f());
        if(chunkSize==null){
            renderWorld(null, deltaTime);
        }else
            throw new UnsupportedOperationException("Chunk-based World Layers are not yet implemented!");
    }
    public abstract void renderWorld(Vector3i chunk, double deltaTime);

    protected void drawObjects(){
        for(var batch : batcher.batch(getObjects()))batch.render();
    }
}
