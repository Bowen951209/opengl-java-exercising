package utilities;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class ValuesContainer {
    public static final Vector3f VEC3_FOR_UTILS = new Vector3f();
    public static final FloatBuffer VALS_OF_16 = BufferUtils.createFloatBuffer(16);

    public static void printFloatBuffer(FloatBuffer buffer) {
        System.out.print("{");
        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.print(buffer.get(i));
            if (i == buffer.capacity() - 1) {
                break;
            }
            System.out.print(", ");
        }
        System.out.println("}");
    }
}
