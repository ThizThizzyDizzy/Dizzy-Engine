package com.thizthizzydizzy.dizzyengine.graphics;
import com.thizthizzydizzy.dizzyengine.ResourceManager;
import java.nio.ByteBuffer;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
public class Font{
    public int texture;
    public int ascent, descent, lineGap;
    public final STBTTBakedChar.Buffer charBuffer;
    public final float yOff;
    public final float height;
    public final int bitmapSize;
    private HashMap<Character, FontCharacter> characters = new HashMap<>();
    public Font(STBTTFontinfo info, int texture, STBTTBakedChar.Buffer charBuffer, float yOff, float height, int bitmapSize){
        this.texture = texture;
        int[] ascent = new int[1];
        int[] descent = new int[1];
        int[] lineGap = new int[1];
        STBTruetype.stbtt_GetFontVMetrics(info, ascent, descent, lineGap);
        this.ascent = ascent[0];
        this.descent = descent[0];
        this.lineGap = lineGap[0];
        this.charBuffer = charBuffer;
        this.yOff = yOff;
        this.height = height;
        this.bitmapSize = bitmapSize;
        initCharacters();
    }
    private static final int fontHeight = 512;
    public static Font loadFont(ByteBuffer fontData){
        return loadFont(fontData, 0);
    }
    public static Font loadFont(ByteBuffer fontData, float yOff){
        return loadFont(fontData, yOff, fontHeight, fontHeight*8);
    }
    public static Font loadFont(ByteBuffer fontData, float yOff, float height, int bitmapSize){
        ByteBuffer buffer = BufferUtils.createByteBuffer(bitmapSize*bitmapSize*4);//no idea what this is used for, but it's here
        STBTTBakedChar.Buffer charBuffer = STBTTBakedChar.create(255);
        STBTruetype.stbtt_BakeFontBitmap(fontData, height, buffer, bitmapSize, bitmapSize, 0, charBuffer);
        int texture = ResourceManager.loadGLTexture(bitmapSize, bitmapSize, buffer);
        STBTTFontinfo info = STBTTFontinfo.create();
        if(!STBTruetype.stbtt_InitFont(info, fontData, 0))throw new RuntimeException("Failed to initialize font!");
        return new Font(info, texture, charBuffer, yOff, height, bitmapSize);
    }
    public void initCharacters(){
        for(char c = ' '; c<='~'; c++){
            characters.put(c, new FontCharacter(this, c));
        }
    }
    public float getStringWidth(String str, float height){
        float width = 0;
        for(char c : str.toCharArray()){
            if(characters.containsKey(c))width+=characters.get(c).dx/this.height;
        }
        return width*height;
    }
    public FontCharacter getCharacter(char c){
        return characters.get(c);
    }
}