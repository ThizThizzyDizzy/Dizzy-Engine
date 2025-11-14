package com.thizthizzydizzy.dizzyengine.graphics;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ResourceManager;
import com.thizthizzydizzy.dizzyengine.debug.PerformanceTracker;
import com.thizthizzydizzy.dizzyengine.graphics.image.Color;
import com.thizthizzydizzy.dizzyengine.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
public class Renderer{
    private static Shader defaultShader;
    private static Shader shader;
    private static Font defaultFont;
    private static Font font;
    private static Stack<Bound> boundStack = new Stack<>();
    private static Matrix4fStack modelMatStack = new Matrix4fStack(64);
    private static final HashMap<String, Element> elements = new HashMap<>();
    public static float preferredTextScale = 24;
    private static final String[] fallbackFontPaths = new String[]{
        "/usr/share/fonts/liberation/LiberationSans-Regular.ttf",
        "/usr/share/fonts/liberation-fonts/LiberationSans-Regular.ttf",
        "C:\\Windows\\Fonts\\comic.ttf"
    };
    public static void setDefaultFont(Font f){
        defaultFont = f;
        if(font==null)font = f;
    }
    public static void setFont(Font f){
        font = f;
    }
    public static Font getFont(){
        Logger.push(Renderer.class);
        if(font==null&&defaultFont==null){
            Logger.warn("defaultFont is null! Attempting to set default font to a fallback font.");
            for(String path : fallbackFontPaths){
                Logger.warn("Attempting to load fallback font: "+path);
                Font fallbackFont = null;
                try{
                    fallbackFont = Font.loadFont(ResourceManager.loadData(new FileInputStream(new File(path))));
                }catch(Exception ex){
                    Logger.warn("Failed to load fallback font: "+path, ex);
                }
                if(fallbackFont!=null){
                    setDefaultFont(fallbackFont);
                    Logger.warn("Set fallback font: "+path);
                    break;
                }
            }
            if(defaultFont==null){
                Logger.error("Unable to set fallback font!");
            }
        }
        if(font==null){
            Logger.warn("font is null! Setting to default font.");
            setFont(defaultFont);
        }
        Logger.pop();
        return font;
    }
    public static void resetFont(){
        font = defaultFont;
    }
    static{
        elements.put("rect", createRectangleElement(0, 0, 1, 1));
        elements.put("cube", new Element(){
            public int vao, vbo, ebo;
            @Override
            public void init(){
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();
                float[] verticies = new float[]{
                    //+z
                    0, 0, 1, 0, 0, 1, 0, 1,
                    0, 1, 1, 0, 0, 1, 0, 0,
                    1, 0, 1, 0, 0, 1, 1, 1,
                    1, 1, 1, 0, 0, 1, 1, 0,
                    //-z
                    0, 0, 0, 0, 0, -1, 0, 1,
                    1, 0, 0, 0, 0, -1, 1, 1,
                    0, 1, 0, 0, 0, -1, 0, 0,
                    1, 1, 0, 0, 0, -1, 1, 0,
                    //+y
                    0, 1, 0, 0, 1, 0, 0, 0,
                    1, 1, 0, 0, 1, 0, 1, 0,
                    0, 1, 1, 0, 1, 0, 0, 1,
                    1, 1, 1, 0, 1, 0, 1, 1,
                    //-y
                    0, 0, 0, 0, -1, 0, 0, 1,
                    0, 0, 1, 0, -1, 0, 0, 0,
                    1, 0, 0, 0, -1, 0, 1, 1,
                    1, 0, 1, 0, -1, 0, 1, 0,
                    //+x
                    1, 0, 0, 1, 0, 0, 0, 1,
                    1, 0, 1, 1, 0, 0, 1, 1,
                    1, 1, 0, 1, 0, 0, 0, 0,
                    1, 1, 1, 1, 0, 0, 1, 0,
                    //-x
                    0, 0, 0, -1, 0, 0, 0, 1,
                    0, 1, 0, -1, 0, 0, 0, 0,
                    0, 0, 1, -1, 0, 0, 1, 1,
                    0, 1, 1, -1, 0, 0, 1, 0
                };
                int[] indicies = new int[]{
                    //+z
                    1, 0, 2,
                    3, 1, 2,
                    //-z
                    5, 4, 6,
                    7, 5, 6,
                    //+y
                    9, 8, 10,
                    11, 9, 10,
                    //-y
                    13, 12, 14,
                    15, 13, 14,
                    //+x
                    17, 16, 18,
                    19, 17, 18,
                    //-x
                    21, 20, 22,
                    23, 21, 22
                };

                glBindVertexArray(vao);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, verticies, GL_STREAM_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STREAM_DRAW);

                //pos
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 8*4, 0);

                //norm
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, 8*4, 3*4);

                //tex
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*4, 6*4);

