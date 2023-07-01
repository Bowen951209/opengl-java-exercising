package utilities;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class Destroyer {
    public static void destroyAll(long windowID, GUI gui) {
        destroyGLFWWindow(windowID);
        terminateGLFW();
        freeGLFWCallback();
        destroyGUI(gui);
    }

    public static void destroyGLFWWindow(long windowID) {
        glfwDestroyWindow(windowID);
        System.out.println("GLFW window destroyed");
    }

    public static void terminateGLFW() {
        glfwTerminate();
        System.out.println("GLFW terminated");
    }

    public static void freeGLFWCallback() {
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        System.out.println("GLFW error callback freed");
    }

    public static void destroyGUI(GUI gui) {
        gui.destroy();
    }
}
