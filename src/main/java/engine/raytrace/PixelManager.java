package engine.raytrace;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * This class manages what pixels should be rendered, what should not when ray tracing.
 * */
public class PixelManager {

    public PixelManager(int usingIndex) {
    }

    /**
     * Generate X/Y pixels random turn on order.
     * (Should be called when program start up or numX/numY value resized.)
     */
    public IntBuffer generateList(int numX, int numY) {
        List<Integer> xList = new ArrayList<>();
        List<Integer> yList = new ArrayList<>();

        for (int i = 0; i < numX; i++)
            xList.add(i);
        for (int i = 0; i < numY; i++)
            yList.add(i);

        Collections.shuffle(xList);
        Collections.shuffle(yList);

        // Combine 2 lists and store into buffer.
        IntBuffer buffer = BufferUtils.createIntBuffer(numX * numY * 2);
        for (Integer x : xList) {
            for (Integer y : yList) {
                buffer.put(x);
                buffer.put(y);
            }
        }
        buffer.flip();
        return buffer;
    }
}
