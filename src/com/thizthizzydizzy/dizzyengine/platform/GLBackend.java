package com.thizthizzydizzy.dizzyengine.platform;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
/** Optional graphics backend. Unsupported operations fail explicitly. */
public interface GLBackend {
    default int glGenVertexArrays() { throw new UnsupportedOperationException("glGenVertexArrays"); }
    default int glGenBuffers() { throw new UnsupportedOperationException("glGenBuffers"); }
    default int glGenTextures() { throw new UnsupportedOperationException("glGenTextures"); }
    default int glGenFramebuffers() { throw new UnsupportedOperationException("glGenFramebuffers"); }
    default int glGenRenderbuffers() { throw new UnsupportedOperationException("glGenRenderbuffers"); }
    default void glBindVertexArray(int array) { throw new UnsupportedOperationException("glBindVertexArray"); }
    default void glBindBuffer(int target, int buffer) { throw new UnsupportedOperationException("glBindBuffer"); }
    default void glBindBufferBase(int target, int index, int buffer) { throw new UnsupportedOperationException("glBindBufferBase"); }
    default void glBufferData(int target, float[] data, int usage) { throw new UnsupportedOperationException("glBufferData"); }
    default void glBufferData(int target, int[] data, int usage) { throw new UnsupportedOperationException("glBufferData"); }
    default void glBufferData(int target, long size, int usage) { throw new UnsupportedOperationException("glBufferData"); }
    default void glBufferSubData(int target, long offset, FloatBuffer data) { throw new UnsupportedOperationException("glBufferSubData"); }
    default void glEnableVertexAttribArray(int index) { throw new UnsupportedOperationException("glEnableVertexAttribArray"); }
    default void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) { throw new UnsupportedOperationException("glVertexAttribPointer"); }
    default void glDrawElements(int mode, int count, int type, long indices) { throw new UnsupportedOperationException("glDrawElements"); }
    default void glDrawElementsInstanced(int mode, int count, int type, long indices, int instances) { throw new UnsupportedOperationException("glDrawElementsInstanced"); }
    default void glDeleteBuffers(int buffer) { throw new UnsupportedOperationException("glDeleteBuffers"); }
    default void glDeleteVertexArrays(int array) { throw new UnsupportedOperationException("glDeleteVertexArrays"); }
    default void glDeleteTextures(int texture) { throw new UnsupportedOperationException("glDeleteTextures"); }
    default void glDeleteFramebuffers(int framebuffer) { throw new UnsupportedOperationException("glDeleteFramebuffers"); }
    default void glDeleteRenderbuffers(int renderbuffer) { throw new UnsupportedOperationException("glDeleteRenderbuffers"); }
    default void glBindTexture(int target, int texture) { throw new UnsupportedOperationException("glBindTexture"); }
    default void glTexParameteri(int target, int pname, int param) { throw new UnsupportedOperationException("glTexParameteri"); }
    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) { throw new UnsupportedOperationException("glTexImage2D"); }
    default void glGenerateMipmap(int target) { throw new UnsupportedOperationException("glGenerateMipmap"); }
    default void glActiveTexture(int texture) { throw new UnsupportedOperationException("glActiveTexture"); }
    default void glBindFramebuffer(int target, int framebuffer) { throw new UnsupportedOperationException("glBindFramebuffer"); }
    default void glBindRenderbuffer(int target, int renderbuffer) { throw new UnsupportedOperationException("glBindRenderbuffer"); }
    default void glRenderbufferStorage(int target, int internalformat, int width, int height) { throw new UnsupportedOperationException("glRenderbufferStorage"); }
    default void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) { throw new UnsupportedOperationException("glFramebufferTexture2D"); }
    default void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) { throw new UnsupportedOperationException("glFramebufferRenderbuffer"); }
    default int glCheckFramebufferStatus(int target) { throw new UnsupportedOperationException("glCheckFramebufferStatus"); }
    default void glViewport(int x, int y, int width, int height) { throw new UnsupportedOperationException("glViewport"); }
    default void glClearColor(float red, float green, float blue, float alpha) { throw new UnsupportedOperationException("glClearColor"); }
    default void glClear(int mask) { throw new UnsupportedOperationException("glClear"); }
    default void glEnable(int cap) { throw new UnsupportedOperationException("glEnable"); }
    default void glDisable(int cap) { throw new UnsupportedOperationException("glDisable"); }
    default boolean glIsEnabled(int cap) { throw new UnsupportedOperationException("glIsEnabled"); }
    default void glBlendFunc(int sfactor, int dfactor) { throw new UnsupportedOperationException("glBlendFunc"); }
    default void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) { throw new UnsupportedOperationException("glColorMask"); }
    default void glDepthMask(boolean flag) { throw new UnsupportedOperationException("glDepthMask"); }
    default void glStencilMask(int mask) { throw new UnsupportedOperationException("glStencilMask"); }
    default void glStencilFunc(int func, int ref, int mask) { throw new UnsupportedOperationException("glStencilFunc"); }
    default void glStencilOp(int sfail, int dpfail, int dppass) { throw new UnsupportedOperationException("glStencilOp"); }
    default void glScissor(int x, int y, int width, int height) { throw new UnsupportedOperationException("glScissor"); }
    default int glGetInteger(int pname) { throw new UnsupportedOperationException("glGetInteger"); }
    default void glGetIntegerv(int pname, int[] params) { throw new UnsupportedOperationException("glGetIntegerv"); }
    default void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) { throw new UnsupportedOperationException("glReadPixels"); }
    default int glCreateShader(int type) { throw new UnsupportedOperationException("glCreateShader"); }
    default void glShaderSource(int shader, CharSequence string) { throw new UnsupportedOperationException("glShaderSource"); }
    default void glCompileShader(int shader) { throw new UnsupportedOperationException("glCompileShader"); }
    default void glGetShaderiv(int shader, int pname, int[] params) { throw new UnsupportedOperationException("glGetShaderiv"); }
    default String glGetShaderInfoLog(int shader) { throw new UnsupportedOperationException("glGetShaderInfoLog"); }
    default int glCreateProgram() { throw new UnsupportedOperationException("glCreateProgram"); }
    default void glAttachShader(int program, int shader) { throw new UnsupportedOperationException("glAttachShader"); }
    default void glLinkProgram(int program) { throw new UnsupportedOperationException("glLinkProgram"); }
    default void glGetProgramiv(int program, int pname, int[] params) { throw new UnsupportedOperationException("glGetProgramiv"); }
    default String glGetProgramInfoLog(int program) { throw new UnsupportedOperationException("glGetProgramInfoLog"); }
    default void glUseProgram(int program) { throw new UnsupportedOperationException("glUseProgram"); }
    default int glGetUniformLocation(int program, CharSequence name) { throw new UnsupportedOperationException("glGetUniformLocation"); }
    default void glUniform1f(int location, float v0) { throw new UnsupportedOperationException("glUniform1f"); }
    default void glUniform2f(int location, float v0, float v1) { throw new UnsupportedOperationException("glUniform2f"); }
    default void glUniform3f(int location, float v0, float v1, float v2) { throw new UnsupportedOperationException("glUniform3f"); }
    default void glUniform4f(int location, float v0, float v1, float v2, float v3) { throw new UnsupportedOperationException("glUniform4f"); }
    default void glUniform1i(int location, int v0) { throw new UnsupportedOperationException("glUniform1i"); }
    default void glUniformMatrix4fv(int location, boolean transpose, float[] value) { throw new UnsupportedOperationException("glUniformMatrix4fv"); }
    default void glDeleteShader(int shader) { throw new UnsupportedOperationException("glDeleteShader"); }
    default void glDeleteProgram(int program) { throw new UnsupportedOperationException("glDeleteProgram"); }
}
