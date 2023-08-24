package engine.callbacks;

import engine.sceneComponents.Camera;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

class DefaultCursorCB extends GLFWCursorPosCallback {
    private final Camera camera;
    private final long window;
    private float deltaScroll;
    public DefaultCursorCB(Camera camera, long window) {
        this.camera = camera;
        this.window = window;
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
        if (lockCursor) {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            glfwGetFramebufferSize(window, width, height);

            int centerX = width.get(0) / 2;
            int centerY = height.get(0) / 2;
            glfwSetCursorPos(window, centerX, centerY);

            if (ypos < centerY) {
                // up
                camera.lookUp();
            } else if (ypos > centerY) {
                // down
                camera.lookDown();
            }

            if (xpos < centerX) {
                // left
                camera.lookLeft();
            } else if (xpos > centerX) {
                // right
                camera.lookRight();
            }
        }
    }
}
