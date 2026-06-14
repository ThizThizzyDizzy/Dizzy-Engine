package com.thizthizzydizzy.dizzyengine.updater;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;
public class DizzyUpdater{
    public String currentVersion;
    public String fileRegex;
    public boolean hasNewVersion;
    public String newVersionString;
    private String newVersionDownloadURL;
    public String newVersionFileName;
    public DizzyUpdater(String currentVersion){
        this.currentVersion = currentVersion;
    }
    public DizzyUpdater(Class<?> packageReference){
        Logger.push(this);
        Logger.info("Initializing...");
        HashMap<String, String> keys = new HashMap<>();
        String packageName = packageReference.getPackageName();
        String lastPackageName;
        do{
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(packageReference.getResourceAsStream("/"+packageName.replace('.', '/')+"/version.properties")))){
                Logger.info("Found version.properties "+(packageName.isEmpty()?"at root":"under "+packageName));
                String line;
                while((line = reader.readLine())!=null){
                    String[] parts = line.split("=", 2);
                    keys.put(parts[0], parts[1]);
                }
                break;
            }catch(Exception ex){
            }
            lastPackageName = packageName;
            packageName = packageName.contains(".")?packageName.substring(0, packageName.lastIndexOf('.')):"";
        }while(!lastPackageName.isEmpty());
        currentVersion = keys.get("version");
        if(currentVersion==null){
            Logger.error("Could not find version in version.properties!");
            return;
        }
        Logger.info("Current version: "+currentVersion);
        fileRegex = keys.getOrDefault("file_regex", ".jar");
        Logger.pop();
    }
    public DizzyUpdater checkGitHubLatest(String repoOwner, String repoName, String tagPrefix){
        Logger.push(this);
        String url = "https://api.github.com/repos/"+repoOwner+"/"+repoName+"/releases/latest";
        Logger.info("Fetching latest version from "+url);
        HttpURLConnection conn;
        try{
            conn = (HttpURLConnection)new URI(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            if(conn.getResponseCode()==200){
                try(var reader = new InputStreamReader(conn.getInputStream())){
                    JsonObject releaseJson = JsonParser.parseReader(reader).getAsJsonObject();
                    String releaseVersion = releaseJson.get("tag_name").getAsString();
                    if(releaseVersion.startsWith(tagPrefix))
                        releaseVersion = releaseVersion.substring(tagPrefix.length());
                    if(compareVersions(releaseVersion, currentVersion)<1){
                        Logger.info("Up to date.");
                        Logger.pop();
                        return this;
                    }
                    Logger.info("Found new version: "+releaseVersion);
                    var assets = releaseJson.getAsJsonArray("assets");

                    for(int i = 0; i<assets.size(); i++){
                        var asset = assets.get(i).getAsJsonObject();
                        String name = asset.get("name").getAsString();
                        if(fileRegex!=null&&!Pattern.matches(fileRegex, name))
                            continue;
                        newVersionDownloadURL = asset.get("browser_download_url").getAsString();
                        newVersionString = releaseVersion;
                        newVersionFileName = name;
                        hasNewVersion = true;
                        break;
                    }
                }
            }
        }catch(Exception ex){
            Logger.error("GitHub update check failed!", ex);
        }
        Logger.pop();
        return this;
    }
    private int compareVersions(String v1, String v2){
        String[] parts1 = v1.split("-", 2);
        String[] parts2 = v2.split("-", 2);

        // Compare core versions (Major.Minor.Patch)
        String[] core1 = parts1[0].split("\\.");
        String[] core2 = parts2[0].split("\\.");

        for(int i = 0; i<3; i++){
            int c1 = Integer.parseInt(core1[i]);
            int c2 = Integer.parseInt(core2[i]);
            if(c1!=c2){
                return Integer.compare(c1, c2);
            }
        }

        // Core versions are identical; check pre-release status
        boolean hasPre1 = parts1.length>1;
        boolean hasPre2 = parts2.length>1;

        if(hasPre1&&!hasPre2)return -1; // Pre-release is lower than normal version
        if(!hasPre1&&hasPre2)return 1;  // Normal version is higher than pre-release
        if(!hasPre1&&!hasPre2)return 0; // Both are normal and identical

        String[] preParts1 = parts1[1].split("\\.");
        String[] preParts2 = parts2[1].split("\\.");

        for(int i = 0; i<Math.min(preParts1.length, preParts2.length); i++){
            String p1 = preParts1[i];
            String p2 = preParts2[i];

            if(!p1.equals(p2)){
                // If both are numeric, compare as integers
                if(p1.matches("\\d+")&&p2.matches("\\d+")){
                    return Integer.compare(Integer.parseInt(p1), Integer.parseInt(p2));
                }
                // Otherwise, compare alphabetically (identifiers with letters always take precedence)
                return p1.compareTo(p2);
            }
        }

        return Integer.compare(preParts1.length, preParts2.length);
    }
    public boolean update(){
        if(!hasNewVersion)return false;
        Logger.push(this);
        Logger.info("Fetching "+newVersionFileName+" from "+newVersionDownloadURL+"...");
        HttpURLConnection conn;
        try{
            conn = (HttpURLConnection)new URI(newVersionDownloadURL).toURL().openConnection();
            conn.setRequestMethod("GET");
            if(conn.getResponseCode()==200){
                Logger.info("Downloading file...");
                try(InputStream in = conn.getInputStream()){

                    Files.copy(in, Paths.get(newVersionFileName), StandardCopyOption.REPLACE_EXISTING);
                    Logger.info("Update complete!");
                    Logger.pop();
                    return true;
                }
            }
        }catch(Exception ex){
            Logger.error("Update failed!", ex);
        }
        Logger.pop();
        return false;
    }
    public static OperatingSystem identifyOperatingSystem(){
        String osName = System.getProperty("os.name");
        if(osName==null)osName = "null";
        osName = osName.toLowerCase(Locale.ROOT);
        if(osName.contains("win"))return OperatingSystem.WINDOWS;
        if(osName.contains("mac"))return OperatingSystem.MACOS;
        if(osName.contains("nix")||osName.contains("nux")||osName.contains("aix"))return OperatingSystem.LINUX;
        return OperatingSystem.UNKNOWN;
    }
    public static ProcessorArchitecture identifyProcessorArchitecture(){
        String osArch = System.getProperty("os.arch");
        if(osArch==null)osArch = "null";
        osArch = osArch.toLowerCase(Locale.ROOT);
        if(osArch.equals("amd64"))return ProcessorArchitecture.X86_64;
        if(osArch.equals("x64"))return ProcessorArchitecture.X86_64;
        if(osArch.equals("x86"))return ProcessorArchitecture.X86;
        if(osArch.equals("arm32"))return ProcessorArchitecture.ARM32;
        if(osArch.equals("arm64"))return ProcessorArchitecture.ARM64;
        if(osArch.equals("aarch32"))return ProcessorArchitecture.ARM32;
        if(osArch.equals("aarch64"))return ProcessorArchitecture.ARM64;
        return ProcessorArchitecture.UNKNOWN;
    }
    public static enum OperatingSystem{
        WINDOWS,
        MACOS,
        LINUX,
        UNKNOWN;
    }
    public static enum ProcessorArchitecture{
        X86,
        X86_64,
        ARM32,
        ARM64,
        UNKNOWN;
    }
}
