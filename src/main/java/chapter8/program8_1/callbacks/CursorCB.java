package chapter8.program8_1.callbacks;

import chapter8.program8_1.launcher.Program8_1;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import utilities.Camera;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public class CursorCB extends GLFWCursorPosCallback {
    private Camera CAMERA;
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
        } else {
            lockCursor();
        }
    }
    @Override
    public void invoke(long window, double xpos, double ypos) {
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