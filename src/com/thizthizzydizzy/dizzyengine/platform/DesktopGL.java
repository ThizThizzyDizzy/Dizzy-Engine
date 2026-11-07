package com.thizthizzydizzy.dizzyengine.platform;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
final class DesktopGL implements GLBackend {
    public int glGenVertexArrays() { return GL30.glGenVertexArrays(); }
    public int glGenBuffers() { return GL15.glGenBuffers(); }
    public int glGenTextures() { return GL11.glGenTextures(); }
    public int glGenFramebuffers() { return GL30.glGenFramebuffers(); }
    public int glGenRenderbuffers() { return GL30.glGenRenderbuffers(); }
    public void glBindVertexArray(int array) { GL30.glBindVertexArray(array); }
    public void glBindBuffer(int target, int buffer) { GL15.glBindBuffer(target, buffer); }
    public void glBindBufferBase(int target, int index, int buffer) { GL30.glBindBufferBase(target, index, buffer); }
    public void glBufferData(int target, float[] data, int usage) { GL15.glBufferData(target, data, usage); }
    public void glBufferData(int target, int[] data, int usage) { GL15.glBufferData(target, data, usage); }
    public void glBufferData(int target, long size, int usage) { GL15.glBufferData(target, size, usage); }
    public void glBufferSubData(int target, long offset, FloatBuffer data) { GL15.glBufferSubData(target, offset, data); }
    public void glEnableVertexAttribArray(int index) { GL20.glEnableVertexAttribArray(index); }
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) { GL20.glVertexAttribPointer(index, size, type, normalized, stride, pointer); }
    public void glDrawElements(int mode, int count, int type, long indices) { GL11.glDrawElements(mode, count, type, indices); }
    public void glDrawElementsInstanced(int mode, int count, int type, long indices, int instances) { GL31.glDrawElementsInstanced(mode, count, type, indices, instances); }
    public void glDeleteBuffers(int buffer) { GL15.glDeleteBuffers(buffer); }
    public void glDeleteVertexArrays(int array) { GL30.glDeleteVertexArrays(array); }
    public void glDeleteTextures(int texture) { GL11.glDeleteTextures(texture); }
    public void glDeleteFramebuffers(int framebuffer) { GL30.glDeleteFramebuffers(framebuffer); }
    public void glDeleteRenderbuffers(int renderbuffer) { GL30.glDeleteRenderbuffers(renderbuffer); }
    public void glBindTexture(int target, int texture) { GL11.glBindTexture(target, texture); }
    public void glTexParameteri(int target, int pname, int param) { GL11.glTexParameteri(target, pname, param); }
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) { GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels); }
    public void glGenerateMipmap(int target) { GL30.glGenerateMipmap(target); }
    public void glActiveTexture(int texture) { GL13.glActiveTexture(texture); }
    public void glBindFramebuffer(int target, int framebuffer) { GL30.glBindFramebuffer(target, framebuffer); }
    public void glBindRenderbuffer(int target, int renderbuffer) { GL30.glBindRenderbuffer(target, renderbuffer); }
    public void glRenderbufferStorage(int target, int internalformat, int width, int height) { GL30.glRenderbufferStorage(target, internalformat, width, height); }
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) { GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level); }
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) { GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer); }
    public int glCheckFramebufferStatus(int target) { return GL30.glCheckFramebufferStatus(target); }
    public void glViewport(int x, int y, int width, int height) { GL11.glViewport(x, y, width, height); }
    public void glClearColor(float red, float green, float blue, float alpha) { GL11.glClearColor(red, green, blue, alpha); }
    public void glClear(int mask) { GL11.glClear(mask); }
    public void glEnable(int cap) { GL11.glEnable(cap); }
    public void glDisable(int cap) { GL11.glDisable(cap); }
    public boolean glIsEnabled(int cap) { return GL11.glIsEnabled(cap); }
    public void glBlendFunc(int sfactor, int dfactor) { GL11.glBlendFunc(sfactor, dfactor); }
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) { GL11.glColorMask(red, green, blue, alpha); }
    public void glDepthMask(boolean flag) { GL11.glDepthMask(flag); }
    public void glStencilMask(int mask) { GL11.glStencilMask(mask); }
    public void glStencilFunc(int func, int ref, int mask) { GL11.glStencilFunc(func, ref, mask); }
    public void glStencilOp(int sfail, int dpfail, int dppass) { GL11.glStencilOp(sfail, dpfail, dppass); }
    public void glScissor(int x, int y, int width, int height) { GL11.glScissor(x, y, width, height); }
    public int glGetInteger(int pname) { return GL11.glGetInteger(pname); }
    public void glGetIntegerv(int pname, int[] params) { GL11.glGetIntegerv(pname, params); }
    public void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) { GL11.glReadPixels(x, y, width, height, format, type, pixels); }
    public int glCreateShader(int type) { return GL20.glCreateShader(type); }
    public void glShaderSource(int shader, CharSequence string) { GL20.glShaderSource(shader, string); }
    public void glCompileShader(int shader) { GL20.glCompileShader(shader); }
    public void glGetShaderiv(int shader, int pname, int[] params) { GL20.glGetShaderiv(shader, pname, params); }
    public String glGetShaderInfoLog(int shader) { return GL20.glGetShaderInfoLog(shader); }
    public int glCreateProgram() { return GL20.glCreateProgram(); }
    public void glAttachShader(int program, int shader) { GL20.glAttachShader(program, shader); }
    public void glLinkProgram(int program) { GL20.glLinkProgram(program); }
    public void glGetProgramiv(int program, int pname, int[] params) { GL20.glGetProgramiv(program, pname, params); }
    public String glGetProgramInfoLog(int program) { return GL20.glGetProgramInfoLog(program); }
    public void glUseProgram(int program) { GL20.glUseProgram(program); }
    public int glGetUniformLocation(int program, CharSequence name) { return GL20.glGetUniformLocation(program, name); }
    public void glUniform1f(int location, float v0) { GL20.glUniform1f(location, v0); }
    public void glUniform2f(int location, float v0, float v1) { GL20.glUniform2f(location, v0, v1); }
    public void glUniform3f(int location, float v0, float v1, float v2) { GL20.glUniform3f(location, v0, v1, v2); }
    public void glUniform4f(int location, float v0, float v1, float v2, float v3) { GL20.glUniform4f(location, v0, v1, v2, v3); }
    public void glUniform1i(int location, int v0) { GL20.glUniform1i(location, v0); }
    public void glUniformMatrix4fv(int location, boolean transpose, float[] value) { GL20.glUniformMatrix4fv(location, transpose, value); }
    public void glDeleteShader(int shader) { GL20.glDeleteShader(shader); }
    public void glDeleteProgram(int program) { GL20.glDeleteProgram(program); }
}
