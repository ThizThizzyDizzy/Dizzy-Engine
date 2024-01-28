package com.thizthizzydizzy.dizzyengine.discord;
import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
public class DiscordPresence{
    private static final DiscordEventHandlers handlers = new DiscordEventHandlers();
    private static final DiscordRichPresence richPresence = new DiscordRichPresence();
    private static boolean updateNeeded = true;
    public static void init(String discordAppID){
        DiscordRPC.INSTANCE.Discord_Initialize(discordAppID, handlers, true, null);
        richPresence.startTimestamp = System.currentTimeMillis()/1000;
        DizzyEngine.addFixedUpdateThread("Discord RPC Thread", (updateCounter) -> {
            if(updateNeeded){
                DiscordRPC.INSTANCE.Discord_UpdatePresence(richPresence);
                updateNeeded = false;
            }
            DiscordRPC.INSTANCE.Discord_RunCallbacks();
        }, ()->{
            DiscordRPC.INSTANCE.Discord_Shutdown();
        }, 10);
    }
    public static void setState(String state){
        richPresence.state = state==null?"":state;
        updateNeeded = true;
    }
    public static void setDetails(String details){
        richPresence.details = details==null?"":details;
        updateNeeded = true;
    }
    public static void setLargeImage(String key, String text){
        richPresence.largeImageKey = key==null?"":key;
        richPresence.largeImageText = text==null?"":text;
        updateNeeded = true;
    }
    public static void setSmallImage(String key, String text){
        richPresence.smallImageKey = key==null?"":key;
        richPresence.smallImageText = text==null?"":text;
        updateNeeded = true;
    }
    public static void setTimestamps(long start, long end){
        richPresence.startTimestamp = start;
        richPresence.endTimestamp = end;
        updateNeeded = true;
    }
    public static void setEndTimestamp(long end){
        richPresence.endTimestamp = end;
        updateNeeded = true;
    }
}
