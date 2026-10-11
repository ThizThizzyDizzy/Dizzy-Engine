package com.thizthizzydizzy.dizzyengine.platform;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;
/** GL entry points. LWJGL remains the default unless a host explicitly installs a backend. */
public final class GL {
    private static GLBackend backend;
    private GL() {}
    public static void setBackend(GLBackend replacement) { backend = Objects.requireNonNull(replacement); }
    private static GLBackend backend() { if (backend == null) backend = new DesktopGL(); return backend; }
    public static int glGenVertexArrays() { return backend().glGenVertexArrays(); }
    public static int glGenBuffers() { return backend().glGenBuffers(); }
    public static int glGenTextures() { return backend().glGenTextures(); }
    public static int glGenFramebuffers() { return backend().glGenFramebuffers(); }
    public static int glGenRenderbuffers() { return backend().glGenRenderbuffers(); }
    public static void glBindVertexArray(int array) { backend().glBindVertexArray(array); }
    public static void glBindBuffer(int target, int buffer) { backend().glBindBuffer(target, buffer); }
    public static void glBindBufferBase(int target, int index, int buffer) { backend().glBindBufferBase(target, index, buffer); }
    public static void glBufferData(int target, float[] data, int usage) { backend().glBufferData(target, data, usage); }
    public static void glBufferData(int target, int[] data, int usage) { backend().glBufferData(target, data, usage); }
    public static void glBufferData(int target, long size, int usage) { backend().glBufferData(target, size, usage); }
    public static void glBufferSubData(int target, long offset, FloatBuffer data) { backend().glBufferSubData(target, offset, data); }
    public static void glEnableVertexAttribArray(int index) { backend().glEnableVertexAttribArray(index); }
    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) { backend().glVertexAttribPointer(index, size, type, normalized, stride, pointer); }
    public static void glDrawElements(int mode, int count, int type, long indices) { backend().glDrawElements(mode, count, type, indices); }
    public static void glDrawElementsInstanced(int mode, int count, int type, long indices, int instances) { backend().glDrawElementsInstanced(mode, count, type, indices, instances); }
    public static void glDeleteBuffers(int buffer) { backend().glDeleteBuffers(buffer); }
    public static void glDeleteVertexArrays(int array) { backend().glDeleteVertexArrays(array); }
    public static void glDeleteTextures(int texture) { backend().glDeleteTextures(texture); }
    public static void glDeleteFramebuffers(int framebuffer) { backend().glDeleteFramebuffers(framebuffer); }
    public static void glDeleteRenderbuffers(int renderbuffer) { backend().glDeleteRenderbuffers(renderbuffer); }
    public static void glBindTexture(int target, int texture) { backend().glBindTexture(target, texture); }
    public static void glTexParameteri(int target, int pname, int param) { backend().glTexParameteri(target, pname, param); }
    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) { backend().glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels); }
    public static void glGenerateMipmap(int target) { backend().glGenerateMipmap(target); }
    public static void glActiveTexture(int texture) { backend().glActiveTexture(texture); }
    public static void glBindFramebuffer(int target, int framebuffer) { backend().glBindFramebuffer(target, framebuffer); }
    public static void glBindRenderbuffer(int target, int renderbuffer) { backend().glBindRenderbuffer(target, renderbuffer); }
    public static void glRenderbufferStorage(int target, int internalformat, int width, int height) { backend().glRenderbufferStorage(target, internalformat, width, height); }
    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) { backend().glFramebufferTexture2D(target, attachment, textarget, texture, level); }
    public static void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) { backend().glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer); }
    public static int glCheckFramebufferStatus(int target) { return backend().glCheckFramebufferStatus(target); }
    public static void glViewport(int x, int y, int width, int height) { backend().glViewport(x, y, width, height); }
    public static void glClearColor(float red, float green, float blue, float alpha) { backend().glClearColor(red, green, blue, alpha); }
    public static void glClear(int mask) { backend().glClear(mask); }
    public static void glEnable(int cap) { backend().glEnable(cap); }
    public static void glDisable(int cap) { backend().glDisable(cap); }
    public static boolean glIsEnabled(int cap) { return backend().glIsEnabled(cap); }
    public static void glBlendFunc(int sfactor, int dfactor) { backend().glBlendFunc(sfactor, dfactor); }
    public static void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) { backend().glColorMask(red, green, blue, alpha); }
    public static void glDepthMask(boolean flag) { backend().glDepthMask(flag); }
    public static void glStencilMask(int mask) { backend().glStencilMask(mask); }
    public static void glStencilFunc(int func, int ref, int mask) { backend().glStencilFunc(func, ref, mask); }
    public static void glStencilOp(int sfail, int dpfail, int dppass) { backend().glStencilOp(sfail, dpfail, dppass); }
    public static void glScissor(int x, int y, int width, int height) { backend().glScissor(x, y, width, height); }
    public static int glGetInteger(int pname) { return backend().glGetInteger(pname); }
    public static void glGetIntegerv(int pname, int[] params) { backend().glGetIntegerv(pname, params); }
    public static void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) { backend().glReadPixels(x, y, width, height, format, type, pixels); }
    public static int glCreateShader(int type) { return backend().glCreateShader(type); }
    public static void glShaderSource(int shader, CharSequence string) { backend().glShaderSource(shader, string); }
    public static void glCompileShader(int shader) { backend().glCompileShader(shader); }
    public static void glGetShaderiv(int shader, int pname, int[] params) { backend().glGetShaderiv(shader, pname, params); }
    public static String glGetShaderInfoLog(int shader) { return backend().glGetShaderInfoLog(shader); }
    public static int glCreateProgram() { return backend().glCreateProgram(); }
    public static void glAttachShader(int program, int shader) { backend().glAttachShader(program, shader); }
    public static void glLinkProgram(int program) { backend().glLinkProgram(program); }
    public static void glGetProgramiv(int program, int pname, int[] params) { backend().glGetProgramiv(program, pname, params); }
    public static String glGetProgramInfoLog(int program) { return backend().glGetProgramInfoLog(program); }
    public static void glUseProgram(int program) { backend().glUseProgram(program); }
    public static int glGetUniformLocation(int program, CharSequence name) { return backend().glGetUniformLocation(program, name); }
    public static void glUniform1f(int location, float v0) { backend().glUniform1f(location, v0); }
    public static void glUniform2f(int location, float v0, float v1) { backend().glUniform2f(location, v0, v1); }
    public static void glUniform3f(int location, float v0, float v1, float v2) { backend().glUniform3f(location, v0, v1, v2); }
    public static void glUniform4f(int location, float v0, float v1, float v2, float v3) { backend().glUniform4f(location, v0, v1, v2, v3); }
    public static void glUniform1i(int location, int v0) { backend().glUniform1i(location, v0); }
    public static void glUniformMatrix4fv(int location, boolean transpose, float[] value) { backend().glUniformMatrix4fv(location, transpose, value); }
    public static void glDeleteShader(int shader) { backend().glDeleteShader(shader); }
    public static void glDeleteProgram(int program) { backend().glDeleteProgram(program); }
    public static final int GL_ALWAYS = 519;
    public static final int GL_ARRAY_BUFFER = 34962;
    public static final int GL_BLEND = 3042;
    public static final int GL_COLOR_ATTACHMENT0 = 36064;
    public static final int GL_COLOR_BUFFER_BIT = 16384;
    public static final int GL_COMPILE_STATUS = 35713;
    public static final int GL_CULL_FACE = 2884;
    public static final int GL_DEPTH24_STENCIL8 = 35056;
    public static final int GL_DEPTH_BUFFER_BIT = 256;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = 33306;
    public static final int GL_DEPTH_TEST = 2929;
    public static final int GL_DYNAMIC_DRAW = 35048;
    public static final int GL_ELEMENT_ARRAY_BUFFER = 34963;
    public static final int GL_FLOAT = 5126;
    public static final int GL_FRAGMENT_SHADER = 35632;
    public static final int GL_FRAMEBUFFER = 36160;
    public static final int GL_FRAMEBUFFER_BINDING = 36006;
    public static final int GL_FRAMEBUFFER_COMPLETE = 36053;
    public static final int GL_KEEP = 7680;
    public static final int GL_LINK_STATUS = 35714;
    public static final int GL_MULTISAMPLE = 32925;
    public static final int GL_NEAREST = 9728;
    public static final int GL_NOTEQUAL = 517;
    public static final int GL_ONE = 1;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
    public static final int GL_QUADS = 7;
    public static final int GL_RENDERBUFFER = 36161;
    public static final int GL_REPEAT = 10497;
    public static final int GL_REPLACE = 7681;
    public static final int GL_RGBA = 6408;
    public static final int GL_SCISSOR_TEST = 3089;
    public static final int GL_SHADER_STORAGE_BUFFER = 37074;
    public static final int GL_SRC_ALPHA = 770;
    public static final int GL_STATIC_DRAW = 35044;
    public static final int GL_STENCIL_BUFFER_BIT = 1024;
    public static final int GL_STENCIL_TEST = 2960;
    public static final int GL_STREAM_DRAW = 35040;
    public static final int GL_TEXTURE0 = 33984;
    public static final int GL_TEXTURE_2D = 3553;
    public static final int GL_TEXTURE_MAG_FILTER = 10240;
    public static final int GL_TEXTURE_MIN_FILTER = 10241;
    public static final int GL_TEXTURE_WRAP_S = 10242;
    public static final int GL_TEXTURE_WRAP_T = 10243;
    public static final int GL_TRIANGLES = 4;
    public static final int GL_UNSIGNED_BYTE = 5121;
    public static final int GL_UNSIGNED_INT = 5125;
    public static final int GL_VERTEX_SHADER = 35633;
    public static final int GL_VIEWPORT = 2978;
}
