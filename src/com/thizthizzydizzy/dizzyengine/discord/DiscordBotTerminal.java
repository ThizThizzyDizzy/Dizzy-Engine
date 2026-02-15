package com.thizthizzydizzy.dizzyengine.discord;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import com.thizthizzydizzy.dizzyengine.terminal.DizzyEngineTerminal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBotTerminal extends ListenerAdapter{
    private JDA jda;
    private final String channelId;
    private final DizzyEngineTerminal terminal;
    private static final int MAX_MESSAGE_LENGTH = 2000;
    public DiscordBotTerminal(String token, String channelId, DizzyEngineTerminal terminal){
        this.channelId = channelId;
        this.terminal = terminal;
        try{
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();
            jda.awaitReady();
            Logger.info("Discord bot initialized successfully");
        }catch(InterruptedException ex){
            Logger.error("Interrupted while waiting for Discord bot to initialize!", ex);
            Thread.currentThread().interrupt();
        }catch(Exception ex){
            Logger.error("Failed to initialize Discord bot!", ex);
        }
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot()){
            return;
        }
        MessageChannel channel = event.getChannel();
        if(!channel.getId().equals(channelId)){
            return;
        }
        String messageContent = event.getMessage().getContentRaw();
        if(messageContent.isEmpty()){
            return;
        }
        DiscordOutputConsumer outputConsumer = new DiscordOutputConsumer(channel);
        terminal.run(outputConsumer, messageContent);
        outputConsumer.sendRemaining();
    }
    public void shutdown(){
        if(jda!=null){
            jda.shutdown();
            try{
                if(!jda.awaitShutdown(5, TimeUnit.SECONDS)){
                    jda.shutdownNow();
                }
            }catch(InterruptedException ex){
                Logger.error("Interrupted while shutting down Discord bot!", ex);
                Thread.currentThread().interrupt();
            }
        }
    }
    private static class DiscordOutputConsumer implements Consumer<String>{
        private final MessageChannel channel;
        private final List<String> buffer = new ArrayList<>();
        private static final int MAX_BUFFER_SIZE = 1900;
        public DiscordOutputConsumer(MessageChannel channel){
            this.channel = channel;
        }
        @Override
        public void accept(String line){
            buffer.add(line);
            flushIfNeeded();
        }
        public void sendRemaining(){
            if(!buffer.isEmpty()){
                flushBuffer();
            }
        }
        private void flushIfNeeded(){
            int totalLength = buffer.stream().mapToInt(String::length).sum();
            totalLength += buffer.size() - 1;
            if(totalLength >= MAX_BUFFER_SIZE){
                flushBuffer();
            }
        }
        private void flushBuffer(){
            StringBuilder messageBuilder = new StringBuilder();
            List<String> sentLines = new ArrayList<>();
            for(String line : buffer){
                if(messageBuilder.length() + line.length() + 1 > MAX_BUFFER_SIZE){
                    if(messageBuilder.length() > 0){
                        channel.sendMessage(messageBuilder.toString()).queue();
                        messageBuilder.setLength(0);
                    }
                    if(line.length() > MAX_MESSAGE_LENGTH){
                        List<String> chunks = splitLongMessage(line);
                        for(String chunk : chunks){
                            channel.sendMessage(chunk).queue();
                        }
                    }else{
                        messageBuilder.append(line).append("\n");
                    }
                }else{
                    messageBuilder.append(line).append("\n");
                }
                sentLines.add(line);
            }
            if(messageBuilder.length() > 0){
                String message = messageBuilder.toString().trim();
                if(!message.isEmpty()){
                    channel.sendMessage(message).queue();
                }
            }
            buffer.removeAll(sentLines);
        }
        private List<String> splitLongMessage(String message){
            List<String> chunks = new ArrayList<>();
            int start = 0;
            while(start < message.length()){
                int end = Math.min(start + MAX_MESSAGE_LENGTH, message.length());
                if(end < message.length()){
                    int lastNewline = message.lastIndexOf('\n', end);
                    if(lastNewline > start){
                        end = lastNewline;
                    }
                }
                chunks.add(message.substring(start, end));
                start = end;
            }
            return chunks;
        }
    }
}
