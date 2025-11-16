package com.thizthizzydizzy.dizzyengine.terminal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TerminalUtils{
    public static String[] trimOne(String[] args){
        if(args.length==0)return args;
        String[] newArgs = new String[args.length-1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }
    public static List<String> parseArgs(String input){
        List<String> args = new ArrayList<>();
        String regex = "\"(?:\\\\.|[^\"\\\\])*\"|[^\\s]+";
        Matcher matcher = Pattern.compile(regex).matcher(input);
        while(matcher.find()){
            String arg = matcher.group();
            if(arg.startsWith("\"")&&arg.endsWith("\"")){
                arg = arg.substring(1, arg.length()-1).replace("\\\"", "\"").replace("\\\\", "\\");
            }
            args.add(arg);
        }
        return args;
    }
}
