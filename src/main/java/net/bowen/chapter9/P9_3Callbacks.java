package net.bowen.chapter9;


import net.bowen.engine.sceneComponents.Camera;

import static org.lwjgl.glfw.GLFW.*;

public class P9_3Callbacks {
    private final P9_3CursorCB defaultCursorCB;
    private final P9_3FrameBufferResizeCB defaultFrameBufferResizeCB;
    private final P9_3KeyCB defaultKeyCB;
    private final long windowID;

    public P9_3Callbacks(long windowID, Camera camera) {
        this.windowID = windowID;

        defaultCursorCB = new P9_3CursorCB(camera);
        defaultFrameBufferResizeCB = new P9_3FrameBufferResizeCB(camera);
        defaultKeyCB = new P9_3KeyCB(camera, defaultCursorCB);
    }

    public void bindToGLFW() {
        glfwSetCursorPosCallback(windowID, defaultCursorCB);
        glfwSetFramebufferSizeCallback(windowID, defaultFrameBufferResizeCB);
        glfwSetKeyCallback(windowID, defaultKeyCB);
    }
}
