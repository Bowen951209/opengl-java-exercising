package engine.callbacks;


import imgui.ImGuiIO;
import imgui.internal.ImGui;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import engine.sceneComponents.Camera;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class DefaultCallbacks {
    private final DefaultCursorCB defaultCursorCB;
    private final DefaultFrameBufferResizeCB defaultFrameBufferResizeCB;
    private final DefaultKeyCB defaultKeyCB;

    public DefaultKeyCB getDefaultKeyCB() {
        return defaultKeyCB;
    }

    private final long windowID;

    public DefaultCallbacks(long windowID, Camera camera) {
        this.windowID = windowID;

        defaultCursorCB = new DefaultCursorCB(camera);
        defaultFrameBufferResizeCB = new DefaultFrameBufferResizeCB(camera);
        defaultKeyCB = new DefaultKeyCB(camera, defaultCursorCB);
    }

    public DefaultCallbacks(long windowID, Camera camera, boolean isUsingImGUI) {
        this.windowID = windowID;
        if (!isUsingImGUI) {
            defaultCursorCB = new DefaultCursorCB(camera);
            defaultFrameBufferResizeCB = new DefaultFrameBufferResizeCB(camera);
        } else {
            // using imGUI
            ImGuiIO IO = ImGui.getIO();
            defaultCursorCB = new DefaultCursorCB(camera) {
                @Override
                public void invoke(long window, double xpos, double ypos) {
                    super.invoke(window, xpos, ypos);

                    DoubleBuffer cursorXpos = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer cursorYpos = BufferUtils.createDoubleBuffer(1);
                    GLFW.glfwGetCursorPos(window, cursorXpos, cursorYpos);
                    IO.setMousePos((float) cursorXpos.get(0), (float) cursorYpos.get(0));
                }
            };

            defaultFrameBufferResizeCB = new DefaultFrameBufferResizeCB(camera) {
                @Override
                public void invoke(long window, int width, int height) {
                    super.invoke(window, width, height);

                    IntBuffer frameBufferWidth = BufferUtils.createIntBuffer(1);
                    IntBuffer frameBufferHeight = BufferUtils.createIntBuffer(1);
                    GLFW.glfwGetFramebufferSize(window, frameBufferWidth, frameBufferHeight);
                    IO.setDisplaySize(frameBufferWidth.get(0), frameBufferHeight.get(0));
                }
            };
        }
        defaultKeyCB = new DefaultKeyCB(camera, defaultCursorCB);
    }

    public void bindToGLFW() {
        glfwSetCursorPosCallback(windowID, defaultCursorCB);
        glfwSetFramebufferSizeCallback(windowID, defaultFrameBufferResizeCB);
        glfwSetKeyCallback(windowID, defaultKeyCB);
    }
}
