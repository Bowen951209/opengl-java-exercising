package utilities;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class ValuesContainer {
    public static final Vector3f VEC3_FOR_UTILS = new Vector3f();
    public static final Matrix4f MAT4_FOR_UTILS = new Matrix4f();
    public static final FloatBuffer VALS_OF_16 = BufferUtils.createFloatBuffer(16);
}