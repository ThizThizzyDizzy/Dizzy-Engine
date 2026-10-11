package com.thizthizzydizzy.dizzyengine.graphics.text;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ResourceManager;
import java.nio.ByteBuffer;
import java.util.HashMap;
import com.thizthizzydizzy.dizzyengine.platform.Buffers;
import java.util.Map;
import java.util.Objects;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
public class Font{
    public interface Backend { Font load(ByteBuffer data, float yOff, float height, int bitmapSize); }
    private static Backend backend;
    public static void setBackend(Backend replacement) { backend = Objects.requireNonNull(replacement); }
    public static final class Glyph {
        public final float advance;
        private final float x0, y0, x1, y1, s0, t0, s1, t1;
        public Glyph(float advance, float x0, float y0, float x1, float y1, float s0, float t0, float s1, float t1) {
            this.advance=advance;this.x0=x0;this.y0=y0;this.x1=x1;this.y1=y1;this.s0=s0;this.t0=t0;this.s1=s1;this.t1=t1;
        }
        public float x0(){return x0;} public float y0(){return y0;} public float x1(){return x1;} public float y1(){return y1;}
        public float s0(){return s0;} public float t0(){return t0;} public float s1(){return s1;} public float t1(){return t1;}
    }
    private Map<Character, Glyph> glyphs;
    /** Creates a font from host-rasterized glyphs. Existing STB constructors remain available. */
    public Font(int texture, float height, float yOff, int bitmapSize, Map<Character, Glyph> glyphs) {
        this.texture=texture;this.height=height;this.yOff=yOff;this.bitmapSize=bitmapSize;this.charBuffer=null;this.glyphs=glyphs;
        initCharacters();
    }
    Glyph getGlyph(char c) {
        if(glyphs != null)return glyphs.get(c);
        STBTTAlignedQuad q=STBTTAlignedQuad.create();
        float[] x=new float[1], y=new float[1];
        STBTruetype.stbtt_GetBakedQuad(charBuffer, bitmapSize, bitmapSize, c, x, y, q, true);
        return new Glyph(x[0], q.x0(), q.y0(), q.x1(), q.y1(), q.s0()/4, q.t0()/4, q.s1()/4, q.t1()/4);
    }

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
        if(backend != null)return backend.load(fontData, yOff, height, bitmapSize);
        if(DizzyEngine.headless)return null;
        ByteBuffer buffer = Buffers.createByteBuffer(bitmapSize*bitmapSize*4);//no idea what this is used for, but it's here
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