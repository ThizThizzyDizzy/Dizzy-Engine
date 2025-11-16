package com.thizthizzydizzy.dizzyengine.terminal;
import java.util.HashMap;
import java.util.function.Consumer;
public abstract class Terminal{
    private HashMap<String, TerminalCommand> commands = new HashMap<>();
    public Terminal(){
        registerCommands();
    }
    public void run(Consumer<String> output, String command){
        run(output, TerminalUtils.parseArgs(command).toArray(String[]::new));
    }
    public void run(Consumer<String> output, String[] command){
        if(command.length==0)return;
        var cmd = commands.get(command[0]);
        if(cmd==null){
            output.accept(getShellName()+": "+command[0]+" command not found");
            return;
        }
        cmd.run(output, TerminalUtils.trimOne(command));
    }
    public abstract String getShellName();
    protected abstract void registerCommands();
    public void registerCommand(TerminalCommand command){
        String cmd = command.getBaseCommand();
        if(commands.containsKey(cmd))
            throw new IllegalArgumentException("Command already registered: "+cmd);
        commands.put(cmd, command);
    }
}
