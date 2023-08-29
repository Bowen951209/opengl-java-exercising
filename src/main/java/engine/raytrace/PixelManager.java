package engine.raytrace;

import java.util.HashSet;
import java.util.Set;
import static org.lwjgl.opengl.GL43.*;

/*
* This class manages what pixels should be rendered, what should not when ray tracing.
* */
public class PixelManager {
    private final Set<Integer> pixelSet;
    private int[] pixelArray;

    public PixelManager() {
        this.pixelSet = new HashSet<>();
    }

    public void turnOn(int number) {
        pixelArray = pixelSet.stream().mapToInt(i->i).toArray();

        for (int i = 0; i < number; i++) {
            pixelArray[i] = 1;
        }
    }

    public void fill(int size) {
        pixelSet.clear();
        for (int i = 0; i < size; i++) {
            pixelSet.add(0);
        }
    }

    public void putPixelArrayToSSBO(int ssboID) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboID);
        glBufferData(GL_SHADER_STORAGE_BUFFER, pixelArray, GL_DYNAMIC_DRAW);
    }
}
