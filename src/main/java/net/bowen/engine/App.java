package net.bowen.engine;

import net.bowen.engine.gui.GUI;
import net.bowen.engine.sceneComponents.models.FileModel;
import net.bowen.engine.sceneComponents.textures.Texture3D;
import net.bowen.engine.util.Timer;
import net.bowen.engine.callbacks.DefaultCallbacks;
import net.bowen.engine.sceneComponents.Camera;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public abstract class App {
    private final Timer timer = new Timer();

    private List<Texture3D> texture3DList;
    private List<FileModel> fileModelList;
    private boolean wantGUI, wantCullFace;
    private float fps;
    private DefaultCallbacks defaultCallbacks;

    protected GLFWWindow glfwWindow;
    protected GUI gui;
    protected Camera camera;

    public void addFileModel(FileModel model) {
        if (fileModelList == null) {
            fileModelList = new ArrayList<>() {
                @Override
                public boolean add(FileModel fileModel) {
                    fileModel.start();
                    return super.add(fileModel);
                }
            };
        }

        fileModelList.add(model);
    }

    public void addTexture3D(Texture3D tex) {
        if (texture3DList == null) {
            texture3DList = new ArrayList<>() {
                @Override
                public boolean add(Texture3D texture3D) {
                    texture3D.start();
                    return super.add(texture3D);
                }
            };
        }

        texture3DList.add(tex);
    }

    public final DefaultCallbacks getDefaultCallbacks() {
        return defaultCallbacks;
    }

    public final GLFWWindow getGlfwWindow() {
        return glfwWindow;
    }

    public final GUI getGui() {
        return gui;
    }

    public final float getFps() {
        return fps;
    }

    protected void run() {
        final Timer timer = new Timer();
        timer.start();

        initGLFWWindow();
        initShaderPrograms();
        // customizable customizedInit
        customizedInit();

        // always the same setup.
        glfwSwapInterval(1);
        camera = new Camera(glfwWindow.getInitWidth(), glfwWindow.getInitHeight()); // camera customizedInit.
        initTextures();
        initModels();
        defaultCallbacks = new DefaultCallbacks(glfwWindow.getID(), camera, wantGUI); // callback.
        defaultCallbacks.bindToGLFW();
        addCallbacks();
        configGL(); // In some programs, like one using tessellation, wouldn't work with face culling.
        if (wantGUI)
            initGUI();

        if (texture3DList != null)
            texture3DList.forEach(Texture3D::end);

        if (fileModelList != null)
            fileModelList.forEach(FileModel::end);

        timer.end("Program initialized in: ");
        // loop.
        assert glfwWindow != null;
        while (!GLFW.glfwWindowShouldClose(glfwWindow.getID())) {
            loop();
        }

        // clean up.
        destroy();
    }

    public void run(boolean isWantGUI) {
        this.wantGUI = isWantGUI;
        run();
    }

    public void run(boolean isWantCullFace, boolean isWantGUI) {
        this.wantCullFace = isWantCullFace;
        this.wantGUI = isWantGUI;
        run();
    }

    protected void loop() {
        timer.start();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        drawScene();
        if (wantGUI)
            gui.update();

        camera.handle();

        glfwSwapBuffers(glfwWindow.getID());
        glfwPollEvents();
        timer.end();
        fps = timer.getFps();
    }

    private void configGL() {
        // GL settings
        if (wantCullFace)
            glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }


    protected abstract void drawScene();

    protected abstract void destroy();

    protected void customizedInit() {
    }

    protected void addCallbacks() {
    }

    protected void initGLFWWindow() {
    }

    protected void initShaderPrograms() {
    }

    protected void initTextures() {
    }

    protected void initModels() {
    }

    protected void initGUI() {
    }
}