                glBindVertexArray(0);
            }
            @Override
            public void draw(){
                glBindVertexArray(vao);
                glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
                PerformanceTracker.drawCalls++;
                glBindVertexArray(0);
            }
            @Override
            public void cleanup(){
                glDeleteBuffers(ebo);
                glDeleteBuffers(vbo);
                glDeleteVertexArrays(vao);
            }
        });
    }
    private static Element createRectangleElement(float left, float top, float right, float bottom){
        return new Element(){
            public int vao, vbo, ebo;
            @Override
            public void init(){
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();

                float[] verticies = new float[]{
                    0, 0, 0, 0, 0, 1, left, bottom,//top left
                    0, 1, 0, 0, 0, 1, left, top,//bottom left
                    1, 0, 0, 0, 0, 1, right, bottom,//top right
                    1, 1, 0, 0, 0, 1, right, top//bottom right
                };
                int[] indicies = new int[]{
                    1, 0, 2,
                    3, 1, 2
                };

                glBindVertexArray(vao);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, verticies, GL_STREAM_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STREAM_DRAW);

                //pos
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 8*4, 0);

                //norm
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, 8*4, 3*4);

                //tex
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*4, 6*4);

                glBindVertexArray(0);
            }
            @Override
            public void draw(){
                glBindVertexArray(vao);
                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
                PerformanceTracker.drawCalls++;
                glBindVertexArray(0);
            }
            @Override
            public void cleanup(){
                glDeleteBuffers(ebo);
                glDeleteBuffers(vbo);
                glDeleteVertexArrays(vao);
            }
        };
    }
    private static Element createRegularPolygonElement(int sides){
        return new Element(){
            public int vao, vbo, ebo;
            @Override
            public void init(){
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();

                ArrayList<Float> verticiesList = new ArrayList<>();
                verticiesList.addAll(Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));//center point
                float angle = 0;
                for(int i = 0; i<sides; i++){
                    float x = (float)Math.cos(Math.toRadians(angle-90));
                    float y = (float)Math.sin(Math.toRadians(angle-90));
                    verticiesList.addAll(Arrays.asList(x, y, 0f, 0f, 0f, 1f, 0f, 0f));
                    angle += (360f/sides);
                }

                float[] verticies = new float[verticiesList.size()];
                for(int i = 0; i<verticiesList.size(); i++)
                    verticies[i] = verticiesList.get(i);

                ArrayList<Integer> indiciesList = new ArrayList<>();
                for(int i = 0; i<sides; i++){
                    indiciesList.add(0);
                    indiciesList.add(i+1);
                    indiciesList.add(i+2);
                }
                int[] indicies = new int[indiciesList.size()];
                for(int i = 0; i<indiciesList.size(); i++){
                    indicies[i] = indiciesList.get(i);
                }

                glBindVertexArray(vao);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, verticies, GL_STREAM_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STREAM_DRAW);

                //pos
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 8*4, 0);

                //norm
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, 8*4, 3*4);

                //tex
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*4, 6*4);

                glBindVertexArray(0);
            }
            @Override
            public void draw(){
                glBindVertexArray(vao);
                glDrawElements(GL_TRIANGLES, sides*3, GL_UNSIGNED_INT, 0);
                PerformanceTracker.drawCalls++;
                glBindVertexArray(0);
            }
            @Override
            public void cleanup(){
                glDeleteBuffers(ebo);
                glDeleteBuffers(vbo);
                glDeleteVertexArrays(vao);
            }
        };
    }
    private static Element createHollowRegularPolygonElement(int sides, float sizeRatioX, float sizeRatioY){
        return createHollowRegularPolygonSegmentElement(sides, sizeRatioX, sizeRatioY, 0, sides);
    }
    private static Element createHollowRegularPolygonSegmentElement(int sides, float sizeRatioX, float sizeRatioY, int left, int right){
        return new Element(){
            public int vao, vbo, ebo;
            @Override
            public void init(){
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();

                int actualSides = 0;

                // Hi future thiz, you have found that this does not work. congratz.
                // You need to properly start at one end of the arc and draw to the other, rather than using this "in range" nonsense.
                ArrayList<Float> verticiesList = new ArrayList<>();
                float angle = 0;
                for(int i = 0; i<sides; i++){
                    boolean inRange = false;
                    if(left>right)inRange = i>=left||i<=right;
                    else
                        inRange = i>=left&&i<=right;
                    float x = (float)Math.cos(Math.toRadians(angle-90));
                    float y = (float)Math.sin(Math.toRadians(angle-90));
                    if(inRange){
                        verticiesList.addAll(Arrays.asList(x, y, 0f, 0f, 0f, 1f, 0f, 0f));
                        verticiesList.addAll(Arrays.asList(x*sizeRatioX, y*sizeRatioY, 0f, 0f, 0f, 1f, 0f, 0f));
                        actualSides++;
                    }
                    angle += (360f/sides);
                }

                float[] verticies = new float[verticiesList.size()];
                for(int i = 0; i<verticiesList.size(); i++)
                    verticies[i] = verticiesList.get(i);

                ArrayList<Integer> indiciesList = new ArrayList<>();
                for(int i = 0; i<actualSides; i++){
                    indiciesList.add(0);
                    indiciesList.add(i+1);
                    indiciesList.add(i+2);
                    indiciesList.add(0);
                    indiciesList.add(i+2);
                    indiciesList.add(i+3);
                }
                int[] indicies = new int[indiciesList.size()];
                for(int i = 0; i<indiciesList.size(); i++){
                    indicies[i] = indiciesList.get(i);
                }

                glBindVertexArray(vao);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, verticies, GL_STREAM_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STREAM_DRAW);

                //pos
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 8*4, 0);

                //norm
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, 8*4, 3*4);

                //tex
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*4, 6*4);

                glBindVertexArray(0);
            }
            @Override
            public void draw(){
                glBindVertexArray(vao);
                glDrawElements(GL_TRIANGLES, sides*6, GL_UNSIGNED_INT, 0);
                PerformanceTracker.drawCalls++;
                glBindVertexArray(0);
            }
            @Override
            public void cleanup(){
                glDeleteBuffers(ebo);
                glDeleteBuffers(vbo);
                glDeleteVertexArrays(vao);
            }
        };
    }
    private static Element createHollowCosGearElement(int teeth, int resolution, float toothSizeRatio, float sizeRatio){
        return new Element(){
            public int vao, vbo, ebo;
            @Override
            public void init(){
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();

                ArrayList<Float> verticiesList = new ArrayList<>();
                float angle = 0;
                for(int i = 0; i<teeth*resolution; i++){
                    float toothRad = 1+(float)((toothSizeRatio/2)*Math.cos(Math.toRadians(teeth*angle)));
                    float x = (float)Math.cos(Math.toRadians(angle-90));
                    float y = (float)Math.sin(Math.toRadians(angle-90));
                    verticiesList.addAll(Arrays.asList(x*toothRad, y*toothRad, 0f, 0f, 0f, 1f, 0f, 0f));
                    verticiesList.addAll(Arrays.asList(x*sizeRatio, y*sizeRatio, 0f, 0f, 0f, 1f, 0f, 0f));
                    angle += (360f/(teeth*resolution));
                }

                float[] verticies = new float[verticiesList.size()];
                for(int i = 0; i<verticiesList.size(); i++)
                    verticies[i] = verticiesList.get(i);

                ArrayList<Integer> indiciesList = new ArrayList<>();
                for(int i = 0; i<teeth*resolution; i++){
                    indiciesList.add(0);
                    indiciesList.add(i+1);
                    indiciesList.add(i+2);
                    indiciesList.add(0);
                    indiciesList.add(i+2);
                    indiciesList.add(i+3);
                }
                int[] indicies = new int[indiciesList.size()];
                for(int i = 0; i<indiciesList.size(); i++){
                    indicies[i] = indiciesList.get(i);
                }

                glBindVertexArray(vao);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, verticies, GL_STREAM_DRAW);

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_STREAM_DRAW);

                //pos
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 8*4, 0);

                //norm
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, 8*4, 3*4);

                //tex
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*4, 6*4);

                glBindVertexArray(0);
            }
            @Override
            public void draw(){
                glBindVertexArray(vao);
                glDrawElements(GL_TRIANGLES, teeth*resolution*6, GL_UNSIGNED_INT, 0);
                PerformanceTracker.drawCalls++;
                glBindVertexArray(0);
            }
            @Override
            public void cleanup(){
                glDeleteBuffers(ebo);
                glDeleteBuffers(vbo);
                glDeleteVertexArrays(vao);
            }
        };
    }
    public static void initElements(){
        for(Element e : elements.values())e.init();
        
        DizzyEngine.addShutdownHook(Renderer::cleanupElements);
    }
    public static void cleanupElements(){
        for(Element e : elements.values())e.cleanup();
    }
    public static void drawHorizontalLine(float x1, float y, float x2, float thickness, int texture){
        fillRect(x1, y-thickness/2, x2, y+thickness/2, texture);
    }
    public static void drawVerticalLine(float x, float y1, float y2, float thickness, int texture){
        fillRect(x-thickness/2, y1, x+thickness/2, y2, texture);
    }
    public static void fillRect(float left, float top, float right, float bottom){
        fillRect(left, top, right, bottom, 0);
    }
    public static void fillRect(float left, float top, float right, float bottom, int texture){
        if(right<left){
            float r = left;
            float l = right;
            right = r;
            left = l;
        }
        if(bottom<top){
            float b = top;
            float t = bottom;
            bottom = b;
            top = t;
        }
        bindTexture(texture);
        drawElement("rect", left, top, right-left, bottom-top);
    }
    // Much faster than using stencil bounds
    public static void fillRectWithBound(float left, float top, float right, float bottom, int texture, float leftBound, float topBound, float rightBound, float bottomBound){
        float width = right-left;
        float height = bottom-top;

        float rectLeft = Math.max(left, leftBound);
        float rectTop = Math.max(top, topBound);
        float rectRight = Math.min(right, rightBound);
        float rectBottom = Math.min(bottom, bottomBound);

        float rectWidth = rectRight-rectLeft;
        float rectHeight = rectBottom-rectTop;
        if(rectWidth<=0||rectHeight<=0)return;//bounded out of existence

        float texLeft = Math.max(0, leftBound-left)/width;
        float texTop = Math.max(0, topBound-top)/height;

        float texRight = 1+Math.min(0, rightBound-right)/width;
        float texBottom = 1+Math.min(0, bottomBound-bottom)/height;

        fillRect(rectLeft, rectTop, rectRight, rectBottom, texture, texLeft, texTop, texRight, texBottom);
    }
    public static void fillRect(float left, float top, float right, float bottom, int texture, float texLeft, float texTop, float texRight, float texBottom){
        if(right<left){
            float r = left;
            float l = right;
            right = r;
            left = l;
        }
        if(bottom<top){
            float b = top;
            float t = bottom;
            bottom = b;
            top = t;
        }
        bindTexture(texture);
        String key = "DizzyEngine:Rectangle_"+texLeft+"_"+texTop+"_"+texRight+"_"+texBottom;
        var element = elements.get(key);
        if(element==null){
            element = createRectangleElement(texLeft, texTop, texRight, texBottom);
            elements.put(key, element);
            element.init();
        }
        drawElement(element, left, top, right-left, bottom-top);
    }
    public static void fillRegularPolygon(float x, float y, int sides, float radius){
        fillRegularPolygon(x, y, sides, radius, radius);
    }
    public static void fillRegularPolygon(float x, float y, int sides, float radiusX, float radiusY){
        if(sides<3)
            throw new IllegalArgumentException("A polygon must have at least 3 sides!");
        bindTexture(0);
        String key = "DizzyEngine:RegularPolygon_"+sides;
        var element = elements.get(key);
        if(element==null){
            element = createRegularPolygonElement(sides);
            elements.put(key, element);
            element.init();
        }
        drawElement(element, x, y, radiusX, radiusY);
    }
    public static void fillHollowRegularPolygon(float x, float y, int sides, float innerRadius, float outerRadius){
        fillHollowRegularPolygon(x, y, sides, innerRadius, innerRadius, outerRadius, outerRadius);
    }
    public static void fillHollowRegularPolygon(float x, float y, int sides, float innerRadiusX, float innerRadiusY, float outerRadiusX, float outerRadiusY){
        if(sides<3)
            throw new IllegalArgumentException("A polygon must have at least 3 sides!");
        float sizeRatioX = innerRadiusX/outerRadiusX;
        float sizeRatioY = innerRadiusY/outerRadiusY;
        bindTexture(0);
        String key = "DizzyEngine:HollowRegularPolygon_"+sides+"_"+sizeRatioX+"_"+sizeRatioY;
        var element = elements.get(key);
        if(element==null){
            element = createHollowRegularPolygonElement(sides, sizeRatioX, sizeRatioY);
            elements.put(key, element);
            element.init();
        }
        drawElement(element, x, y, outerRadiusX, outerRadiusY);
    }
    public static void fillHollowRegularPolygonSegment(float x, float y, int sides, float innerRadiusX, float innerRadiusY, float outerRadiusX, float outerRadiusY, int left, int right){
        if(sides<3)
            throw new IllegalArgumentException("A polygon must have at least 3 sides!");
        while(left<0)left += sides;
        while(right<0)right += sides;
        while(left>sides)left -= sides;
        while(right>sides)right -= sides;
        float sizeRatioX = innerRadiusX/outerRadiusX;
        float sizeRatioY = innerRadiusY/outerRadiusY;
        bindTexture(0);
        String key = "DizzyEngine:HollowRegularPolygonSegment_"+sides+"_"+sizeRatioX+"_"+sizeRatioY+"_"+left+"_"+right;
        var element = elements.get(key);
        if(element==null){
            element = createHollowRegularPolygonSegmentElement(sides, sizeRatioX, sizeRatioY, left, right);
            elements.put(key, element);
            element.init();
        }
        drawElement(element, x, y, outerRadiusX, outerRadiusY);
    }
    public static void fillHollowCosGear(float x, float y, float innerRadius, float outerRadius, float toothSize, int teeth, float rotation){
        fillHollowCosGear(x, y, innerRadius, outerRadius, toothSize, teeth, rotation, 10);
    }
    public static void fillHollowCosGear(float x, float y, float innerRadius, float outerRadius, float toothSize, int teeth, float rotation, int resolution){
        if(teeth<3)
            throw new IllegalArgumentException("A gear must have at least 3 teeth!");
        float sizeRatio = innerRadius/outerRadius;
        float toothSizeRatio = toothSize/outerRadius;
        bindTexture(0);
        String key = "DizzyEngine:HollowCosGear_"+teeth+"_"+resolution+"_"+toothSizeRatio+"_"+sizeRatio;
        var element = elements.get(key);
        if(element==null){
            element = createHollowCosGearElement(teeth, resolution, toothSizeRatio, sizeRatio);
            elements.put(key, element);
            element.init();
        }
        translate(x, y);
        modelMatStack.mul(new Matrix4f().rotate(rotation, 0, 0, 1));
        drawElement(element, 0, 0, outerRadius, outerRadius);
        unTranslate();
    }
    /**
     * Wraps text around \n and at a specified length
     *
     * @param text The full string of text to draw
     * @param width The maximum width for each line
     * @param height The text height, used to measure string width
     * @param drawFunc The function to draw each line of text (line number
     * starting at 0, and line text)
     */
    public static void wrapText(String text, float width, float height, BiConsumer<Integer, String> drawFunc){
        wrapText(text, (i) -> width, height, drawFunc);
    }
    /**
     * Wraps text around \n and at a specified length
     *
     * @param text The full string of text to draw
     * @param width The maximum width for each line (as a function of line #)
     * @param height The text height, used to measure string width
     * @param drawFunc The function to draw each line of text (line number
     * starting at 0, and line text)
     */
    public static void wrapText(String text, Function<Integer, Float> width, float height, BiConsumer<Integer, String> drawFunc){
        int lineNumber = 0;
        while(!text.isEmpty()){
            String line = text.charAt(0)+"";
            int i;
            for(i = 1; i<text.length(); i++){
                if(line.contains("\n"))break;//next line
                String newLine = line+text.charAt(i);
                if(getStringWidth(newLine, height)>width.apply(lineNumber))
                    break;
                line = newLine;
            }
            drawFunc.accept(lineNumber++, line);
            text = text.substring(i);
        }
    }
    /**
     * Wraps text around words separated by spaces, and around \n. Ignores
     * leading & trailing whitespace.
     *
     * @param text The full string of text to draw
     * @param width The maximum width for each line
     * @param height The text height, used to measure string width
     * @param drawFunc The function to draw each line of text (line number
     * starting at 0, and line text)
     */
    public static void wordWrapText(String text, float width, float height, BiConsumer<Integer, String> drawFunc){
        wordWrapText(text, (i) -> width, height, drawFunc);
    }
    /**
     * Wraps text around words separated by spaces, and around \n. Ignores
     * leading & trailing whitespace.
     *
     * @param text The full string of text to draw
     * @param width The maximum width for each line (as a function of line #)
     * @param height The text height, used to measure string width
     * @param drawFunc The function to draw each line of text (line number
     * starting at 0, and line text)
     */
    public static void wordWrapText(String text, Function<Integer, Float> width, float height, BiConsumer<Integer, String> drawFunc){
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.replace("\n", "\n ").split(" ")));
        int lineNumber = 0;
        while(!words.isEmpty()){
            String line = words.remove(0);

            while(!words.isEmpty()){
                if(line.contains("\n"))break;//next line
                String newLine = line+" "+words.get(0);
                if(getStringWidth(newLine, height)>width.apply(lineNumber))
                    break;
                words.remove(0);
                line = newLine;
            }
            drawFunc.accept(lineNumber++, line.trim());
        }
    }
    public static void drawText(float x, float y, String text, float height){
        if(height<0)return;
        var font = getFont();
        bindTexture(font.texture);
        for(int i = 0; i<text.length(); i++){
            char c = text.charAt(i);
            if(c=='\n'){
                if(i==text.length()-1)continue;//last character of string, ignore
                throw new IllegalArgumentException("Cannot draw newline character!");
            }
            FontCharacter character = font.getCharacter(c);
            if(character==null){
                System.err.println("Unknown font character: "+c);
                character = font.getCharacter('?');
            }
            model(createModelMatrix(x, y+height, height, height));
            character.draw();
            x += character.dx/font.height*height;
            y += character.dy/font.height*height;
        }
        resetModelMatrix();
    }
    public static void drawCenteredText(float left, float top, float right, float bottom, String text){
        float width = getStringWidth(text, bottom-top);
        while(width>right-left&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            width = getStringWidth(text, bottom-top);
        }
        drawText((left+right)/2-width/2, top, text, bottom-top);
    }
    public static void drawRightText(float left, float top, float right, float bottom, String text){
        float width = getStringWidth(text, bottom-top);
        while(width>right-left&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            width = getStringWidth(text, bottom-top);
        }
        drawText((left+right)/2-width, top, text, bottom-top);
    }
    public static void drawText(float left, float top, float right, float bottom, String text){
        float width = getStringWidth(text, bottom-top);
        while(width>right-left&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            width = getStringWidth(text, bottom-top);
        }
        drawText(left, top, text, bottom-top);
    }
    public static float getStringWidth(String text, float height){
        return getFont().getStringWidth(text, height);
    }
    public static void setColor(Color c){
        setColor(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, c.getAlpha()/255f);
    }
    public static void setColor(Color c, float alpha){
        setColor(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, c.getAlpha()/255f*alpha);
    }
    private static Vector4f currentColor = new Vector4f(1, 1, 1, 1);
    public static void setColor(float red, float green, float blue, float alpha){
        if(currentColor.equals(red, green, blue, alpha))return;
        shader.setUniform4f("color", red, green, blue, alpha);
        currentColor.set(red, green, blue, alpha);
    }
    public static void setDefaultShader(Shader shader){
        defaultShader = shader;
    }
    public static void resetShader(){
        setShader(defaultShader);
    }
    public static void setShader(Shader shader){
        Renderer.shader = shader;
        shader.use();
    }
    private static Matrix4f createModelMatrix(float x, float y, float scaleX, float scaleY){
        return new Matrix4f().setTranslation(x, y, 0).scaleXY(scaleX, scaleY);
    }
    public static void model(Matrix4f matrix){
        setExactModelMatrix(modelMatStack.mul(matrix, matrix));
    }
    public static void pushModel(Matrix4f matrix){
        modelMatStack.pushMatrix();
        modelMatStack.mul(matrix);
        resetModelMatrix();
    }
    public static void popModel(){
        modelMatStack.popMatrix();
        resetModelMatrix();
    }
    public static void setModel(Matrix4f matrix){
        resetModelMatrix();
        model(matrix);
    }
    private static void setExactModelMatrix(Matrix4f matrix){
        shader.setUniformMatrix4fv("model", matrix);
    }
    public static void resetModelMatrix(){
        model(new Matrix4f());
    }
    public static void view(Matrix4f matrix){
        shader.setUniformMatrix4fv("view", matrix);
    }
    public static void projection(Matrix4f matrix){
        shader.setUniformMatrix4fv("projection", matrix);
    }
    private static int boundTexture = -1;
    public static void unbindTexture(){
        bindTexture(0);
    }
    public static void bindTexture(int tex){
        glBindTexture(GL_TEXTURE_2D, tex);
        if(boundTexture==tex)return;
        if(tex==0)shader.setUniform4f("noTex", 1f, 1f, 1f, 0f);
        else
            shader.setUniform4f("noTex", 0f, 0f, 0f, 0f);
        boundTexture = tex;
    }
    public static void bound(float left, float top, float right, float bottom){
        if(boundStack.size()>=1024)
            throw new StackOverflowError("Exceeded render  bound stack limit! ("+boundStack.size()+")");
        boundStack.push(new Bound(modelMatStack.get(new Matrix4f()), left, top, right, bottom));
        redrawStencil();
    }
    public static void translate(float x, float y){
        translate(x, y, 1, 1);
    }
    public static void translate(float x, float y, float sx, float sy){
        modelMatStack.pushMatrix();
//        createModelMatrix(x, y, sx, sy).mul(modelMatStack, modelMatStack);
        modelMatStack.mul(createModelMatrix(x, y, sx, sy));
        resetModelMatrix();
    }
    public static void redrawStencil(){
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilFunc(GL_ALWAYS, 1, 0xff);
        glStencilMask(0xff);
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_STENCIL_BUFFER_BIT);
        glColorMask(false, false, false, false);
        glDepthMask(false);
        for(int i = 0; i<boundStack.size(); i++){
            Bound bound = boundStack.get(i);
            setExactModelMatrix(bound.modelMatrix);
            bound.draw();
        }
        glStencilFunc(GL_NOTEQUAL, 1, 0xff);
        glDepthMask(true);
        glColorMask(true, true, true, true);
        glStencilMask(0x00);
        resetModelMatrix();
    }
    public static void unTranslate(){
        modelMatStack.popMatrix();
        resetModelMatrix();
    }
    public static void unBound(){
        boundStack.pop();
        redrawStencil();
    }
    public static void clearTranslationsAndBounds(){
        clearTranslations();
        clearBounds();
    }
    public static void clearTranslations(){
        modelMatStack.clear();
        resetModelMatrix();
    }
    public static void clearBounds(){
        if(!boundStack.isEmpty())
            Logger.warn("Found "+boundStack.size()+" bounds after frame render! Clearing stencil...");
        boundStack.clear();
        redrawStencil();
    }
    public static float getPreferredTextHeight(){
        return DizzyEngine.getUIContext().getUnitScale()*preferredTextScale;
    }
    private static class Bound{
        private final Matrix4f modelMatrix;
        private final float left;
        private final float top;
        private final float right;
        private final float bottom;
        public Bound(Matrix4f modelMatrix, float left, float top, float right, float bottom){
            this.modelMatrix = modelMatrix;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
        public void draw(){
            var size = DizzyEngine.screenSize;
            fillRect(-size.x, -size.y, left, size.y);
            fillRect(left, -size.y, right, top);
            fillRect(left, bottom, right, size.y);
            fillRect(right, -size.y, size.x, size.y);
        }
    }
    public static void drawElement(String name, float x, float y, float scaleX, float scaleY){
        drawElement(name, x, y, 1, scaleX, scaleY, 1);
    }
    public static void drawElement(String name, float x, float y, float z, float scaleX, float scaleY, float scaleZ){
        model(new Matrix4f().setTranslation(x, y, z).scale(scaleX, scaleY, scaleZ));
        drawElement(name);
        resetModelMatrix();
    }
    public static void drawElement(String name){
        if(!elements.containsKey(name))
            throw new IllegalArgumentException("Cannot draw element: "+name+" does not exist!");
        elements.get(name).draw();
    }
    public static void drawElement(Element element, float x, float y, float width, float height){
        drawElement(element, x, y, 1, width, height, 1);
    }
    public static void drawElement(Element element, float x, float y, float z, float width, float height, float depth){
        model(new Matrix4f().setTranslation(x, y, z).scale(width, height, depth));
        drawElement(element);
        resetModelMatrix();
    }
    public static void drawElement(Element element){
        element.draw();
    }
    public static void reset(){
        resetShader();
        clearTranslationsAndBounds();
        unbindTexture();
        setColor(1f, 1f, 1f, 1f);
    }
    public static interface Element{
        public void init();
        public void draw();
        public void cleanup();
    }
}
