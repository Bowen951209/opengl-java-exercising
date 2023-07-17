package engine;

import engine.gui.GUI;
import org.lwjgl.glfw.GLFW;
import engine.callbacks.DefaultCallbacks;
import engine.sceneComponents.Camera;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public abstract class App {
    protected GLFWWindow glfwWindow;
    protected GUI gui;
    protected Camera camera;

    protected abstract void init();

    protected void getAllUniformLocs() {}

    protected abstract void drawScene();

    protected abstract void destroy();

    private void configGL(boolean isCullFace) {
        // GL settings
        if (isCullFace)
            glEnable(GL_CULL_FACE);

        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        drawScene();

        gui.update();

        camera.handle();

        glfwSwapBuffers(glfwWindow.getID());
        glfwPollEvents();
    }

    private void noGUILoop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        drawScene();

        camera.handle();

        glfwSwapBuffers(glfwWindow.getID());
        glfwPollEvents();
    }


    protected void run(boolean isWantCullFace) {
        // customizable init
        init();

        // always the same setup.
        camera = new Camera(glfwWindow.getWidth(), glfwWindow.getHeight()); // camera init.
        new DefaultCallbacks(glfwWindow.getID(), camera, true).bindToGLFW(); // callback.
        getAllUniformLocs();
        configGL(isWantCullFace); // In some programs, like one using tessellation, wouldn't work with face culling.

        // loop.
        assert glfwWindow != null;
        while (!GLFW.glfwWindowShouldClose(glfwWindow.getID())) {
            loop();
        }

        // clean up.
        destroy();
    }
    public void run(boolean isWantCullFace, boolean isWantGUI) {
        if (isWantGUI)
            run(isWantCullFace);
        else {
            init();

            // always the same setup.
            camera = new Camera(glfwWindow.getWidth(), glfwWindow.getHeight()); // camera init.
            new DefaultCallbacks(glfwWindow.getID(), camera, false).bindToGLFW(); // callback.
            getAllUniformLocs();
            configGL(isWantCullFace); // In some programs, like one using tessellation, wouldn't work with face culling.

            // loop.
            assert glfwWindow != null;
            while (!GLFW.glfwWindowShouldClose(glfwWindow.getID())) {
                noGUILoop();
            }

            // clean up.
            destroy();
        }
    }
}
