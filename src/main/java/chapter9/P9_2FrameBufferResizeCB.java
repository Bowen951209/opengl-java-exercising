package chapter9;

import engine.util.ShadowFrameBuffer;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import engine.sceneComponents.Camera;

import static org.lwjgl.opengl.GL11.glViewport;

public class P9_2FrameBufferResizeCB extends GLFWFramebufferSizeCallback {
    private final Camera camera;
    private final ShadowFrameBuffer SHADOW_FRAME_BUFFER;

    public P9_2FrameBufferResizeCB(Camera camera, ShadowFrameBuffer SHADOW_FRAME_BUFFER) {
        this.camera = camera;
        this.SHADOW_FRAME_BUFFER = SHADOW_FRAME_BUFFER;
    }
    @Override
    public void invoke(long window, int width, int height) {
        System.out.println("GLFW Window Resized to: " + width + "*" + height);
        glViewport(0, 0, width, height);
        camera.setProjMat(width, height);
        SHADOW_FRAME_BUFFER.resetTex(width, height);
    }
}
