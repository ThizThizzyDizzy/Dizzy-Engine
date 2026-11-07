import com.thizthizzydizzy.dizzyengine.*;
import com.thizthizzydizzy.dizzyengine.graphics.*;
import com.thizthizzydizzy.dizzyengine.graphics.text.*;
import com.thizthizzydizzy.dizzyengine.graphics.image.ImageRenderer;
import com.thizthizzydizzy.dizzyengine.platform.*;
import java.lang.reflect.Proxy;
import com.thizthizzydizzy.dizzyengine.ui.UILayer;
import java.nio.ByteBuffer;
import java.util.*;

/** Recording-backend regression for retained fonts across repeated context loss. */
public class HostedGraphicsRecoveryTest{
    private static void check(boolean condition, String message){
        if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args){
        Set<Integer> arrays = new HashSet<>(), textures = new HashSet<>();
        Set<Integer> programs = new HashSet<>(), shaders = new HashSet<>();
        int[] next = {1}, uploads = {0}, draws = {0}, initializations = {0}, restores = {0};
        int[] framebuffer = {7}, postImageDraws = {0}, deletedFramebuffers = {0};
        boolean[] imageDraw = {false};
        GL.setBackend((GLBackend)Proxy.newProxyInstance(GLBackend.class.getClassLoader(),
            new Class<?>[]{GLBackend.class}, (proxy, method, values) -> {
                switch(method.getName()){
                    case "glGetInteger": return framebuffer[0];
                    case "glBindFramebuffer": framebuffer[0] = (Integer)values[1]; break;
                    case "glCheckFramebufferStatus": return GL.GL_FRAMEBUFFER_COMPLETE;
                    case "glDeleteFramebuffers": deletedFramebuffers[0]++; break;
                    case "glCreateProgram": {
                        int id = next[0]++;
                        programs.add(id);
                        return id;
                    }
                    case "glCreateShader": {
                        int id = next[0]++;
                        shaders.add(id);
                        return id;
                    }
                    case "glDeleteProgram":
                        check(programs.remove((Integer)values[0]), "Deleted stale or duplicate program");
                        break;
                    case "glDeleteShader":
                        check(shaders.remove((Integer)values[0]), "Deleted stale or duplicate shader");
                        break;
                    case "glGenVertexArrays": {
                        int id = next[0]++;
                        arrays.add(id);
                        return id;
                    }
                    case "glGenTextures": {
                        int id = next[0]++;
                        textures.add(id);
                        return id;
                    }
                    case "glBindVertexArray":
                        check((Integer)values[0]==0 || arrays.contains((Integer)values[0]), "Stale glyph VAO");
                        break;
                    case "glBindTexture":
                        check((Integer)values[1]==0 || textures.contains((Integer)values[1]), "Stale atlas");
                        break;
                    case "glTexImage2D": uploads[0]++; break;
                    case "glDrawElements":
                        if(imageDraw[0]){
                            check(framebuffer[0]==7, "Drawing after offscreen rendering missed host framebuffer");
                            postImageDraws[0]++;
                        }else draws[0]++;
                        break;
                    case "glGetShaderiv": case "glGetProgramiv": ((int[])values[2])[0] = 1; break;
                }
                if(method.getReturnType()==int.class)return next[0]++;
                if(method.getReturnType()==boolean.class)return false;
                return null;
            }));
        ByteBuffer atlas = Buffers.createByteBuffer(4);
        Map<Character, Font.Glyph> glyphs = new HashMap<>();
        for(char c=' '; c<='~'; c++)glyphs.put(c, new Font.Glyph(8, 0, 0, 8, 8, 0, 0, 1, 1));
        Font font = new Font(ResourceManager.loadGLTexture(1, 1, atlas), 8, 0, 1, glyphs);
        FontCharacter retained = font.getCharacter('A');
        Renderer.setDefaultFont(font);
        DizzyEngine.addLayer(new DizzyLayer(){
            public void init(){initializations[0]++;}
            public void cleanup(){}
            public void restoreGraphics(){
                font.restoreContext(ResourceManager.loadGLTexture(1, 1, atlas));
                restores[0]++;
            }
            public void render(double deltaTime){
                check(framebuffer[0]==7, "Layer did not enter host framebuffer");
                new ImageRenderer(){protected void draw(){}}.renderImage(1, 1);
                imageDraw[0] = true;
                try{GL.glDrawElements(GL.GL_TRIANGLES, 0, GL.GL_UNSIGNED_INT, 0L);}
                finally{imageDraw[0] = false;}
                RuntimeException failure = new RuntimeException("expected draw failure");
                try{
                    new ImageRenderer(){protected void draw(){throw failure;}}.renderImage(1, 1);
                    throw new AssertionError("Draw failure was swallowed");
                }catch(RuntimeException ex){check(ex==failure, "Wrong draw failure");}
                check(framebuffer[0]==7, "Failed offscreen draw lost host framebuffer");
                check(Renderer.getFont()==font, "Renderer lost the retained font");
                check(font.getCharacter('A')==retained, "Retained glyph reference replaced");
                Renderer.bindTexture(font.texture);
                retained.draw();
                retained.drawItalic();
                check(font.getStringWidth("AA", 8)==16, "Font metrics changed");
            }
        });
        int[] uiRestores = {0};
        for(int i=0; i<2; i++)DizzyEngine.addLayer(new UILayer(){
            public float getUnitScale(){return 1;}
            public void init(){}
            public void cleanup(){}
            public void render(double deltaTime){}
            public void restoreGraphics(){
                check(DizzyEngine.getUIContext()==this, "Wrong UI context during restoration");
                uiRestores[0]++;
            }
        });
        DizzyEngine.initHosted(640, 480, new Shader("void main(){}", "void main(){}"));
        try{
            DizzyEngine.renderHosted(0.016);
            for(int i=0; i<2; i++){
                arrays.clear();
                textures.clear();
                programs.clear();
                shaders.clear();
                DizzyEngine.restoreHostedGraphics(new Shader("void main(){}", "void main(){}"));
                check(restores[0]==i+1, "Missing restoration callback");
                check(uiRestores[0]==2*(i+1), "Missing UI restoration callbacks");
                DizzyEngine.renderHosted(0.016);
            }
            check(postImageDraws[0]==3 && deletedFramebuffers[0]==6, "Offscreen draw or cleanup missing");
            check(initializations[0]==1, "Context recovery restarted the layer");
            check(uploads[0]==9 && draws[0]==6, "Atlas upload or glyph draw missing");
        }finally{
            DizzyEngine.shutdownHosted();
        }
        check(programs.isEmpty() && shaders.isEmpty(), "Live shader resources leaked at shutdown");
        System.out.println("Hosted graphics recovery checks passed");
    }
}
