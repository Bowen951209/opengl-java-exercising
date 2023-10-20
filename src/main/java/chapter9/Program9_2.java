/*
* This shaderProgram is a demo of skybox.*/

package chapter9;


import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.readers.ModelReader;
import engine.sceneComponents.Camera;
import engine.sceneComponents.Skybox;
import engine.sceneComponents.models.Torus;
import engine.util.Color;
import engine.util.Material;
import engine.util.ShadowFrameBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
public class Program9_2 {

    private static long windowHandle;
    private static Skybox skybox;

    public static long getWindowHandle() {
        return windowHandle;
    }

    private static final FloatBuffer valsOf16 = BufferUtils.createFloatBuffer(16);// utility buffer for transferring matrices
    private static final FloatBuffer valsOf3 = BufferUtils.createFloatBuffer(3);
    private static final int[] vbo = new int[9], vao = new int[1];

    private static final Vector3f LIGHT_POS = new Vector3f(-3.8f, 2.2f, 1.1f);
    private static final Vector3f TORUS_POS = new Vector3f(1.6f, 0f, -.3f);
    private static final Vector3f PYRAMID_POS = new Vector3f(-1f, .1f, .3f);
    private static final Vector3f GRID_POS = new Vector3f(0f, 8.5f, -9.5f);

