package engine.sceneComponents;

import engine.exceptions.InvalidPatternException;
import engine.util.NoiseGenerator;
import engine.util.Timer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL43.*;

public class Texture3D extends Thread {
    private int textureID;
    private final int usingUnit;
    private final int textureWidth = 256;
    private final int textureHeight = 256;
    private final int textureDepth = 256;
    private int zoom = 1;

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    private ByteBuffer data;
    private final String pattern;

    private final NoiseGenerator noiseGenerator = new NoiseGenerator();  // This would be random seed.

    public Texture3D(int usingUnit, String pattern) {
        this.usingUnit = usingUnit;
        this.pattern = pattern;
    }

    @Override
    public void run() {
        fillDataArray(); // This take quite long.
    }

    public void end() {
        try {
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        loadToTexture();
    }

    // TODO: 2023/7/20 convert to static
    private void fillDataArray() {
        Timer timer = new Timer();
        timer.start();
        data = BufferUtils.createByteBuffer(textureWidth * textureHeight * textureDepth * 4);

        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
                    switch (pattern.toUpperCase()) {
                        case "STRIPE" -> fillStripe(y);
                        case "SMOOTH" -> fillSmoothNoise(x, y, z);
                        case "MIX-SMOOTH" -> fillSmoothNoise(x, y, z, zoom); // With levels mixed
                        default -> throw new InvalidPatternException();
                    }
                }
            }
        }
        data.flip();
        timer.end("\"" + pattern + "\" fills data in array takes: ");
    }

    private void fillStripe(int y) {
        if ((y / 10) % 2 == 0) {
            // yellow
            data.put((byte) 255); // r
            data.put((byte) 255); // g
            data.put((byte) 0);// b
            data.put((byte) 255); // a
        } else {
            // blue
            data.put((byte) 0); // r
            data.put((byte) 0); // g
            data.put((byte) 255);// b
            data.put((byte) 255); // a
        }
    }


    // This is for multi-level mixed;
    private void fillSmoothNoise(float x, float y, float z, int zoom) {
        float mappedValue = (float) noiseGenerator.noise(x / zoom, y / zoom, z / zoom, zoom);

        data.put((byte) (mappedValue * 255)); // r
        data.put((byte) (mappedValue * 255)); // g
        data.put((byte) (mappedValue * 255));// b
        data.put((byte) 255); // a
    }

    // This is for no multi-level mixed.
    private void fillSmoothNoise(float x, float y, float z) {
        float mappedValue = (float) noiseGenerator.noise(x / zoom, y / zoom, z / zoom, 1);

        data.put((byte) (mappedValue * 255)); // r
        data.put((byte) (mappedValue * 255)); // g
        data.put((byte) (mappedValue * 255));// b
        data.put((byte) 255); // a
    }


    private void loadToTexture() {
        textureID = glGenTextures();
        bind();
        glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, textureWidth, textureHeight, textureDepth);
        glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, textureWidth, textureHeight, textureDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, data);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0 + usingUnit);
        glBindTexture(GL_TEXTURE_3D, textureID);
    }
}
