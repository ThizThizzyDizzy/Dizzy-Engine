package com.thizthizzydizzy.dizzyengine.terminal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
public abstract class TerminalCommand{
    private HashMap<String, Integer> flagArguments = new HashMap();
    private HashMap<Character, String> quickFlags = new HashMap<>();
    public TerminalCommand(){
        registerArguments();
    }
    public abstract String getBaseCommand();
    public abstract void registerArguments();
    public void registerArgument(Character flag, String name, int numArguments){
        flagArguments.put(name, numArguments);
        if(flag!=null)quickFlags.put(flag, name);
    }
    public void run(Consumer<String> output, String... args){
        ArrayList<FlagArguments> commandArguments = new ArrayList<>();
        
        String currentFlag = null;
        FlagArguments currentFlagArguments = null;
        int currentArg = 0;
        for(int i = 0; i<args.length; i++){
            if(currentFlag!=null){
                int numArgs = flagArguments.get(currentFlag);
                if(currentFlagArguments==null){
                    currentFlagArguments = new FlagArguments(currentFlag, numArgs);
                    currentArg = 0;
                }
                if(currentArg>=numArgs){
                    commandArguments.add(currentFlagArguments);
                    currentFlagArguments = null;
                }else{
                    currentFlagArguments.arguments[currentArg++] = args[i];
                    continue;
                }
            }
            if(args[i].startsWith("--")){
                currentFlag = args[i].substring(2);
            }else if(args[i].startsWith("-")){
                for(int j = 1; j<args[i].length(); j++){
                    if(currentFlag!=null){
                        output.accept(getBaseCommand()+": flag --"+currentFlag+" requires arguments!");
                        return;
                    }
                    char quickFlag = args[i].charAt(j);
                    if(!quickFlags.containsKey(quickFlag)){
                        output.accept(getBaseCommand()+": unknown flag: -"+quickFlag);
                        return;
                    }
                    String flag = quickFlags.get(quickFlag);
                    if(flagArguments.get(flag)>0){
                        currentFlag = flag;
                    }
                }
            }
            if(!flagArguments.containsKey(currentFlag)){
                output.accept(getBaseCommand()+": unknown flag: --"+currentFlag);
                return;
            }
        }
        if(currentFlagArguments!=null)commandArguments.add(currentFlagArguments);
        run(output, commandArguments);
    }
    protected abstract void run(Consumer<String> output, ArrayList<FlagArguments> commandArguments);
}
