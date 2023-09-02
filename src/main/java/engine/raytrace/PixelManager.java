package engine.raytrace;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL43.*;

/*
 * This class manages what pixels should be rendered, what should not when ray tracing.
 * */
public class PixelManager {
    private static final int STATE_NO_DRAW = 0;
    private static final int STATE_DO_DRAW = 1;

    private final int ssboID;
    private final Random random;
    private IntBuffer pixelListBuffer;

    public PixelManager(int usingIndex) {
        random = new Random();
        ssboID = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboID);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, usingIndex, ssboID);
    }

    public void turnOn(int number) {
        pixelListBuffer.position(pixelListBuffer.capacity());

        for (int i = 0; i < number; i++) {
            int randIndex = random.nextInt(pixelListBuffer.capacity());
            if (pixelListBuffer.get(randIndex) == STATE_NO_DRAW) {
                pixelListBuffer.put(randIndex, STATE_DO_DRAW);
            }
        }
        pixelListBuffer.flip();
    }

    public void fill(int size) {
        pixelListBuffer = BufferUtils.createIntBuffer(size);
        for (int i = 0; i < pixelListBuffer.capacity(); i++) {
            pixelListBuffer.put(STATE_NO_DRAW);
        }
    }

    public void putPixelArrayToSSBO() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboID);
        glBufferData(GL_SHADER_STORAGE_BUFFER, pixelListBuffer, GL_DYNAMIC_DRAW);
    }

    public void getDataBack() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboID);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, pixelListBuffer);
    }
}
