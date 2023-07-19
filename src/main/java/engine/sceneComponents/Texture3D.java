package engine.sceneComponents;

import engine.exceptions.InvalidPatternException;
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

    private final float[][][] texture3DPattern = new float[textureWidth][textureHeight][textureDepth];
    private ByteBuffer data;
    private final String pattern;

    public Texture3D(int usingUnit, String pattern) {
        this.usingUnit = usingUnit;
        this.pattern = pattern;
    }

    @Override
    public void run() {
        switch (pattern.toUpperCase()) {
            case "STRIPE" -> generatePattern();
            case "NOISE" -> generateNoise();
            default -> throw new InvalidPatternException("Unsupported pattern passed in");
        }
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

    private void generatePattern() {
        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
                    if ((y / 10) % 2 == 0)
                        texture3DPattern[x][y][z] = 0.0f;
                    else
                        texture3DPattern[x][y][z] = 1.0f;
                }
            }
        }
    }

    private void generateNoise() {
        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
                    texture3DPattern[x][y][z] = (float) Math.random();
                }
            }
        }
    }

    private void fillDataArray() {
        Timer timer = new Timer();
        timer.start();
        data = BufferUtils.createByteBuffer(textureWidth * textureHeight * textureDepth * 4);

        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
                    switch (pattern.toUpperCase()) {
                        case "STRIPE" -> fillStripe(x, y, z);
                        case "NOISE" -> fillNoise(x, y, z);
                    }
                }
            }
        }
        data.flip();
        timer.end("Fill data in array for 3D texture takes: ");
    }

    private void fillStripe(int x, int y, int z) {
        if (texture3DPattern[x][y][z] == 1.0) {
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

    public void fillNoise(int x, int y, int z) {
        float mappedValue = texture3DPattern[x / zoom][y / zoom][z / zoom];

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
