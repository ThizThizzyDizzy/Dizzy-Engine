package com.thizthizzydizzy.dizzyengine.terminal;
import java.util.ArrayList;
import java.util.List;
public class FlagArguments{
    public final String flag;
    public final List<String> arguments = new ArrayList<>();
    private final int numArguments;
    public FlagArguments(String flag, int numArguments){
        this.flag = flag;
        this.numArguments = numArguments;
    }
    public boolean full(){
        return numArguments == -1 || arguments.size()>=numArguments;
    }
}
