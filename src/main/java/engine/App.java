package engine;

import engine.gui.GUI;
import engine.models.FileModel;
import engine.sceneComponents.textures.Texture3D;
import org.lwjgl.glfw.GLFW;
import engine.callbacks.DefaultCallbacks;
import engine.sceneComponents.Camera;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public abstract class App {
    protected GLFWWindow glfwWindow;
    protected GUI gui;
    protected Camera camera;
    private boolean isWantGUI, isWantCullFace;
    protected List<FileModel> fileModelList = new ArrayList<>();
    protected List<Texture3D> texture3DList = new ArrayList<>();

    protected abstract void init();

    protected void getAllUniformLocs() {
    }

    protected abstract void drawScene();

    protected abstract void destroy();

    private void configGL() {
        // GL settings
        if (isWantCullFace)
            glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        drawScene();
        if (isWantGUI)
            gui.update();

        camera.handle();

        glfwSwapBuffers(glfwWindow.getID());
        glfwPollEvents();
    }

    protected void run() {
        // customizable init
        init();

        for (Texture3D i : texture3DList) {
            i.start();
        }
        for (FileModel i : fileModelList) {
            i.start();
        }


        // always the same setup.
        camera = new Camera(glfwWindow.getWidth(), glfwWindow.getHeight()); // camera init.
        new DefaultCallbacks(glfwWindow.getID(), camera, isWantGUI).bindToGLFW(); // callback.
        getAllUniformLocs();
        configGL(); // In some programs, like one using tessellation, wouldn't work with face culling.


        for (Texture3D i : texture3DList) {
            i.end();
        }
        for (FileModel i : fileModelList) {
            i.end();
        }

        // loop.
        assert glfwWindow != null;
        while (!GLFW.glfwWindowShouldClose(glfwWindow.getID())) {
            loop();
        }

        // clean up.
        destroy();
    }

    public void run(boolean isWantGUI) {
        this.isWantGUI = isWantGUI;
        run();
    }

    public void run(boolean isWantCullFace, boolean isWantGUI) {
        this.isWantCullFace = isWantCullFace;
        this.isWantGUI = isWantGUI;
        run();
    }
}
