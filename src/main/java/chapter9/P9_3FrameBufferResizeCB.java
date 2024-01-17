package chapter9;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import engine.sceneComponents.Camera;

import static org.lwjgl.opengl.GL11.glViewport;

class P9_3FrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera camera;

    public P9_3FrameBufferResizeCB(Camera camera) {
        this.camera = camera;
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        camera.setProjMat(width, height);
    }
}
