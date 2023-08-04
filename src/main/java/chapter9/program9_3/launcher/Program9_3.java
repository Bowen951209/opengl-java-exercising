package chapter9.program9_3.launcher;


import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import engine.ShaderProgram;
import engine.sceneComponents.Camera;
import engine.util.Color;
import engine.GLFWWindow;
import chapter9.program9_3.callbacks.P9_3Callbacks;
import engine.sceneComponents.models.Torus;
import engine.sceneComponents.textures.CubeMapTexture;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL43.*;
import static engine.util.ValuesContainer.VALS_OF_16;

public class Program9_3 {

    private static long windowID;

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
        defaultProgram = new ShaderProgram(Path.of("src/main/java/chapter9/program9_3/shaders/default/vertShader.glsl")
                , Path.of("src/main/java/chapter9/program9_3/shaders/default/fragShader.glsl"))
                .getID();
        skyBoxProgram = new ShaderProgram(Path.of("src/main/java/chapter9/program9_3/shaders/skybox/CubeVertShader.glsl")
                , Path.of("src/main/java/chapter9/program9_3/shaders/skybox/SkyboxFragShader.glsl"))
                .getID();

        CubeMapTexture skyboxTexture = new CubeMapTexture("src/main/java/chapter9/program9_3/textures/skybox");
        skyboxTexture.bind();

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

        FloatBuffer pvalues = BufferUtils.createFloatBuffer(torus.getVertices().length * 3);
        FloatBuffer nvalues = BufferUtils.createFloatBuffer(torus.getNormals().length * 3);
        IntBuffer indices = torus.getIndicesInBuffer();
        for (int i = 0; i < torus.getNumVertices(); i++) {
            pvalues.put(torus.getVertices()[i].x());         // vertex position
            pvalues.put(torus.getVertices()[i].y());
            pvalues.put(torus.getVertices()[i].z());

            nvalues.put(torus.getNormals()[i].x());         // normal vector
            nvalues.put(torus.getNormals()[i].y());
            nvalues.put(torus.getNormals()[i].z());
        }
        pvalues.flip(); // 此行非常必要!
        nvalues.flip();
        indices.flip();

        // Torus
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // #0: 頂點
        glBufferData(GL_ARRAY_BUFFER, pvalues, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[2]); // #2: 法向量
        glBufferData(GL_ARRAY_BUFFER, nvalues, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]); // #3: 索引
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // skybox
        float[] skyboxVertices = {	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
                1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
                1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
        };
        glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        glBufferData(GL_ARRAY_BUFFER, skyboxVertices, GL_STATIC_DRAW);

        System.out.println("Model load done.");
    }

    private static void drawSkybox() {
        glUseProgram(skyBoxProgram);

        glDisable(GL_DEPTH_TEST);
        glUniformMatrix4fv(pSkyVMat, false, CAMERA.getVMat().get(VALS_OF_16));
        glUniformMatrix4fv(pSkyPMat, false, CAMERA.getProjMat().get(VALS_OF_16));

        glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, 108);

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

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
        glDrawElements(GL_TRIANGLES, torus.getNumIndices(), GL_UNSIGNED_INT, 0);
    }

    private static void getAllUniformsLoc() {
        pDefaultMvLoc = glGetUniformLocation(defaultProgram, "mv_matrix");
        pDefaultProjLoc = glGetUniformLocation(defaultProgram, "proj_matrix");
        pDefaultNormLoc = glGetUniformLocation(defaultProgram, "norm_matrix");

        pSkyVMat = glGetUniformLocation(skyBoxProgram, "v_matrix");
        pSkyPMat = glGetUniformLocation(skyBoxProgram, "p_matrix");
    }
}
