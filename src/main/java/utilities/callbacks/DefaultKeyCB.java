package utilities.callbacks;

import org.lwjgl.glfw.GLFWKeyCallback;
import utilities.sceneComponents.Camera;

import static org.lwjgl.glfw.GLFW.*;

class DefaultKeyCB extends GLFWKeyCallback {
    private final DefaultCursorCB cursorCB;
    private final Camera camera;

    public DefaultKeyCB(Camera camera, DefaultCursorCB cursorCB) {
        this.camera = camera;
        this.cursorCB = cursorCB;
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            switch (key) {
                case GLFW_KEY_W -> camera.forward();
                case GLFW_KEY_S -> camera.backward();
                case GLFW_KEY_A -> camera.left();
                case GLFW_KEY_D -> camera.right();
                case GLFW_KEY_SPACE -> camera.fly();
                case GLFW_KEY_LEFT_SHIFT -> camera.land();

                case GLFW_KEY_F1 -> cursorCB.changeLock();
            }
        } else if (action == GLFW_RELEASE) {
            switch (key) {
                case GLFW_KEY_W -> camera.cancelF();
                case GLFW_KEY_S -> camera.cancelB();
                case GLFW_KEY_A -> camera.cancelL();
                case GLFW_KEY_D -> camera.cancelR();
                case GLFW_KEY_SPACE -> camera.cancelFly();
                case GLFW_KEY_LEFT_SHIFT -> camera.cancelLand();
            }
        }
    }
}
