package com.thizthizzydizzy.dizzyengine.graphics.batch;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import com.thizthizzydizzy.dizzyengine.world.object.WorldObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class Batcher implements AutoCloseable{
    public Batcher(){
        DizzyEngine.addShutdownHook(this);
    }
    private final List<Batch> staticBatches = new ArrayList<>();
    private final List<Batch> deadBatches = new ArrayList<>();
    public List<Batch> batch(List<WorldObject> objects){
        // Remove any static batches for dirty objects in the list to be rendered
        for(var object : objects){
            if(!object.isStatic())continue;
            if(object.isDirty()){
                for(var batch : staticBatches){
                    if(batch.matches(object)){
                        batch.dirty = true;
                        break;
                    }
                }
            }
        }

        // Remove any dirty static batches, and any that contain dirty objects
        for(Iterator<Batch> it = staticBatches.iterator(); it.hasNext();){
            Batch batch = it.next();
            for(var obj : batch.objects){
                if(batch.dirty |= obj.isDirty())break;
            }
            if(batch.dirty){
                deadBatches.add(batch);
                it.remove();
            }
        }
        
        for(Iterator<Batch> it = deadBatches.iterator(); it.hasNext();){
            Batch batch = it.next();
            try{
                batch.close();
                it.remove();
            }catch(Exception ex){
                Logger.error("Could not clean up Batch!", ex);
            }
        }

        List<Batch> newBatches = new ArrayList<>();

        // Create batches for all objects
        for(var object : objects){
            if(object.isStatic()){
                Batch existingBatch = null;
                for(var batch : staticBatches){
                    if(batch.matches(object)){
                        existingBatch = batch;
                        break;
                    }
                }
                if(existingBatch!=null)continue; // This is part of a static batch that has not changed.
            }

            Batch targetBatch = null;
            for(var batch : newBatches){
                if(batch.matches(object)){
                    targetBatch = batch;
                    break;
                }
            }
            if(targetBatch==null){
                BatchType type = object.isStatic()?BatchType.STATIC_INSTANCED:BatchType.DYNAMIC_INSTANCED;
                if(object.getMaterial()==null)type = BatchType.INDIVIDUAL;
                targetBatch = new Batch(type, object.getMaterial());
                newBatches.add(targetBatch);
            }
            targetBatch.objects.add(object);
            object.unmarkDirty();
        }

        var allBatches = new ArrayList<>(staticBatches);
        allBatches.addAll(newBatches);
        allBatches.sort((a, b) -> a.type.ordinal()-b.type.ordinal());

        for(var batch : newBatches){
            if(batch.type==BatchType.STATIC_INSTANCED)staticBatches.add(batch);
            else deadBatches.add(batch);
        }

        return allBatches;
    }
    @Override
    public void close() throws Exception{
        for(var batch : staticBatches){
            batch.close();
        }
        for(var batch : deadBatches){
            batch.close();
        }
    }
}
