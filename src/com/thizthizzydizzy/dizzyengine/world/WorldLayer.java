package com.thizthizzydizzy.dizzyengine.world;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import org.joml.Vector3f;
import org.joml.Vector3i;
public abstract class WorldLayer extends DizzyLayer{
    public float panX;
    public float panY;
    public float zoom = 1;
    private final Vector3f chunkSize;
    /**
     * WorldLayer is a layer made for any world (2D or 3D) with support for 2D
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
    public WorldLayer(){
        this.chunkSize = null;
    }
    @Override
    public void init(){
    }
    @Override
    public void render(double deltaTime){
        Renderer.translate(panX, panY, zoom, zoom);
        if(chunkSize==null){
            renderWorld(null, deltaTime);
        }else
            throw new UnsupportedOperationException("Chunk-based World Layers are not yet implemented!");
        Renderer.unTranslate();
    }
    public abstract void renderWorld(Vector3i chunk, double deltaTime);
    @Override
    public void cleanup(){
    }
}
