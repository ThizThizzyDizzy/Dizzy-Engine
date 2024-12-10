package com.thizthizzydizzy.dizzyengine.graphics.image;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.Framebuffer;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import java.nio.ByteBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
public abstract class ImageRenderer{
    protected int width;
    protected int height;
    protected abstract void draw();
    public Image renderImage(int width, int height){
        return renderImageFlipped(width, height).flip(); // no, this isn't a typo. flip() un-flips the image from OpenGL, which is flipped by default
    }
    public Image renderImageFlipped(int width, int height){
        this.width = width;
        this.height = height;
        boolean cull = glIsEnabled(GL_CULL_FACE);
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if(cull)glDisable(GL_CULL_FACE);
        if(depth)glDisable(GL_DEPTH_TEST);
        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(width*height*4);

        Framebuffer framebuffer = new Framebuffer(width, height);

        glViewport(0, 0, width, height);
        glClearColor(0f, 0f, 0f, 0f);
        glStencilMask(0xff);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
        glStencilMask(0x00);

        Renderer.projection(new Matrix4f().setOrtho(0, width, height, 0, 0.1f, 10f));

        draw();

        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(0, 0, DizzyEngine.screenSize.x, DizzyEngine.screenSize.y);

        framebuffer.destroy();

        int[] imgRGBData = new int[width*height];
        byte[] imgData = new byte[width*height*4];
        imageBuffer.rewind();
        imageBuffer.get(imgData);
        Image img = new Image(width, height);
        for(int i = 0; i<imgRGBData.length; i++){
            imgRGBData[i] = (f(imgData[i*4])<<16)+(f(imgData[i*4+1])<<8)+(f(imgData[i*4+2]))+(f(imgData[i*4+3])<<24);//DO NOT Use RED, GREEN, or BLUE channel (here BLUE) for alpha data
        }
        img.setRGB(0, 0, width, height, imgRGBData, 0, width);
        if(cull)glEnable(GL_CULL_FACE);
        if(depth)glEnable(GL_DEPTH_TEST);
        return img;
    }
    private static int f(byte imgData){
        return (imgData+256)&255;
    }
}
