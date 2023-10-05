package engine.raytrace;

import engine.ShaderProgram;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL43.*;

/*
 * This class manages what pixels should be rendered, what should not when ray tracing.
 * */
public class PixelManager {
    private final int ssbo, numDispatchCall;
    private final String numXUniformName, numRenderedPixelName;
    private final ShaderProgram shader;

    private int numX, numY, numRenderedPixel;

    public int getNumDispatchCall() {
        return numDispatchCall;
    }

    public void zeroNumRendered() {
        this.numRenderedPixel = -numDispatchCall;
    }

    public void addNumRendered(int number) {
        if (numRenderedPixel + number >= numX * numY) {
            numRenderedPixel = numX * numY;
            return;
        } else
            this.numRenderedPixel += number;
        shader.putUniform1i(numRenderedPixelName, numRenderedPixel);
    }

    public PixelManager(ShaderProgram shader, String numXUniformName, String numRenderedPixelName,
                        int ssboBinding, int numDispatchCall) {
        this.shader = shader;
        this.numXUniformName = numXUniformName;
        this.numRenderedPixelName = numRenderedPixelName;
        this.numDispatchCall = numDispatchCall;
        this.ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ssboBinding, ssbo);
    }

    /**
     * @param buffer The pixel order list IntBuffer.
     */
    public void putListToShader(IntBuffer buffer) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, this.ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
    }

    public void putNumXToShader() {
        shader.putUniform1i(numXUniformName, numX);
    }

    /**
     * Generate X/Y pixels random turn on order.
     * (Should be called when program start up or numX/numY value resized.)
     */
    public IntBuffer generateList(int numX, int numY) {
        this.numX = numX;
        this.numY = numY;
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
        // FIXME: 2023/10/5 2023/10/5 pixel order now goes vertical lines.
        for (int i = 0; i < xList.size(); i++) {
            for (int j = 0; j < yList.size(); j++) {
                buffer.put(xList.get((i + j) % xList.size()));
                buffer.put(yList.get(j));
            }
        }

        buffer.flip();
        return buffer;
    }
}
