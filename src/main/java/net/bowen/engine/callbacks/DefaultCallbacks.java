package net.bowen.engine.callbacks;


import imgui.ImGuiIO;
import imgui.internal.ImGui;
import net.bowen.engine.sceneComponents.Camera;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

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

    public DefaultFrameBufferResizeCB getDefaultFrameBufferResizeCB() {
        return defaultFrameBufferResizeCB;
    }

    private final long windowID;

    public DefaultCallbacks(long windowID, Camera camera) {
        this.windowID = windowID;

        defaultCursorCB = new DefaultCursorCB(camera, windowID);
        defaultFrameBufferResizeCB = new DefaultFrameBufferResizeCB(camera);
        defaultKeyCB = new DefaultKeyCB(camera, defaultCursorCB);
    }

    public DefaultCallbacks(long windowID, Camera camera, boolean isUsingImGUI) {
        this.windowID = windowID;
        if (!isUsingImGUI) {
            defaultCursorCB = new DefaultCursorCB(camera, windowID);
            defaultFrameBufferResizeCB = new DefaultFrameBufferResizeCB(camera);
        } else {
            // using imGUI

            ImGuiIO IO = ImGui.getIO();
            defaultCursorCB = new DefaultCursorCB(camera, windowID) {
                private static final DoubleBuffer CURSOR_XPOS = BufferUtils.createDoubleBuffer(1);
                private static final DoubleBuffer CURSOR_YPOS = BufferUtils.createDoubleBuffer(1);

                @Override
                public void invoke(long window, double xpos, double ypos) {
                    super.invoke(window, xpos, ypos);
                    GLFW.glfwGetCursorPos(window, CURSOR_XPOS, CURSOR_YPOS);

                    IO.setMousePos((float) CURSOR_XPOS.get(0), (float) CURSOR_YPOS.get(0));
                }
            };

            defaultCursorCB.createScrollCallback();

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
