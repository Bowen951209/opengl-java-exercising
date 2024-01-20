package net.bowen.chapter8;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import net.bowen.engine.sceneComponents.Camera;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class CursorCB extends GLFWCursorPosCallback {
    private Camera CAMERA;
    private long window;

    public CursorCB setCamera(Camera camera) {
        this.CAMERA = camera;
        return this;
    }

    private boolean lockCursor;
    private void lockCursor() {
        lockCursor = true;
    }
    private void unlockCursor() {
        lockCursor = false;
    }
    public void changeLock() {
        if (lockCursor) {
            unlockCursor();
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            lockCursor();
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        }
    }
    @Override
    public void invoke(long window, double xpos, double ypos) {
        this.window = window;

        if (lockCursor) {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            glfwGetFramebufferSize(Program8_1.getWindowHandle(), width, height);
            assert width.get(0) == 0;
            assert height.get(0) == 0;

            int centerX = width.get(0) / 2;
            int centerY = height.get(0) / 2;
            glfwSetCursorPos(window, centerX, centerY);

            if (ypos < centerY) {
                // up
                CAMERA.lookUp();
            } else if (ypos > centerY) {
                // down
                CAMERA.lookDown();
            }

            if (xpos < centerX) {
                // left
                CAMERA.lookLeft();
            } else if (xpos > centerX) {
                // right
                CAMERA.lookRight();
            }
        }
    }

}
