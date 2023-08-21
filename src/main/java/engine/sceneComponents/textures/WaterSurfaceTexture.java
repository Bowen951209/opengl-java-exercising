package engine.sceneComponents.textures;

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;

public class WaterSurfaceTexture extends Texture3D {
    private final PerlinNoiseGenerator noiseGenerator = PerlinNoiseGenerator.newBuilder().build();

    public WaterSurfaceTexture(int usingUnit) {
        super(usingUnit);
    }

    @Override
    protected void fillDataArray() {
        levelMixedNoise(zoom, 1);
    }

    public void levelMixedNoise(double maxSize, int minSize) {
        final double initialSize = maxSize;
        int sizeCounts = 0;
        while (maxSize >= minSize) {
            sizeCounts++;
            maxSize /= 2.0;
        }
        maxSize = initialSize;

        Thread[] threads = new Thread[sizeCounts];
        for (int i = 0; i < sizeCounts; i++) {
            final double currentSize = maxSize;

            threads[i] = new Thread(() -> {
                int currentIndex = 0;
                for (double x = 0; x < textureWidth; x++) {
                    for (double y = 0; y < textureHeight; y++) {
                        for (double z = 0; z < textureDepth; z++) {
                            double sinValue = Math.sin(1.0 / 512.0 * 8 * Math.PI * (x + z) + 1) * 8.0;

                            double noiseValue = noiseGenerator.evaluateNoise(x / currentSize, y / currentSize, z / currentSize) * currentSize;
                            noiseValue /= initialSize;


                            byte lastValue = data.get(currentIndex);
                            byte newValue = (byte) (lastValue + noiseValue * 255);

                            data.put(currentIndex, newValue);            //r
                            data.put(currentIndex + 1, newValue); //g
                            data.put(currentIndex + 2, newValue); //b
                            data.put(currentIndex + 3, (byte) 255);//a

                            currentIndex += 4;
                        }
                    }
                }
            });

            threads[i].start();

            maxSize /= 2;
        }

        for (int i = 0; i < sizeCounts; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
