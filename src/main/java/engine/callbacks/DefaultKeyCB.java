package engine.callbacks;

import engine.sceneComponents.Camera;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

class DefaultKeyCB extends GLFWKeyCallback {
    private final HashMap<Integer, Runnable> pressKeymap = new HashMap<>(), releaseKeymap = new HashMap<>();

    public DefaultKeyCB(Camera camera, DefaultCursorCB cursorCB) {
        pressKeymap.put(GLFW_KEY_W, camera::forward);
        releaseKeymap.put(GLFW_KEY_W, camera::cancelF);
        pressKeymap.put(GLFW_KEY_S, camera::backward);
        releaseKeymap.put(GLFW_KEY_S, camera::cancelB);
        pressKeymap.put(GLFW_KEY_A, camera::left);
        releaseKeymap.put(GLFW_KEY_A, camera::cancelL);
        pressKeymap.put(GLFW_KEY_D, camera::right);
        releaseKeymap.put(GLFW_KEY_D, camera::cancelR);
        pressKeymap.put(GLFW_KEY_SPACE, camera::fly);
        releaseKeymap.put(GLFW_KEY_SPACE, camera::cancelFly);
        pressKeymap.put(GLFW_KEY_LEFT_SHIFT, camera::land);
        releaseKeymap.put(GLFW_KEY_LEFT_SHIFT, camera::cancelLand);

        pressKeymap.put(GLFW_KEY_F1, cursorCB::changeLock);
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (pressKeymap.get(key) != null) // Defined keybindings
                pressKeymap.get(key).run();
        } else if (action == GLFW_RELEASE) {
            if (releaseKeymap.get(key) != null) // Defined keybindings
                releaseKeymap.get(key).run();
        }
    }
}
