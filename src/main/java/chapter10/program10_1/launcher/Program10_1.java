package chapter10.program10_1.launcher;


import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.callbacks.DefaultCallbacks;
import engine.sceneComponents.Camera;
import engine.sceneComponents.Skybox;
import engine.sceneComponents.models.Torus;
import engine.util.Color;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.Objects;

import static engine.util.ValuesContainer.VALS_OF_16;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program10_1 {

    protected long windowID;

    public Program10_1(String title) {
        init(title);
        while (!GLFW.glfwWindowShouldClose(windowID)) {
            loop();
        }
        destroy();
    }


    private final Vector3f TORUS_POS = new Vector3f(0f, 0f, 0f);

    private Torus torus;
    private Skybox skybox;
    private int defaultProgram, skyBoxProgram;

    private int pDefaultMvLoc, pDefaultProjLoc, pDefaultNormLoc;


    protected final Camera camera = new Camera().sensitive(.04f).step(.05f);


    public static void main(String[] args) {
        new Program10_1("Procedural Bump Mapping");
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("App exit and freed glfw.");
    }

    protected void init(String title) {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        camera.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new DefaultCallbacks(windowID, camera).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glActiveTexture(GL_TEXTURE1);
        defaultProgram = new ShaderProgram(Path.of("assets/shaders/program10_1/vert.glsl")
                , Path.of("assets/shaders/program10_1/frag.glsl"))
                .getID();

        getAllUniformsLoc();
        setupVertices(skyBoxProgram);

        System.out.println("Hint: You can press F1 to look around.");
    }

    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        skybox.draw();

        drawScene();

        camera.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }


    private void setupVertices(int skyBoxProgram) {
        System.out.println("Loading models...");

        torus = new Torus(.5f, .2f, 48, true, TORUS_POS);
        skybox = new Skybox(camera, "assets/textures/skycubes/lakesIsland");

        System.out.println("Model load done.");
    }

    private void drawScene() {
        glUseProgram(defaultProgram);

//         繪製torus
        torus.updateState(camera);

        glUniformMatrix4fv(pDefaultMvLoc, false, torus.getMvMat().get(VALS_OF_16));
        glUniformMatrix4fv(pDefaultProjLoc, false, camera.getProjMat().get(VALS_OF_16));

        glUniformMatrix4fv(pDefaultNormLoc, false, torus.getInvTrMat().get(VALS_OF_16));

        torus.draw(GL_TRIANGLES);
    }

    protected void getAllUniformsLoc() {
        pDefaultMvLoc = glGetUniformLocation(defaultProgram, "mv_matrix");
        pDefaultProjLoc = glGetUniformLocation(defaultProgram, "proj_matrix");
        pDefaultNormLoc = glGetUniformLocation(defaultProgram, "norm_matrix");
    }

    protected void destroy() {
        // overrideable
        glfwDestroyWindow(windowID);
        System.out.println("GLFW window destroyed");
        glfwTerminate();
        System.out.println("GLFW terminated");
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        System.out.println("GLFW error callback freed");
    }
}
