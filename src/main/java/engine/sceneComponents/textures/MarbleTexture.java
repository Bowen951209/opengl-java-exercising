package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

import java.awt.*;

public class MarbleTexture extends Texture3D{
    private final NoiseGenerator noiseGenerator;
    private static final double VEIN_FREQUENCY = 1.0;
    private static final double TURB_POWER = 5.2;

    public MarbleTexture(int usingUnit) {
        super(usingUnit);
        this.noiseGenerator = new NoiseGenerator();
    }

    @Override
    protected void fillDataArray() {
        fillMarble();
    }

    private void fillMarble() {
        // generate data into buffer for coming up usage.
        noiseGenerator.turbulence(data, textureWidth, textureHeight, textureDepth, zoom, 1);

        int index = 0;
        for (double x = 0; x < textureWidth; x++) {
            for (double y = 0; y < textureHeight; y++) {
                for (double z = 0; z < textureDepth; z++) {
                    byte noiseValue = data.get(index);

                    double xyzValue = x / textureWidth + y / textureHeight + z / textureDepth
                            + TURB_POWER * noiseValue / 256.0;

                    double sineValue = logistic(Math.abs(Math.sin(xyzValue * 3.14159 * VEIN_FREQUENCY)));
                    sineValue = Math.max(-1.0, Math.min(sineValue * 1.25 - 0.20, 1.0));

                    Color c = new Color((float) sineValue,
                            (float) Math.min(sineValue * 1.5 - 0.25, 1.0),
                            (float) sineValue);

                    data.put((byte) (c.getRed())); // r
                    data.put((byte) (c.getGreen())); // g
                    data.put((byte) (c.getBlue()));// b
                    data.put((byte) 255); // a

                    index += 4;
                }
            }
        }

        data.flip();
    }

    private static double logistic(double x) {
        double k = 3.0;
        return (1.0 / (1.0 + Math.pow(2.718, -k * x)));
    }

}
