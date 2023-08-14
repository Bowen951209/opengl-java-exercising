package engine.util;


import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.generators.noise_parameters.interpolation.Interpolation;
import de.articdive.jnoise.pipeline.JNoise;

import java.nio.ByteBuffer;

public class NoiseGenerator {
    private final JNoise perlinNoiseGenerator = JNoise.newBuilder().perlin(1077, Interpolation.LINEAR, FadeFunction.IMPROVED_PERLIN_NOISE)
//            .scale(1 / 32.0) // int point will return 0 (perlin noise law)
            .addModifier(v -> (v + 1) / 2.0) // [-1, 1] to [0, 1]
            .clamp(0.0, 1.0)
            .build();

    public JNoise getPerlinNoiseGenerator() {
        return perlinNoiseGenerator;
    }

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

                            double noiseValue = perlinNoiseGenerator
                                    .evaluateNoise(x / currentSize, y / currentSize, z / currentSize)
                                    * ((float) currentSize / initialSize);
                            byte byteNoiseValue = (byte) (noiseValue * 170);
                            // If the value is > 255 then it will go back to 0, so it will look black.
                            // The way to fix it is to mul a smaller number, so I changed "noiseValue * 255" to * 170
                            // NOTE: In java, byte are signed byte, which range [-127, 127]. Our library will transform it to unsigned byte.
                            byte lastValue = buffer.get(currentIndex);

                            byte newValue = (byte) (lastValue + byteNoiseValue);

                            if (color.equalsIgnoreCase("WHITE")) {
                                buffer.put(currentIndex, newValue);             //r
                                buffer.put(currentIndex + 1, newValue);  //g
                                buffer.put(currentIndex + 2, newValue);  //b
                                buffer.put(currentIndex + 3, (byte) 255);//a
                            } else if (color.equalsIgnoreCase("BLUE")) {
                                buffer.put(currentIndex, newValue);             //r
                                buffer.put(currentIndex + 1, newValue);  //g
                                buffer.put(currentIndex + 2, (byte) 255);//b
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