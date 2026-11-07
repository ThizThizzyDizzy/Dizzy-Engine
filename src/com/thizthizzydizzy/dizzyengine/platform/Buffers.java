package com.thizthizzydizzy.dizzyengine.platform;
import java.nio.*;
/** Direct native-order buffers without a native-library dependency. */
public final class Buffers {
    private Buffers() {}
    public static ByteBuffer createByteBuffer(int size) { return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()); }
    public static IntBuffer createIntBuffer(int size) { return createByteBuffer(Math.multiplyExact(size, 4)).asIntBuffer(); }
    public static FloatBuffer createFloatBuffer(int size) { return createByteBuffer(Math.multiplyExact(size, 4)).asFloatBuffer(); }
    public static DoubleBuffer createDoubleBuffer(int size) { return createByteBuffer(Math.multiplyExact(size, 8)).asDoubleBuffer(); }
}
