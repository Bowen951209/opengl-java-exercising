package engine.util;


import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.generators.noise_parameters.interpolation.Interpolation;
import de.articdive.jnoise.pipeline.JNoise;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

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

    public void tileTurbulence(ByteBuffer buffer, int textureWidth, int textureHeight, int textureDepth, int maxSize, int minSize) {
        final int CORES_COUNT = Runtime.getRuntime().availableProcessors();
        final int SLICE_WIDTH = textureWidth / CORES_COUNT; // !!! not int !!!
        final int LAST_SLICE_WIDTH = textureWidth - (SLICE_WIDTH * CORES_COUNT);

        final List<Thread> threadList = new ArrayList<>();


        for (int i = 0; i < CORES_COUNT; i++) { // 1 slice 1 thread
            final int START_X = SLICE_WIDTH * i;
            final int END_X = START_X + SLICE_WIDTH;

            Thread thread = new Thread(() -> {
                for (int x = START_X; x < END_X; x++) {
                    for (int y = 0; y < textureHeight; y++) {
                        for (int z = 0; z < textureDepth; z++) {
                            double sum = getTileTurbulenceValue(textureWidth, textureHeight, maxSize, x, y, z);

                            final int INDEX = (z + textureDepth * y + textureDepth * textureHeight * x) * 4;
                            buffer.put(INDEX, (byte) sum);
                            buffer.put(INDEX + 1, (byte) sum);
                            buffer.put(INDEX + 2, (byte) sum);
                            buffer.put(INDEX + 3, (byte) 255);
                        }
                    }
                }
            });
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // complete the last slice
        for (int x = 0; x < LAST_SLICE_WIDTH; x++) {
            for (int y = 0; y < textureHeight; y++) {
                for (int z = 0; z < textureDepth; z++) {
                    double sum = getTileTurbulenceValue(textureWidth, textureHeight, maxSize, x, y, z);

                    final int INDEX = (z + textureDepth * y + textureDepth * textureHeight * x) * 4;
                    buffer.put(INDEX, (byte) sum);
                    buffer.put(INDEX + 1, (byte) sum);
                    buffer.put(INDEX + 2, (byte) sum);
                    buffer.put(INDEX + 3, (byte) 255);
                }
            }
        }
    }


    private double getTileTurbulenceValue(int textureWidth, int textureDepth, int maxSize, double x, double y, double z) {
        double sum, zoom = maxSize;
        sum = (sin((1.0 / (textureWidth + textureDepth)) * (8 * PI) * (x + z)) + 1) * 8.0;

        while (zoom >= 0.9) {
            sum = sum + perlinNoiseGenerator.evaluateNoise(zoom, x / zoom, y / zoom, z / zoom) * zoom;
            zoom = zoom / 2.0;
        }

        sum = 100.0 * sum / maxSize; // 100 is my tune value I tested out.
        return sum;
    }
}