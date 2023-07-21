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
            case "STRIPE" -> generateStripe();
            case "NOISE", "SMOOTH", "MIX-SMOOTH" -> generateNoise();
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


    // -------------------These generate methods generate data to pattern----------------------------------
    private void generateStripe() {
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
    // -----------------------------------------------------------------------------------------------------


    // TODO: 2023/7/20 convert to static
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
                        case "SMOOTH" -> fillSmoothNoise(x, y, z);
                        case "MIX-SMOOTH" -> fillMixedLevelsSmoothNoise(x, y, z);
                    }
                }
            }
        }
        data.flip();
        timer.end("\"" + pattern + "\" fills data in array takes: ");
    }


    // --------------Convert texture3DPattern to 1D buffer---------------
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

    private void fillNoise(int x, int y, int z) {
        float mappedValue = texture3DPattern[x / zoom][y / zoom][z / zoom];

        data.put((byte) (mappedValue * 255)); // r
        data.put((byte) (mappedValue * 255)); // g
        data.put((byte) (mappedValue * 255));// b
        data.put((byte) 255); // a
    }

    private void fillSmoothNoise(float x, float y, float z) {
        float mappedValue = smoothNoise(x / zoom, y / zoom, z / zoom);

        data.put((byte) (mappedValue * 255)); // r
        data.put((byte) (mappedValue * 255)); // g
        data.put((byte) (mappedValue * 255));// b
        data.put((byte) 255); // a
    }

    private void fillMixedLevelsSmoothNoise(float x, float y, float z) {
        int maxZoom = 32;
        float mappedValue = mixedZoomLevels(x, y, z, maxZoom);

        data.put((byte) (mappedValue * 255)); // r
        data.put((byte) (mappedValue * 255)); // g
        data.put((byte) (mappedValue * 255));// b
        data.put((byte) 255); // a
    }

    // -----------------This will use the texture3DPattern and calculate to smooth pattern------------------
    private float smoothNoise(float x1, float y1, float z1) {
        //get fractional part of x, y, and z
        float fractX = x1 - (int) x1;
        float fractY = y1 - (int) y1;
        float fractZ = z1 - (int) z1;

        //neighbor values
        float x2 = x1 - 1;
        if (x2 < 0) x2 = (float) (Math.round(textureWidth / (float) zoom) - 1.0);
        float y2 = y1 - 1;
        if (y2 < 0) y2 = (float) (Math.round(textureHeight / (float) zoom) - 1.0);
        float z2 = z1 - 1;
        if (z2 < 0) z2 = (float) (Math.round(textureDepth / (float) zoom) - 1.0);

        //smooth the noise by interpolating
        float value = 0.0f;
        value += fractX * fractY * fractZ * texture3DPattern[(int) x1][(int) y1][(int) z1];
        value += (1.0 - fractX) * fractY * fractZ * texture3DPattern[(int) x2][(int) y1][(int) z1];
        value += fractX * (1.0 - fractY) * fractZ * texture3DPattern[(int) x1][(int) y2][(int) z1];
        value += (1.0 - fractX) * (1.0 - fractY) * fractZ * texture3DPattern[(int) x2][(int) y2][(int) z1];

        value += fractX * fractY * (1.0 - fractZ) * texture3DPattern[(int) x1][(int) y1][(int) z2];
        value += (1.0 - fractX) * fractY * (1.0 - fractZ) * texture3DPattern[(int) x2][(int) y1][(int) z2];
        value += fractX * (1.0 - fractY) * (1.0 - fractZ) * texture3DPattern[(int) x1][(int) y2][(int) z2];
        value += (1.0 - fractX) * (1.0 - fractY) * (1.0 - fractZ) * texture3DPattern[(int) x2][(int) y2][(int) z2];

        return value;
    }

    private float mixedZoomLevels(float x, float y, float z, float maxZoom) {
        float sum = 0f;
        float zoom = maxZoom;
        for (float i = maxZoom; i > this.zoom; i /= 2) {
            sum += smoothNoise(x / zoom, y / zoom, z / zoom) ;
            zoom /= 2f;
        }
        sum = 128f * sum / maxZoom; // make sure sum < 256 when maxZoom >= 64
        return sum;
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
