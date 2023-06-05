package chapter9.program9_3.launcher;


import chapter6.Torus;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import utilities.Camera;
import utilities.Color;
import utilities.GLFWWindow;
import utilities.ShaderProgramSetter;
import utilities.callbacks.defaultCBs.DefaultCallbacks;
import utilities.readers.CubeMapReader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
public class Program9_3 {

    private static long windowID;

    public static long getWindowID() {
        return windowID;
    }

    private static final FloatBuffer valsOf16 = BufferUtils.createFloatBuffer(16);// utility buffer for transferring matrices
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
        System.out.println("Program exit and freed glfw.");
    }

    private static void init() {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        CAMERA.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, "第9章 環境貼圖");
        windowID = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));

        new DefaultCallbacks(windowID, CAMERA).bindToGLFW();

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glActiveTexture(GL_TEXTURE1);
        defaultProgram = new ShaderProgramSetter(Path.of("src/main/java/chapter9/program9_3/shaders/default/vertShader.glsl")
                , Path.of("src/main/java/chapter9/program9_3/shaders/default/fragShader.glsl"))
                .getProgram();
        skyBoxProgram = new ShaderProgramSetter(Path.of("src/main/java/chapter9/program9_3/shaders/skybox/CubeVertShader.glsl")
                , Path.of("src/main/java/chapter9/program9_3/shaders/skybox/SkyboxFragShader.glsl"))
                .getProgram();

        CubeMapReader skyboxTexture = new CubeMapReader("src/main/java/chapter9/program9_3/textures/skybox");
        skyboxTexture.bind();

        setupVertices();
        getAllUniformsLoc();

        System.out.println("Hint: You can press F1 to look around.");
    }

    private static void loop() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        CAMERA.updateVMat();
        Matrix4f torusMMat = new Matrix4f()
                .translate(TORUS_POS)
                .rotateX(toRadians((float) glfwGetTime() * 100f))
                .scale(2.5f);

        drawSkybox();
        drawScene(torusMMat);

        CAMERA.handle();

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }



    private static void setupVertices() {
        System.out.println("Loading models...");

        torus = new Torus(.5f, .2f, 48, true);

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
        glUniformMatrix4fv(pSkyVMat, false, CAMERA.getVMat().get(valsOf16));
        glUniformMatrix4fv(pSkyPMat, false, CAMERA.getProjMat().get(valsOf16));

        glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, 108);

        glEnable(GL_DEPTH_TEST);
    }

    private static void drawScene(Matrix4f torusMMat) {
        glUseProgram(defaultProgram);
        glEnable(GL_DEPTH_TEST);

        // 繪製torus
        Matrix4f mvMat = new Matrix4f(CAMERA.getVMat()).mul(torusMMat);
        glUniformMatrix4fv(pDefaultMvLoc, false, mvMat.get(valsOf16));
        glUniformMatrix4fv(pDefaultProjLoc, false, CAMERA.getProjMat().get(valsOf16));

        Matrix4f invTrMat = new Matrix4f(mvMat).invert().transpose();
        glUniformMatrix4fv(pDefaultNormLoc, false, invTrMat.get(valsOf16));

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
