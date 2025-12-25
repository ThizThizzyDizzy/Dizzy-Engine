package com.thizthizzydizzy.dizzyengine.terminal;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
public abstract class TerminalCommand{
    private HashMap<String, Integer> flagArguments = new HashMap();
    private HashMap<Character, String> quickFlags = new HashMap<>();
    {
        flagArguments.put(null, -1);
    }
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

        FlagArguments baseCommandArguments = new FlagArguments(null, flagArguments.get(null));
        FlagArguments currentFlagArguments = null;
        for(int i = 0; i<args.length; i++){
            if(currentFlagArguments!=null){
                if(currentFlagArguments.full()){
                    commandArguments.add(currentFlagArguments);
                    currentFlagArguments = null;
                }else{
                    currentFlagArguments.arguments.add(args[i]);
                    continue;
                }
            }
            if(args[i].startsWith("--")){
                var flag = args[i].substring(2);
                if(!flagArguments.containsKey(flag)){
                    output.accept(getBaseCommand()+": unknown flag: --"+flag);
                    return;
                }
                currentFlagArguments = new FlagArguments(flag, flagArguments.get(flag));
            }else if(args[i].startsWith("-")){
                for(int j = 1; j<args[i].length(); j++){
                    if(currentFlagArguments!=null){
                        output.accept(getBaseCommand()+": flag --"+currentFlagArguments.flag+" requires arguments!");
                        return;
                    }
                    char quickFlag = args[i].charAt(j);
                    if(!quickFlags.containsKey(quickFlag)){
                        output.accept(getBaseCommand()+": unknown flag: -"+quickFlag);
                        return;
                    }
                    String flag = quickFlags.get(quickFlag);
                    int flagArgs = flagArguments.get(flag);
                    if(flagArgs!=0){
                        currentFlagArguments = new FlagArguments(flag, flagArgs);
                    }else
                        commandArguments.add(new FlagArguments(flag, 0));
                }
            }
            if(currentFlagArguments==null){
                baseCommandArguments.arguments.add(args[i]);
            }
        }
        if(currentFlagArguments!=null)
            commandArguments.add(currentFlagArguments);
        if(flagArguments.get(null)!=0)
            commandArguments.add(0, baseCommandArguments);
        try{
            run(output, commandArguments);
        }catch(Exception ex){
            Logger.error("DizzyEngine Terminal - Exception running command: "+getBaseCommand(), ex);
        }
    }
    protected abstract void run(Consumer<String> output, ArrayList<FlagArguments> flagArguments) throws Exception;
}
