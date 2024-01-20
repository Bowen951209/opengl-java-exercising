package net.bowen.engine.util;

import net.bowen.engine.gui.GUI;
import net.bowen.engine.sceneComponents.textures.Texture2D;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class Destroyer {
    // With GUI
    public static void destroyAll(long windowID, GUI gui) {
        destroyGLFWWindow(windowID);
        terminateGLFW();
        freeGLFWCallback();
        destroyGUI(gui);
        deleteAllTextures();
    }

    // Without GUI
    public static void destroyAll(long windowID) {
        destroyGLFWWindow(windowID);
        terminateGLFW();
        freeGLFWCallback();
        deleteAllTextures();
    }

    private static void destroyGLFWWindow(long windowID) {
        glfwDestroyWindow(windowID);
        System.out.println("GLFW window destroyed.");
    }

    private static void terminateGLFW() {
        glfwTerminate();
        System.out.println("GLFW terminated.");
    }

    private static void freeGLFWCallback() {
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        System.out.println("GLFW error callback freed.");
    }

    private static void destroyGUI(GUI gui) {
        gui.destroy();
        System.out.println("ImGUI destroyed.");
    }

    private static void deleteAllTextures() {
        Texture2D.deleteAllTextures();
        System.out.println("Textures deleted.");
    }
}
