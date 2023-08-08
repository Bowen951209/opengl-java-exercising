package engine.callbacks;

import engine.sceneComponents.Camera;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glViewport;

public class DefaultFrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera camera;
    public DefaultFrameBufferResizeCB(Camera camera) {
        this.camera = camera;
    }
    private final List<Runnable> callbackList = new ArrayList<>();
    public void addCallback(Runnable cb) {
        callbackList.add(cb);
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        camera.setProjMat(width, height);
        callbackList.forEach(Runnable::run);
    }
}
