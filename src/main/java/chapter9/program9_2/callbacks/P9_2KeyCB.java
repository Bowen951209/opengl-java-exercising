package chapter9.program9_2.callbacks;

import org.lwjgl.glfw.GLFWKeyCallback;
import utilities.sceneComponents.Camera;

import static org.lwjgl.glfw.GLFW.*;

public class P9_2KeyCB extends GLFWKeyCallback {
    private final P9_2CursorCB CURSOR_CB;
    private final Camera CAMERA;
    public P9_2KeyCB(Camera camera, P9_2CursorCB cursorCB) {
        this.CAMERA = camera;
        this.CURSOR_CB = cursorCB;
    }
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            switch (key) {
                case GLFW_KEY_W -> CAMERA.forward();
                case GLFW_KEY_S -> CAMERA.backward();
                case GLFW_KEY_A -> CAMERA.left();
                case GLFW_KEY_D -> CAMERA.right();

                case GLFW_KEY_F1 -> CURSOR_CB.changeLock();
            }
        } else if (action == GLFW_RELEASE) {
            switch (key) {
                case GLFW_KEY_W -> CAMERA.cancelF();
                case GLFW_KEY_S -> CAMERA.cancelB();
                case GLFW_KEY_A -> CAMERA.cancelL();
                case GLFW_KEY_D -> CAMERA.cancelR();
            }
        }
    }
}
