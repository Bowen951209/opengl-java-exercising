package utilities.callbacks.defaultCBs;


import utilities.Camera;

import static org.lwjgl.glfw.GLFW.*;

public class DefaultCallbacks {
    private final DefaultCursorCB defaultCursorCB;
    private final DefaultFrameBufferResizeCB defaultFrameBufferResizeCB;
    private final DefaultKeyCB defaultKeyCB;
    private final long windowID;

    public DefaultCallbacks(long windowID, Camera camera) {
        this.windowID = windowID;

        defaultCursorCB = new DefaultCursorCB(camera);
        defaultFrameBufferResizeCB = new DefaultFrameBufferResizeCB(camera);
        defaultKeyCB = new DefaultKeyCB(camera, defaultCursorCB);
    }

    public void bindToGLFW() {
        glfwSetCursorPosCallback(windowID, defaultCursorCB);
        glfwSetFramebufferSizeCallback(windowID, defaultFrameBufferResizeCB);
        glfwSetKeyCallback(windowID, defaultKeyCB);
    }
}
