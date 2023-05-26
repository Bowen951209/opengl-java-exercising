package chapter8.program8_1.callbacks;

import utilities.buffers.ShadowFrameBuffer;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import utilities.Camera;

import static org.lwjgl.opengl.GL11.glViewport;

public class FrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera CAMERA;
    private final ShadowFrameBuffer SHADOW_FRAME_BUFFER;

    public FrameBufferResizeCB(Camera CAMERA, ShadowFrameBuffer SHADOW_FRAME_BUFFER) {
        this.CAMERA = CAMERA;
        this.SHADOW_FRAME_BUFFER = SHADOW_FRAME_BUFFER;
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        CAMERA.setProjMat(width, height);
        SHADOW_FRAME_BUFFER.resetTex(width, height);
    }
}