    // 白光特性
    private static final float[] GLOBAL_AMBIENT = {0.7f, 0.7f, 0.7f, 1.0f};
    private static final float[] LIGHT_AMBIENT = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] LIGHT_DIFFUSE = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] LIGHT_SPECULAR = {1.0f, 1.0f, 1.0f, 1.0f};

    private static Torus torus;
    private static int renderingProgram1, renderingProgram2;


    private static ModelReader pyramid, grid;
    private static int p1shadowMVPLoc;
    private static int p2mvLoc;
    private static int p2projLoc;
    private static int p2nLoc;
    private static int p2sLoc;
    private static int p2mshiLoc;
    private static int p2ambLoc;
    private static int p2globalAmbLoc;
    private static int p2diffLoc;
    private static int p2specLoc;
    private static int p2posLoc;
    private static int p2mambLoc;
    private static int p2mdiffLoc;
    private static int p2mspecLoc;
    private static int p2FrameBufferWidthLoc;
    private static int p2FrameBufferHeightLoc;
    private static final Vector3f ORIGIN = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Vector3fc UP = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Matrix4f B = new Matrix4f(
            .5f, 0f, 0f, 0f,
            0f, .5f, 0f, 0f,
            0f, 0f, .5f, 0f,
            .5f, .5f, .5f, 1f
    );
    private static Matrix4f lightPMat, lightVMat;
    private static ShadowFrameBuffer shadowFrameBuffer;

    private static final Camera camera = new Camera().sensitive(.04f).step(.05f);
    private static final P9_2CursorCB CURSOR_CB = new P9_2CursorCB().setCamera(camera);


    public static void main(String[] args) {
        init();
        // 迴圈
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            loop();
        }
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("App exit and freed glfw.");
    }

    private static void init() {
        final int WINDOW_INIT_W = 1500, WINDOW_INIT_H = 1000;
        camera.setProjMat(WINDOW_INIT_W, WINDOW_INIT_H);
        GLFWWindow glfwWindow = new GLFWWindow(WINDOW_INIT_W, WINDOW_INIT_H, "第9章 天空盒");
        windowHandle = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        shadowFrameBuffer = new ShadowFrameBuffer(windowHandle);
        glfwSetFramebufferSizeCallback(windowHandle, new P9_2FrameBufferResizeCB(camera, shadowFrameBuffer));
        glfwSetCursorPosCallback(windowHandle, CURSOR_CB);
        glfwSetKeyCallback(windowHandle, new P9_2KeyCB(camera, CURSOR_CB));
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glActiveTexture(GL_TEXTURE0);
        glActiveTexture(GL_TEXTURE1);
        renderingProgram1 = new ShaderProgram(Path.of("assets/shaders/program9_2/vert1Shader.glsl")
                , Path.of("assets/shaders/program9_2/frag1Shader.glsl"))
                .getID();
        renderingProgram2 = new ShaderProgram(Path.of("assets/shaders/program9_2/vert2Shader.glsl")
                , Path.of("assets/shaders/program9_2/frag2Shader.glsl"))
                .getID();


        setupVertices();
        getAllUniformsLoc();

        System.out.println("Hint: You can press F1 to look around.");
    }

    private static void loop() {
        LIGHT_POS.rotateY(.01f);
        // ROUND1 從光源處渲染

        // 使用自定義幀緩衝區
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer.getShadowFrameBuffer());

        Matrix4f torusMMat = new Matrix4f().translate(TORUS_POS).rotateX(toRadians(30f)).rotateY(toRadians(40f));
        Matrix4f pyramidMMat = new Matrix4f().translate(PYRAMID_POS).rotateX(toRadians(25f));
        Matrix4f gridMMat = new Matrix4f().translate(GRID_POS).scale(1);

        passOne(torusMMat, pyramidMMat, gridMMat);

        // ROUND2 從相機處渲染
        // 使用顯示緩衝區，重新繪製
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        passTwo(torusMMat, pyramidMMat, gridMMat, GLFWWindow.getFrameBufferSize(windowHandle));

        camera.handle();

        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }


    private static void passOne(Matrix4f torusMMat, Matrix4f pyramidMMat, Matrix4f gridMMat) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(renderingProgram1);

        lightVMat = new Matrix4f().lookAt(LIGHT_POS, ORIGIN, UP);
        lightPMat = camera.getProjMat(); // lightPMat 用的參數跟相機都是一樣的。
        // 繪製torus
        Matrix4f shadowMVP1 = new Matrix4f().mul(lightPMat).mul(lightVMat).mul(torusMMat);

        glUniformMatrix4fv(p1shadowMVPLoc, false, shadowMVP1.get(valsOf16));
        torus.draw(GL_TRIANGLES);

        glBindVertexArray(vao[0]);
        // 繪製pyramid
        shadowMVP1 = new Matrix4f().mul(lightPMat).mul(lightVMat).mul(pyramidMMat);

        glUniformMatrix4fv(p1shadowMVPLoc, false, shadowMVP1.get(valsOf16));
        glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumOfVertices());

        // 繪製grid
        shadowMVP1 = new Matrix4f().mul(lightPMat).mul(lightVMat).mul(gridMMat);

        glUniformMatrix4fv(p1shadowMVPLoc, false, shadowMVP1.get(valsOf16));
        glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, grid.getNumOfVertices());
    }

    private static void setupVertices() {
        System.out.println("Loading models...");

        torus = new Torus(.5f, .2f, 48, true);
        pyramid = new ModelReader("assets/models/pyr.obj");
        grid = new ModelReader("assets/models/bigCube.obj");

        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);

        glGenBuffers(vbo);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        // Pyramid
        glBindBuffer(GL_ARRAY_BUFFER, vbo[4]); // #4: 頂點
        glBufferData(GL_ARRAY_BUFFER, pyramid.getPvalue(), GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[5]); // #5: 法向量
        glBufferData(GL_ARRAY_BUFFER, pyramid.getPvalue(), GL_STATIC_DRAW);

        // grid
        glBindBuffer(GL_ARRAY_BUFFER, vbo[6]); // #6: 頂點
        glBufferData(GL_ARRAY_BUFFER, grid.getPvalue(), GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[7]); // #7: 法向量
        glBufferData(GL_ARRAY_BUFFER, grid.getPvalue(), GL_STATIC_DRAW);

        // skybox
        skybox = new Skybox(camera, "assets/textures/skycubes/fluffyClouds");
        System.out.println("Model load done.");
    }

    private static void passTwo(Matrix4f torusMMat, Matrix4f pyramidMMat, Matrix4f gridMMat, int[] frameBufferSize) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.updateVMat();

        // Draw skybox
        skybox.draw();

        // Draw scene
        glUseProgram(renderingProgram2);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        glUniform1i(p2FrameBufferWidthLoc, frameBufferSize[0]);
        glUniform1i(p2FrameBufferHeightLoc, frameBufferSize[1]);

        // 繪製torus
        setupLights(Material.GOLD_AMBIENT, Material.GOLD_DIFFUSE, Material.GOLD_SPECULAR, Material.GOLD_SHININESS);
        Matrix4f mvMat = new Matrix4f(camera.getVMat()).mul(torusMMat);
        Matrix4f invTrMat = new Matrix4f(mvMat).invert().transpose();
        Matrix4f shadowMVP2 = new Matrix4f(B).mul(lightPMat).mul(lightVMat).mul(torusMMat);
        glUniformMatrix4fv(p2mvLoc, false, mvMat.get(valsOf16));
        glUniformMatrix4fv(p2projLoc, false, camera.getProjMat().get(valsOf16));
        glUniformMatrix4fv(p2nLoc, false, invTrMat.get(valsOf16));
        glUniformMatrix4fv(p2sLoc, false, shadowMVP2.get(valsOf16));

        torus.draw(GL_TRIANGLES);

        glBindVertexArray(vao[0]);

        // 繪製pyramid
        setupLights(Material.BRONZE_AMBIENT, Material.BRONZE_DIFFUSE, Material.BRONZE_SPECULAR, Material.BRONZE_SHININESS);
        mvMat = new Matrix4f(camera.getVMat()).mul(pyramidMMat);
        invTrMat = new Matrix4f(mvMat).invert().transpose();
        shadowMVP2 = new Matrix4f(B).mul(lightPMat).mul(lightVMat).mul(pyramidMMat);
        glUniformMatrix4fv(p2mvLoc, false, mvMat.get(valsOf16));
        glUniformMatrix4fv(p2projLoc, false, camera.getProjMat().get(valsOf16));
        glUniformMatrix4fv(p2nLoc, false, invTrMat.get(valsOf16));
        glUniformMatrix4fv(p2sLoc, false, shadowMVP2.get(valsOf16));

        glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumOfVertices());

        // 繪製grid
        setupLights(Material.SILVER_AMBIENT, Material.SILVER_DIFFUSE, Material.SILVER_SPECULAR, Material.SILVER_SHININESS);
        mvMat = new Matrix4f(camera.getVMat()).mul(gridMMat);
        invTrMat = new Matrix4f(mvMat).invert().transpose();
        shadowMVP2 = new Matrix4f(B).mul(lightPMat).mul(lightVMat).mul(gridMMat);
        glUniformMatrix4fv(p2mvLoc, false, mvMat.get(valsOf16));
        glUniformMatrix4fv(p2projLoc, false, camera.getProjMat().get(valsOf16));
        glUniformMatrix4fv(p2nLoc, false, invTrMat.get(valsOf16));
        glUniformMatrix4fv(p2sLoc, false, shadowMVP2.get(valsOf16));

        glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, grid.getNumOfVertices());
    }

    private static void setupLights(float[] matAmb, float[] matDif, float[] matSpe, float matShi) {
        glProgramUniform4fv(renderingProgram2, p2globalAmbLoc, GLOBAL_AMBIENT);
        glProgramUniform4fv(renderingProgram2, p2ambLoc, LIGHT_AMBIENT);
        glProgramUniform4fv(renderingProgram2, p2diffLoc, LIGHT_DIFFUSE);
        glProgramUniform4fv(renderingProgram2, p2specLoc, LIGHT_SPECULAR);
        glProgramUniform3fv(renderingProgram2, p2posLoc, LIGHT_POS.get(valsOf3));
        glProgramUniform4fv(renderingProgram2, p2mambLoc, matAmb);
        glProgramUniform4fv(renderingProgram2, p2mdiffLoc, matDif);
        glProgramUniform4fv(renderingProgram2, p2mspecLoc, matSpe);
        glProgramUniform1f(renderingProgram2, p2mshiLoc, matShi);
    }

    private static void getAllUniformsLoc() {
        p1shadowMVPLoc = glGetUniformLocation(renderingProgram1, "shadowMVP");
        p2mvLoc = glGetUniformLocation(renderingProgram2, "mv_matrix");
        p2projLoc = glGetUniformLocation(renderingProgram2, "proj_matrix");
        p2nLoc = glGetUniformLocation(renderingProgram2, "norm_matrix");
        p2sLoc = glGetUniformLocation(renderingProgram2, "shadowMVP");
        p2globalAmbLoc = glGetUniformLocation(renderingProgram2, "globalAmbient");
        p2ambLoc = glGetUniformLocation(renderingProgram2, "light.ambient");
        p2diffLoc = glGetUniformLocation(renderingProgram2, "light.diffuse");
        p2specLoc = glGetUniformLocation(renderingProgram2, "light.specular");
        p2posLoc = glGetUniformLocation(renderingProgram2, "light.position");
        p2mambLoc = glGetUniformLocation(renderingProgram2, "material.ambient");
        p2mdiffLoc = glGetUniformLocation(renderingProgram2, "material.diffuse");
        p2mspecLoc = glGetUniformLocation(renderingProgram2, "material.specular");
        p2mshiLoc = glGetUniformLocation(renderingProgram2, "material.shininess");
        p2FrameBufferWidthLoc = glGetUniformLocation(renderingProgram2, "frameBufferSize.width");
        p2FrameBufferHeightLoc = glGetUniformLocation(renderingProgram2, "frameBufferSize.height");
    }
}
