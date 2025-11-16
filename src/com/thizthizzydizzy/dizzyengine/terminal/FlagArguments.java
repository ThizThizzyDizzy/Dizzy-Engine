package com.thizthizzydizzy.dizzyengine.terminal;
public class FlagArguments{
    public final String flag;
    public final String[] arguments;
    public FlagArguments(String flag, int numArguments){
        this.flag = flag;
        arguments = new String[numArguments];
    }
}
