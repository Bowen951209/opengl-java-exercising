package engine.sceneComponents.textures;

import engine.util.NoiseGenerator;

public class WaterCausticTexture extends Texture3D {
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public WaterCausticTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        noiseGenerator.turbulence(data, textureWidth, textureHeight, textureDepth, zoom, 1);
        for (int x = 0; x< textureWidth;x++) {
            for (int y = 0; y< textureHeight;y++) {
                for (int z = 0; z< textureDepth;z++) {
                    double noiseValue = noiseGenerator.getPerlinNoiseGenerator().evaluateNoise(x / zoom,y / zoom,z / zoom) * zoom;
                    byte byteValue = (byte) (noiseValue * 128);
                    data.put(byteValue);
                    data.put(byteValue);
                    data.put(byteValue);
                    data.put((byte) 255);
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
