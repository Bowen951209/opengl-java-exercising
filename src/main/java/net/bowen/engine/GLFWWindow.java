package net.bowen.engine;

import net.bowen.engine.util.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.glClearColor;
import static org.lwjgl.opengl.GL43.glViewport;

public class GLFWWindow {
    private static final int MAJOR_VERSION = 4;
    private static final int MINOR_VERSION = 3;

    private static Color clearColor = new Color(0f, 0f, 0f, 0f);

    private final long windowHandle;
    private final int initWidth, initHeight;

    public int getInitWidth() {
        return initWidth;
    }

    public int getInitHeight() {
        return initHeight;
    }

    public int getCurrentWidth() {
        int[] width = new int[1];
        glfwGetWindowSize(getID(), width, null);
        return width[0];
    }

    public int getCurrentHeight() {
        int[] height = new int[1];
        glfwGetWindowSize(getID(), null, height);
        return height[0];
    }


    public long getID() {
        return windowHandle;
    }

    public void setClearColor(Color clearColor) {
        GLFWWindow.clearColor = clearColor;
        glClearColor(clearColor.getR(), clearColor.getG(), clearColor.getB(), clearColor.getA());
    }


    // NOTE: 因為Gradle 不知道出了什麼問題，中文會顯示亂碼，以前還可以的，所以現在我都只打印英文。
    public GLFWWindow(int glfwWidth, int glfwHeight, String title) {
        this.initWidth = glfwWidth;
        this.initHeight = glfwHeight;

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            System.out.println("GLFW Failed To Init!");
        }
        // 設定GLFW
        GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, MAJOR_VERSION);
        GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, MINOR_VERSION);
        GLFW.glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        windowHandle = glfwCreateWindow(glfwWidth, glfwHeight, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (windowHandle == MemoryUtil.NULL) {
            System.out.println("Failed to create GLFW Window.");
            glfwTerminate();
        }
        glfwMakeContextCurrent(windowHandle);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glViewport(0, 0, glfwWidth, glfwHeight); // glViewport(左下角x, 左下角y, 800, 600)
        // Set the clear color
        glClearColor(clearColor.getR(), clearColor.getG(), clearColor.getB(), clearColor.getA());

    }

    public static int[] getFrameBufferSize(long window) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, w, h);
        return new int[]{w.get(0), h.get(0)};
    }
}