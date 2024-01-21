package net.bowen.chapter9;


import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.sceneComponents.Camera;
import net.bowen.engine.sceneComponents.Skybox;
import net.bowen.engine.sceneComponents.models.Torus;
import net.bowen.engine.util.Color;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static net.bowen.engine.util.ValuesContainer.VALS_OF_16;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL43.*;

public class Program9_3 {

    private static long windowID;
    private static Skybox skybox;

    public static long getWindowID() {
        return windowID;
    }

    private static final int[] vbo = new int[5];

    private static final Vector3f TORUS_POS = new Vector3f(0f, 0f, 0f);

    private static Torus torus;
    private static int defaultProgram, skyBoxProgram;

    private static int pDefaultMvLoc, pDefaultProjLoc, pDefaultNormLoc, pSkyVMat, pSkyPMat;


    private static final Camera CAMERA = new Camera().sensitive(.04f).step(.05f);


    public static void main(String[] args) {
        init();
        // 迴圈
        while (!GLFW.glfwWindowShouldClose(windowID)) {
            loop();
        }
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("App exit and freed glfw.");
    }

    private static void init() {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        CAMERA.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, "第9章 環境貼圖");
        windowID = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new P9_3Callbacks(windowID, CAMERA).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glActiveTexture(GL_TEXTURE1);
        defaultProgram = new ShaderProgram("assets/shaders/program9_3/vertShader.glsl"
                , "assets/shaders/program9_3/fragShader.glsl")
                .getID();


        setupVertices();
        getAllUniformsLoc();

        System.out.println("Hint: You can press F1 to look around.");
    }

    private static void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        CAMERA.updateVMat();

        drawSkybox();
        drawScene();

        CAMERA.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }



    private static void setupVertices() {
        System.out.println("Loading models...");

        torus = new Torus(.5f, .2f, 48, true, TORUS_POS);

        int[] vao = new int[1];
        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);

        glGenBuffers(vbo);

        // skybox
        skybox = new Skybox(CAMERA, "assets/textures/skycubes/lakesIsland");

        System.out.println("Model load done.");
    }

    private static void drawSkybox() {
        glUseProgram(skyBoxProgram);

        glDisable(GL_DEPTH_TEST);
        glUniformMatrix4fv(pSkyVMat, false, CAMERA.getVMat().get(VALS_OF_16));
        glUniformMatrix4fv(pSkyPMat, false, CAMERA.getProjMat().get(VALS_OF_16));

        skybox.draw();


        glEnable(GL_DEPTH_TEST);
    }

    private static void drawScene() {
        glUseProgram(defaultProgram);
        glEnable(GL_DEPTH_TEST);

//         繪製torus
        torus.updateState(CAMERA);

//        glUniformMatrix4fv(pDefaultMvLoc, false, mvMat.get(VALS_OF_16));
        glUniformMatrix4fv(pDefaultMvLoc, false, torus.getMvMat().get(VALS_OF_16));
        glUniformMatrix4fv(pDefaultProjLoc, false, CAMERA.getProjMat().get(VALS_OF_16));

//        glUniformMatrix4fv(pDefaultNormLoc, false, invTrMat.get(VALS_OF_16));
        glUniformMatrix4fv(pDefaultNormLoc, false, torus.getInvTrMat().get(VALS_OF_16));

//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
        torus.draw(GL_TRIANGLES);
    }

    private static void getAllUniformsLoc() {
        pDefaultMvLoc = glGetUniformLocation(defaultProgram, "mv_matrix");
        pDefaultProjLoc = glGetUniformLocation(defaultProgram, "proj_matrix");
        pDefaultNormLoc = glGetUniformLocation(defaultProgram, "norm_matrix");

        pSkyVMat = glGetUniformLocation(skyBoxProgram, "v_matrix");
        pSkyPMat = glGetUniformLocation(skyBoxProgram, "p_matrix");
    }
}
