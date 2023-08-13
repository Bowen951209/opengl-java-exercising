package engine.util;


import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;

import java.nio.ByteBuffer;

public class NoiseGenerator {
    private final PerlinNoiseGenerator perlinNoiseGenerator = PerlinNoiseGenerator.newBuilder().build();

    public void turbulence(ByteBuffer buffer, int textureWidth, int textureHeight, int textureDepth, int maxSize, int minSize, String color) {
        final int initialSize = maxSize;
        int sizeCounts = 0;
        while (maxSize >= minSize) {
            sizeCounts++;
            maxSize /= 2.0;
        }
        maxSize = initialSize;
//        System.out.println("You have " + sizeCounts + " size counts");


        // I don't allocate thread here because I think too large size scale is meaningless
        Thread[] threads = new Thread[sizeCounts];
        for (int i = 0; i < sizeCounts; i++) {
            final int currentSize = maxSize;

            threads[i] = new Thread(() -> {
//                System.out.println("Processing size #" + currentSize);

                int currentIndex = 0;
                for (double x = 0; x < textureWidth; x++) {
                    for (double y = 0; y < textureHeight; y++) {
                        for (double z = 0; z < textureDepth; z++) {
                            double noiseValue = perlinNoiseGenerator.evaluateNoise(x / currentSize, y / currentSize, z / currentSize) * currentSize;
                            noiseValue /= initialSize;


                            byte lastValue = buffer.get(currentIndex);
                            byte newValue = (byte) (lastValue + noiseValue * 255);

                            if (color.equalsIgnoreCase("WHITE")) {
                                buffer.put(currentIndex, newValue);            //r
                                buffer.put(currentIndex + 1, newValue); //g
                                buffer.put(currentIndex + 2, newValue); //b
                                buffer.put(currentIndex + 3, (byte) 255);//a
                            } else if (color.equalsIgnoreCase("BLUE")) {
                                buffer.put(currentIndex, newValue);            //r
                                buffer.put(currentIndex + 1, newValue); //g
                                buffer.put(currentIndex + 2, (byte) 255); //b
                                buffer.put(currentIndex + 3, (byte) 255);//a
                            }
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
    public void turbulence(ByteBuffer buffer, int textureWidth, int textureHeight, int textureDepth, int maxSize, int minSize) {
        turbulence(buffer, textureWidth, textureHeight, textureDepth, maxSize, minSize, "WHITE");
    }

}