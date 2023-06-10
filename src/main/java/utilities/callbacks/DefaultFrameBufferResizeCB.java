package utilities.callbacks;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import utilities.sceneComponents.Camera;

import static org.lwjgl.opengl.GL11.glViewport;

class DefaultFrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera CAMERA;

    public DefaultFrameBufferResizeCB(Camera CAMERA) {
        this.CAMERA = CAMERA;
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        CAMERA.setProjMat(width, height);
    }
}
