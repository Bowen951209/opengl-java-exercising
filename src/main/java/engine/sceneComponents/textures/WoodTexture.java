package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

import java.awt.*;

public class WoodTexture extends Texture3D{
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();
    private static final double XY_PERIOD = 20.0, TURB_POWER = 15.0;
    public WoodTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        fillWood();
    }

    private void fillWood() {
        for (double x = 0; x < textureWidth; x++) {
            for (double y = 0; y < textureHeight; y++) {
                for (double z = 0; z < textureDepth; z++) {
                    double xValue = (x - (double)textureWidth/2.0) / (double)textureWidth;
                    double yValue = (y - (double)textureHeight/2.0) / (double)textureHeight;
                    double distanceFromZ = Math.sqrt(xValue * xValue + yValue * yValue)
                            + TURB_POWER * noiseGenerator.smoothNoise(x / zoom, y / zoom, z / zoom) / 256.0;
                    double sineValue = 128.0 * Math.abs(Math.sin(2.0 * XY_PERIOD * distanceFromZ * Math.PI));

                    Color color = new Color(60+(int)sineValue, 10+(int)sineValue, 0);

                    data.put((byte) (color.getRed())); // r
                    data.put((byte) (color.getGreen())); // g
                    data.put((byte) (color.getBlue()));// b
                    data.put((byte) 255); // a
                }
            }
        }

        data.flip();
    }
}
