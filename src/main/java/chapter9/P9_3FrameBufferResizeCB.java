package chapter9;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import engine.sceneComponents.Camera;

import static org.lwjgl.opengl.GL11.glViewport;

class P9_3FrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera CAMERA;

    public P9_3FrameBufferResizeCB(Camera CAMERA) {
        this.CAMERA = CAMERA;
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        CAMERA.setProjMat(width, height);
    }
}
