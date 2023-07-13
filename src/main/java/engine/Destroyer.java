package engine;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class Destroyer {
    // With GUI
    public static void destroyAll(long windowID, GUI gui) {
        destroyGLFWWindow(windowID);
        terminateGLFW();
        freeGLFWCallback();
        destroyGUI(gui);
    }

    // Without GUI
    public static void destroyAll(long windowID) {
        destroyGLFWWindow(windowID);
        terminateGLFW();
        freeGLFWCallback();
    }

    private static void destroyGLFWWindow(long windowID) {
        glfwDestroyWindow(windowID);
        System.out.println("GLFW window destroyed");
    }

    private static void terminateGLFW() {
        glfwTerminate();
        System.out.println("GLFW terminated");
    }

    private static void freeGLFWCallback() {
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        System.out.println("GLFW error callback freed");
    }

    private static void destroyGUI(GUI gui) {
        gui.destroy();
    }
}
