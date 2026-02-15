package com.thizthizzydizzy.dizzyengine.discord;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import com.thizthizzydizzy.dizzyengine.terminal.DizzyEngineTerminal;
import com.thizthizzydizzy.dizzyengine.terminal.TaskOutputConsumer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBotTerminal extends ListenerAdapter{
    private JDA jda;
    private final String channelId;
    private final DizzyEngineTerminal terminal;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService commandExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Discord-Command-Handler");
        t.setDaemon(true);
        return t;
    });
    private Message currentTaskMessage = null;
    private ScheduledFuture<?> pendingUpdate = null;
    private TaskOutputConsumer currentTask = null;
    private MessageChannel currentChannel = null;
    private static final long UPDATE_DELAY_MS = 1000;
    private static final int MAX_EMBED_CHARS = 5900;
    private static final int MAX_FIELD_VALUE = 1000;
    private static final int MAX_LOG_CHARS = 1500;
    
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
        
        // Run terminal handling off the JDA event thread
        commandExecutor.submit(() -> {
            currentChannel = channel;
            DiscordTaskOutputConsumer taskConsumer = new DiscordTaskOutputConsumer(channel);
            terminal.run(taskConsumer, messageContent);
            
            // Flush any remaining non-task messages
            taskConsumer.flushRemaining();
            
            if(!taskConsumer.isTaskActive() && currentTaskMessage != null){
                finalizeTaskDisplay();
            }
            
            currentTask = null;
            currentTaskMessage = null;
            currentChannel = null;
        });
    }
    
    private void beginTaskDisplay(TaskOutputConsumer task){
        currentTask = task;
        if(currentChannel != null && jda != null){
            try{
                MessageEmbed embed = buildTaskEmbed(task);
                currentTaskMessage = currentChannel.sendMessageEmbeds(embed).complete();
            }catch(Exception ex){
                Logger.error("Failed to create task embed!", ex);
            }
        }
    }
    
    private void scheduleEmbedUpdate(){
        if(pendingUpdate != null && !pendingUpdate.isDone()){
            pendingUpdate.cancel(false);
        }
        pendingUpdate = scheduler.schedule(() -> {
            updateTaskEmbed();
        }, UPDATE_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    
    private void updateTaskEmbed(){
        if(currentTask == null || currentTaskMessage == null || jda == null){
            return;
        }
        try{
            MessageEmbed embed = buildTaskEmbed(currentTask);
            currentTaskMessage.editMessageEmbeds(embed).queue();
        }catch(Exception ex){
            Logger.error("Failed to update Discord embed!", ex);
        }
    }
    
    private void finalizeTaskDisplay(){
        if(currentTaskMessage != null && currentTask != null){
            try{
                MessageEmbed embed = buildTaskEmbed(currentTask);
                currentTaskMessage.editMessageEmbeds(embed).complete();
            }catch(Exception ex){
                Logger.error("Failed to finalize task embed!", ex);
            }
        }
    }
    
    private MessageEmbed buildTaskEmbed(TaskOutputConsumer task){
        EmbedBuilder builder = new EmbedBuilder();
        String title = task.getTaskName();
        if(title == null) title = "Task";
        if(task.getCurrentStatus() != null && !task.getCurrentStatus().isEmpty()){
            title += " - " + task.getCurrentStatus();
        }
        if(title.length() > 256){
            title = title.substring(0, 253) + "...";
        }
        builder.setTitle(title);
        
        int usedChars = title.length();
        
        if(task.getTaskDescription() != null && !task.getTaskDescription().isEmpty()){
            String desc = task.getTaskDescription();
            if(desc.length() > 4096){
                desc = desc.substring(0, 4093) + "...";
            }
            builder.setDescription(desc);
            usedChars += desc.length();
        }
        
        Map<String, String> metadata = task.getMetadata();
        for(Map.Entry<String, String> entry : metadata.entrySet()){
            if(builder.getFields().size() >= 25) break;
            
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            
            if(fieldName.length() > 256){
                fieldName = fieldName.substring(0, 253) + "...";
            }
            if(fieldValue.length() > MAX_FIELD_VALUE){
                fieldValue = fieldValue.substring(0, MAX_FIELD_VALUE - 3) + "...";
            }
            
            int fieldChars = fieldName.length() + fieldValue.length();
            if(usedChars + fieldChars > MAX_EMBED_CHARS - MAX_LOG_CHARS - 100){
                break;
            }
            
            builder.addField(fieldName, fieldValue, false);
            usedChars += fieldChars;
        }
        
        Deque<String> logs = task.getLogBuffer();
        if(!logs.isEmpty()){
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("```\n");
            for(String line : logs){
                // Leave room for closing ``` and line breaks (8 chars: ```\n + \n```)
                int maxContentLength = MAX_FIELD_VALUE - 8;
                if(logBuilder.length() + line.length() + 1 > maxContentLength){
                    break;
                }
                logBuilder.append(line).append("\n");
            }
            logBuilder.append("```");
            builder.addField("Recent Logs", logBuilder.toString(), false);
        }
        
        Color color;
        if(task.isTaskActive()){
            color = Color.YELLOW;
        }else if(task.isTaskSuccess()){
            color = Color.GREEN;
        }else{
            color = Color.RED;
        }
        builder.setColor(color);
        
        return builder.build();
    }
    
    public void shutdown(){
        if(pendingUpdate != null){
            pendingUpdate.cancel(false);
        }
        scheduler.shutdown();
        commandExecutor.shutdown();
        if(jda != null){
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
    
    private class DiscordTaskOutputConsumer extends TaskOutputConsumer{
        private final MessageChannel channel;
        private final DiscordOutputConsumer outputConsumer;
        private boolean taskStarted = false;
        
        public DiscordTaskOutputConsumer(MessageChannel channel){
            super(new DiscordOutputConsumer(channel));
            this.channel = channel;
            this.outputConsumer = (DiscordOutputConsumer) getDelegate();
        }
        
        public void flushRemaining(){
            if(outputConsumer != null){
                outputConsumer.flushBuffer();
            }
        }
        
        @Override
        public void beginTask(String name, String description){
            super.beginTask(name, description);
            taskStarted = true;
            beginTaskDisplay(this);
        }
        
        @Override
        public void accept(String line){
            super.accept(line);
            if(isTaskActive() && taskStarted){
                scheduleEmbedUpdate();
            }
        }
        
        @Override
        public void updateStatus(String status){
            super.updateStatus(status);
            if(isTaskActive() && taskStarted){
                scheduleEmbedUpdate();
            }
        }
        
        @Override
        public void setMetadata(String key, String value){
            super.setMetadata(key, value);
            if(isTaskActive() && taskStarted){
                scheduleEmbedUpdate();
            }
        }
        
        @Override
        public void endTask(boolean success, String finalMessage){
            // First, cancel any pending scheduled embed update
            if(pendingUpdate != null && !pendingUpdate.isDone()){
                pendingUpdate.cancel(false);
            }
            
            // Flush any remaining output
            if(outputConsumer != null){
                outputConsumer.flushBuffer();
            }
            
            // Update the task state (this marks task as inactive)
            super.endTask(success, finalMessage);
            
            // Send final embed update and wait for it to complete
            if(currentTaskMessage != null && jda != null){
                try{
                    MessageEmbed embed = buildTaskEmbed(this);
                    // Use complete() to block until Discord confirms the edit
                    currentTaskMessage.editMessageEmbeds(embed).complete();
                }catch(Exception ex){
                    Logger.error("Failed to finalize task embed in endTask!", ex);
                }
            }
        }
    }
    
    private class DiscordOutputConsumer implements Consumer<String>{
        private final MessageChannel channel;
        private final List<String> messageBuffer = new ArrayList<>();
        private static final int MAX_BUFFER_SIZE = 1900;
        private ScheduledFuture<?> pendingSend = null;
        
        public DiscordOutputConsumer(MessageChannel channel){
            this.channel = channel;
        }
        
        @Override
        public void accept(String line){
            if(line == null || line.isEmpty()){
                return;
            }
            // Don't send to Discord channel if there's an active task (logs go to embed only)
            if(currentTask != null && currentTask.isTaskActive()){
                return;
            }
            messageBuffer.add(line);
            scheduleSend();
        }
        
        private void scheduleSend(){
            if(pendingSend != null && !pendingSend.isDone()){
                pendingSend.cancel(false);
            }
            pendingSend = scheduler.schedule(() -> {
                flushBuffer();
            }, 500, TimeUnit.MILLISECONDS);
        }
        
        public void flushBuffer(){
            if(messageBuffer.isEmpty()){
                return;
            }
            
            StringBuilder messageBuilder = new StringBuilder();
            List<String> sentLines = new ArrayList<>();
            
            for(String line : messageBuffer){
                if(messageBuilder.length() + line.length() + 1 > 2000){
                    if(messageBuilder.length() > 0){
                        String msg = messageBuilder.toString().trim();
                        if(!msg.isEmpty()){
                            channel.sendMessage(msg).queue();
                        }
                        messageBuilder.setLength(0);
                    }
                    if(line.length() > 2000){
                        List<String> chunks = splitMessage(line);
                        for(String chunk : chunks){
                            if(!chunk.isEmpty()){
                                channel.sendMessage(chunk).queue();
                            }
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
                String msg = messageBuilder.toString().trim();
                if(!msg.isEmpty()){
                    channel.sendMessage(msg).queue();
                }
            }
            
            messageBuffer.removeAll(sentLines);
        }
        
        private void sendMessage(String message){
            if(message == null || message.isEmpty()){
                return;
            }
            if(message.length() <= 2000){
                channel.sendMessage(message).queue();
            }else{
                List<String> chunks = splitMessage(message);
                for(String chunk : chunks){
                    if(!chunk.isEmpty()){
                        channel.sendMessage(chunk).queue();
                    }
                }
            }
        }
        
        private List<String> splitMessage(String message){
            List<String> chunks = new ArrayList<>();
            int start = 0;
            while(start < message.length()){
                int end = Math.min(start + 2000, message.length());
                if(end < message.length()){
                    int lastNewline = message.lastIndexOf('\n', end);
                    if(lastNewline > start){
                        end = lastNewline;
                    }
                }
                String chunk = message.substring(start, end);
                if(!chunk.isEmpty()){
                    chunks.add(chunk);
                }
                start = end;
            }
            return chunks;
        }
    }
}
