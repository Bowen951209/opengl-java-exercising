package utilities;

import org.lwjgl.glfw.GLFW;
import utilities.sceneComponents.Camera;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public abstract class Program {
    protected long windowID;
    protected GUI gui;
    protected Camera camera;
    protected int program;


    protected abstract void init();

    protected abstract void getAllUniformLocs();

    protected abstract void drawScene();

    protected abstract void destroy();

    private void setGL(boolean isCullFace) {
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

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }



    protected void run(boolean isCullFace) {
        init();
        getAllUniformLocs();
        setGL(isCullFace);

        assert windowID == 0;
        while (!GLFW.glfwWindowShouldClose(windowID)) {
            loop();
        }
        destroy();
    }
}
