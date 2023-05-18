package chapter8.program8_1.callbacks;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import utilities.Camera;

import static org.lwjgl.opengl.GL11.glViewport;

public class FrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera CAMERA;

    public FrameBufferResizeCB(Camera CAMERA) {
        this.CAMERA = CAMERA;
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        CAMERA.setProjMat(width, height);
    }
}
