package com.thizthizzydizzy.dizzyengine.terminal;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.util.ArrayList;
import java.util.function.Consumer;
public class DizzyEngineTerminal extends Terminal{
    @Override
    public String getShellName(){
        return "dizzyengine";
    }
    @Override
    protected void registerCommands(){
        registerCommand(new TerminalCommand() {
            @Override
            public String getBaseCommand(){
                return "exit";
            }
            @Override
            public void registerArguments(){}
            @Override
            protected void run(Consumer<String> output, ArrayList<FlagArguments> commandArguments){
                output.accept("Stopping DizzyEngine");
                DizzyEngine.stop();
            }
        });
        registerCommand(new TerminalCommand() {
            @Override
            public String getBaseCommand(){
                return "debug";
            }
            @Override
            public void registerArguments(){}
            @Override
            protected void run(Consumer<String> output, ArrayList<FlagArguments> commandArguments){
                Logger.enableDebugLogging = !Logger.enableDebugLogging;
                output.accept("Debug logging "+(Logger.enableDebugLogging?"enabled":"disabled"));
            }
        });
    }
}
