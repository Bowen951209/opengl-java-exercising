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
        System.out.println("\"" + pattern + "\" thread start.");
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

    private void fillDataArray() {
        Timer timer = new Timer();
        timer.start();
        data = BufferUtils.createByteBuffer(textureWidth * textureHeight * textureDepth * 4);

        switch (pattern.toUpperCase()) {
            case "STRIPE" -> fillStripe();
            case "SMOOTH" -> fillSmoothNoise(this.zoom);
            case "MIX-SMOOTH" -> fillSmoothNoiseLevelMixed(this.zoom);
//            case "MARBLE" -> fillMarble();
            default -> throw new InvalidPatternException();
        }
        data.flip();
        timer.end("\"" + pattern + "\" fills data in array takes: ");
    }

    private void fillStripe() {
        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
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
            }
        }
    }


    // This is for multi-level mixed;
    private void fillSmoothNoiseLevelMixed(int zoom) {
        noiseGenerator.levelMixedNoise(data, textureWidth, textureHeight, textureDepth, zoom, 1);
    }

    // This is for no multi-level mixed.
    private void fillSmoothNoise(int zoom) {
        noiseGenerator.levelMixedNoise(data, textureWidth, textureHeight, textureDepth, zoom, zoom);
    }

//    private void fillMarble() {
//        final double veinFrequency = 10.0;
//        final double turbPower = 15.0;
//        final int maxZoom = 32;
//
//        for (double x = 0; x < textureWidth; x++) {
//            for (double y = 0; y < textureHeight; y++) {
//                for (double z = 0; z < textureDepth; z++) {
//                    double xyzValue = (float) x / textureWidth + (float) y / textureHeight + (float) z / textureDepth
//                            + turbPower * noiseGenerator.levelMixedNoise(x, y, z, maxZoom) / 256.0;
//
//                    double sineValue = logistic(Math.abs(Math.sin(xyzValue * 3.14159 * veinFrequency)));
//                    sineValue = Math.max(-1.0, Math.min(sineValue * 1.25 - 0.20, 1.0));
//
//                    Color c = new Color((float) sineValue,
//                            (float) Math.min(sineValue * 1.5 - 0.25, 1.0),
//                            (float) sineValue);
//
//                    data.put((byte) (c.getRed())); // r
//                    data.put((byte) (c.getGreen())); // g
//                    data.put((byte) (c.getBlue()));// b
//                    data.put((byte) 255); // a
//                }
//            }
//        }
//    }

    private double logistic(double x) {
        double k = 3.0;
        return (1.0 / (1.0 + Math.pow(2.718, -k * x)));
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
