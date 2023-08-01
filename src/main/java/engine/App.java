package engine;

import engine.gui.GUI;
import engine.models.FileModel;
import engine.sceneComponents.textures.Texture3D;
import engine.util.Timer;
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
    private DefaultCallbacks defaultCallbacks;

    public DefaultCallbacks getDefaultCallbacks() {
        return defaultCallbacks;
    }

    public GLFWWindow getGlfwWindow() {
        return glfwWindow;
    }

    protected GUI gui;

    public GUI getGui() {
        return gui;
    }

    protected Camera camera;
    private boolean isWantGUI, isWantCullFace;
    protected List<FileModel> fileModelList = new ArrayList<>() {
        @Override
        public boolean add(FileModel fileModel) {
            fileModel.start();
            return super.add(fileModel);
        }
    };
    protected List<Texture3D> texture3DList = new ArrayList<>() {
        @Override
        public boolean add(Texture3D texture3D) {
            texture3D.start();
            return super.add(texture3D);
        }
    };

    private final Timer timer = new Timer();
    private float fps;

    public float getFps() {
        return fps;
    }

    protected abstract void customizedInit();

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
        timer.start();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        drawScene();
        if (isWantGUI)
            gui.update();

        camera.handle();

        glfwSwapBuffers(glfwWindow.getID());
        glfwPollEvents();
        timer.end();
        fps = timer.getFps();
    }

    protected void run() {
        // customizable customizedInit
        customizedInit();

        // TODO: 2023/7/31 Texture3D and FileModel implement same interface.

        // always the same setup.
        camera = new Camera(glfwWindow.getWidth(), glfwWindow.getHeight()); // camera customizedInit.
        defaultCallbacks = new DefaultCallbacks(glfwWindow.getID(), camera, isWantGUI); // callback.
        defaultCallbacks.bindToGLFW();
        getAllUniformLocs();
        configGL(); // In some programs, like one using tessellation, wouldn't work with face culling.
        if (isWantGUI)
            initGUI();


        texture3DList.forEach(Texture3D::end);
        fileModelList.forEach(FileModel::end);

        // loop.
        assert glfwWindow != null;
        while (!GLFW.glfwWindowShouldClose(glfwWindow.getID())) {
            loop();
        }

        // clean up.
        destroy();
    }

    protected void initGUI() {}

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
