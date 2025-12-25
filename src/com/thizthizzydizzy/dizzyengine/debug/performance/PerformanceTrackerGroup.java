package com.thizthizzydizzy.dizzyengine.debug.performance;
import java.util.HashMap;
import java.util.Map;
public class PerformanceTrackerGroup{
    public final PerformanceTrackerGroup parent;
    public PerformanceTrackerGroup(PerformanceTrackerGroup parent){
        this.parent = parent;
    }
    public final Map<String, PerformanceTrackerGroup> subgroups = new HashMap<>();
    public final Map<String, Integer> counters = new HashMap();
    public void reset(){
        counters.clear();
        subgroups.values().forEach(PerformanceTrackerGroup::reset);
    }
    public PerformanceTrackerGroup subgroup(String source){
        var subgroup = subgroups.get(source);
        if(subgroup==null){
            subgroup = new PerformanceTrackerGroup(this);
            subgroups.put(source, subgroup);
        }
        subgroup.addRawCounter("hits", 1);
        return subgroup;
    }
    public void addCounter(String counter, int i){
        addRawCounter(counter, i);
        if(parent!=null)parent.addCounter(counter, i);
    }
    private void addRawCounter(String counter, int i){
        synchronized(counters){
            counters.put(counter, counters.getOrDefault(counter, 0)+i);
        }
    }
}
