package chapter10.program10_1.launcher;


import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import utilities.*;
import utilities.callbacks.DefaultCallbacks;
import utilities.models.Torus;
import utilities.readers.CubeMapReader;
import utilities.sceneComponents.Camera;
import utilities.sceneComponents.Skybox;

import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
import static utilities.ValuesContainer.VALS_OF_16;

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

    private int pDefaultMvLoc, pDefaultProjLoc, pDefaultNormLoc, pSkyVMatLoc, pSkyPMatLoc;


    protected final Camera CAMERA = new Camera().sensitive(.04f).step(.05f);


    public static void main(String[] args) {
        new Program10_1("Procedural Bump Mapping");
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("Program exit and freed glfw.");
    }

    protected void init(String title) {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        CAMERA.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, title);
        windowID = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new DefaultCallbacks(windowID, CAMERA).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glActiveTexture(GL_TEXTURE1);
        defaultProgram = new ShaderProgramSetter(Path.of("assets/shaders/program10_1/vert.glsl")
                , Path.of("assets/shaders/program10_1/frag.glsl"))
                .getProgram();
        skyBoxProgram = new ShaderProgramSetter(Path.of("src/main/java/chapter9/program9_3/shaders/skybox/CubeVertShader.glsl")
                , Path.of("src/main/java/chapter9/program9_3/shaders/skybox/SkyboxFragShader.glsl"))
                .getProgram();

        CubeMapReader skyboxTexture = new CubeMapReader("assets/textures/skycubes/lakesIsland");
        skyboxTexture.bind();

        getAllUniformsLoc();
        setupVertices(skyBoxProgram);

        System.out.println("Hint: You can press F1 to look around.");
    }

    protected void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        CAMERA.updateVMat();

        skybox.draw();

        drawScene();

        CAMERA.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }


    private void setupVertices(int skyBoxProgram) {
        System.out.println("Loading models...");

        torus = new Torus(.5f, .2f, 48, true, TORUS_POS);
        skybox = new Skybox(skyBoxProgram, pSkyVMatLoc, pSkyPMatLoc, CAMERA);

        System.out.println("Model load done.");
    }

    private void drawScene() {
        glUseProgram(defaultProgram);

//         繪製torus
        torus.updateState(CAMERA);

        glUniformMatrix4fv(pDefaultMvLoc, false, torus.getMV_MAT().get(VALS_OF_16));
        glUniformMatrix4fv(pDefaultProjLoc, false, CAMERA.getProjMat().get(VALS_OF_16));

        glUniformMatrix4fv(pDefaultNormLoc, false, torus.getINV_TR_MAT().get(VALS_OF_16));

        torus.draw(GL_TRIANGLES);
    }

    protected void getAllUniformsLoc() {
        pDefaultMvLoc = glGetUniformLocation(defaultProgram, "mv_matrix");
        pDefaultProjLoc = glGetUniformLocation(defaultProgram, "proj_matrix");
        pDefaultNormLoc = glGetUniformLocation(defaultProgram, "norm_matrix");

        pSkyVMatLoc = glGetUniformLocation(skyBoxProgram, "v_matrix");
        pSkyPMatLoc = glGetUniformLocation(skyBoxProgram, "p_matrix");
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
