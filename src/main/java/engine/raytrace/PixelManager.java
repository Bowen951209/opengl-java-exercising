package engine.raytrace;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL43.*;

/*
 * This class manages what pixels should be rendered, what should not when ray tracing.
 * */
public class PixelManager {
    private static final int STATE_NO_DRAW = 0, STATE_DO_DRAW = 1, STATE_DRAWN = 2;
    private final List<Integer> turnOnOrder = new ArrayList<>();
    private int numTurnedOnPixels;

    private final int ssboID;
    private IntBuffer pixelListBuffer;
    private final Set<Boolean> stateSet = new HashSet<>();
    private boolean isAllDrawn = false;

    public PixelManager(int usingIndex) {
        ssboID = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboID);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, usingIndex, ssboID);
    }

    /**
     * This method should be called when the frame buffer size update.
     */
    public void resizeTurnOnOrder(int size) {
        turnOnOrder.clear();
        numTurnedOnPixels = 0;

        for (int i = 0; i < size; i++) {
            turnOnOrder.add(i);
        }

        Collections.shuffle(turnOnOrder);
    }

    public void turnOn(int number) {
        if (!isAllDrawn()) {
            pixelListBuffer.position(pixelListBuffer.capacity());

            for (int i = 0; i < number; i++) {
                int randIndex = turnOnOrder.get(numTurnedOnPixels);
                pixelListBuffer.put(randIndex, STATE_DO_DRAW);

                numTurnedOnPixels++;
                if (numTurnedOnPixels == turnOnOrder.size()) {
                    break;
                }
            }
            pixelListBuffer.flip();
        }
    }

    private boolean isAllDrawn() {
        if (!isAllDrawn) {
            if (numTurnedOnPixels == turnOnOrder.size()) {
                isAllDrawn = true;
            }
            return isAllDrawn;
        } else {
            return true;
        }
    }

    /**
     * This method should be called whenever you want to refresh.
     */
    public void fill(int size) {
        numTurnedOnPixels = 0;
        isAllDrawn = false;
        pixelListBuffer = BufferUtils.createIntBuffer(size);
        for (int i = 0; i < pixelListBuffer.capacity(); i++) {
            pixelListBuffer.put(STATE_NO_DRAW);
        }
    }

    public void putPixelArrayToSSBO() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboID);
        glBufferData(GL_SHADER_STORAGE_BUFFER, pixelListBuffer, GL_DYNAMIC_DRAW);
    }

    /**
     * This method should be called every time after executing compute shader.
     * */
    public void updateBuffer() {
        for (int i = 0; i < pixelListBuffer.capacity(); i++) {
            if (pixelListBuffer.get(i) == STATE_DO_DRAW) {
                pixelListBuffer.put(i, STATE_DRAWN);
            }
        }
    }
}
