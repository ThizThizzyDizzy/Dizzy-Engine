package com.thizthizzydizzy.dizzyengine.vr;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.DizzyLayer;
import com.thizthizzydizzy.dizzyengine.Framebuffer;
import com.thizthizzydizzy.dizzyengine.MathUtil;
import com.thizthizzydizzy.dizzyengine.graphics.Renderer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.OpenVR;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.TrackedDevicePose;
import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRCompositor.*;
import static org.lwjgl.openvr.VRSystem.*;
public class VRLayer extends DizzyLayer{
    private int vrWidth;
    private int vrHeight;
    private Framebuffer leftEyeBuffer;
    private Framebuffer rightEyeBuffer;
    @Override
    public void init(){
        if(!VR_IsRuntimeInstalled())throw new UnsupportedOperationException("OpenVR Runtime is not installed!");
        if(!VR_IsHmdPresent())throw new UnsupportedOperationException("VR HMD is not present!");
        IntBuffer peError = BufferUtils.createIntBuffer(1);
        int token = VR_InitInternal(peError, EVRApplicationType_VRApplication_Scene);
        if(peError.get(0)!=0)throw new RuntimeException("VR Initialization failed!");
        OpenVR.create(token);
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        VRSystem_GetRecommendedRenderTargetSize(w, h);
        vrWidth = w.get(0);
        vrHeight = h.get(0);
        leftEyeBuffer = new Framebuffer(vrWidth, vrHeight);
        rightEyeBuffer = new Framebuffer(vrWidth, vrHeight);
    }
    private final Matrix4f spectatorViewMatrix = new Matrix4f().setTranslation(0, 0, -5);
    private final Matrix4f spectatorProjectionMatrix = new Matrix4f();
    @Override
    public void render(double deltaTime){
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        Renderer.view(spectatorViewMatrix);
        Renderer.projection(spectatorProjectionMatrix);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Renderer.fillRect(0, 0, DizzyEngine.screenSize.x, DizzyEngine.screenSize.y, leftEyeBuffer.texture);//draw screen buffer to renderbuffer
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        TrackedDevicePose.Buffer tdpbl = TrackedDevicePose.create(k_unMaxTrackedDeviceCount);
        TrackedDevicePose.Buffer tdpbr = TrackedDevicePose.create(k_unMaxTrackedDeviceCount);
        VRCompositor_WaitGetPoses(tdpbl, tdpbr);
        //TODO process VR input events
        //prepare all the matrices
        Matrix4f[] projectionMatrices = new Matrix4f[2];
        for(int i = 0; i<2; i++){
            projectionMatrices[i] = MathUtil.convertHmdMatrix(VRSystem_GetProjectionMatrix(i, .01f, 1000f, HmdMatrix44.create())).transpose();
        }
        Matrix4f[] eyeMatrices = new Matrix4f[2];
        TrackedDevicePose[] hmdPoses = new TrackedDevicePose[]{
            tdpbl.get(k_unTrackedDeviceIndex_Hmd),
            tdpbr.get(k_unTrackedDeviceIndex_Hmd)
        };
        Matrix4f[] headPoses = new Matrix4f[2];
        for(int i = 0; i<2; i++){
            headPoses[i] = new Matrix4f(MathUtil.convertHmdMatrix(hmdPoses[i].mDeviceToAbsoluteTracking())).invert();
            eyeMatrices[i] = new Matrix4f(MathUtil.convertHmdMatrix(VRSystem_GetEyeToHeadTransform(i, HmdMatrix34.create()))).invert().mul(headPoses[i]);
        }
        //render left eye
        leftEyeBuffer.bind();
        glViewport(0, 0, vrWidth, vrHeight);
        //clear buffers
        glClearColor(0, 0, 0, 0);//blank color for depth/stencil
        glStencilMask(0xff);//write to stencil
        glClear(GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);//clear depth & stencil buffers
        glStencilMask(0x00);//don't write to stencil
        glClearColor(0f, 0f, 0f, 1f);//background for color buffer
        glClear(GL_COLOR_BUFFER_BIT);//clear color buffer
        Renderer.projection(projectionMatrices[0]);
        Renderer.view(eyeMatrices[0]);
        //TODO VR render!
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Renderer.setColor(1f, 1f, 1f, 1f);
        Texture textureLeft = Texture.create();
        textureLeft.set(leftEyeBuffer.texture, ETextureType_TextureType_OpenGL, EColorSpace_ColorSpace_Auto);
        int left = VRCompositor_Submit(EVREye_Eye_Left, textureLeft, null, EVRSubmitFlags_Submit_Default);
        //TODO error handling
        //render right eye
        rightEyeBuffer.bind();
        glViewport(0, 0, vrWidth, vrHeight);
        //clear buffers
        glClearColor(0, 0, 0, 0);//blank color for depth/stencil
        glStencilMask(0xff);//write to stencil
        glClear(GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);//clear depth & stencil buffers
        glStencilMask(0x00);//don't write to stencil
        glClearColor(0f, 0f, 0f, 1f);//background for color buffer
        glClear(GL_COLOR_BUFFER_BIT);//clear color buffer
        Renderer.projection(projectionMatrices[1]);
        Renderer.view(eyeMatrices[1]);
        //TODO VR render!
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Renderer.setColor(1f, 1f, 1f, 1f);
        Texture textureRight = Texture.create();
        textureRight.set(rightEyeBuffer.texture, ETextureType_TextureType_OpenGL, EColorSpace_ColorSpace_Auto);
        int right = VRCompositor_Submit(EVREye_Eye_Right, textureRight, null, EVRSubmitFlags_Submit_Default);
        //TODO error handling
    }
    @Override
    public void cleanup(){
        leftEyeBuffer.destroy();
        rightEyeBuffer.destroy();
        OpenVR.destroy();
        VR_ShutdownInternal();
    }
    @Override
    protected void onScreenSize(Vector2i screenSize){
        spectatorProjectionMatrix.setOrtho(0, screenSize.x, screenSize.y, 0, 0.1f, 10f);
    }
}
