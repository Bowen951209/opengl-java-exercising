package utilities.callbacks;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import utilities.Camera;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

class DefaultCursorCB extends GLFWCursorPosCallback {
    private final Camera CAMERA;
    private long window;

    public DefaultCursorCB(Camera camera) {
        this.CAMERA = camera;
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
            glfwGetFramebufferSize(window, width, height);
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
