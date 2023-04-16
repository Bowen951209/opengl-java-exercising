package chapter7.lighting_and_texture;

import clojure.core.Vec;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import utilities.*;

import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL41.*;

public class Main {
    private static long windowHandle;

    private static float cameraX;
    private static float cameraY;
    private static float cameraZ;

    private static Matrix4f pMat;
//    private static final float[] vals = new float[16];
    private static final FloatBuffer vals = BufferUtils.createFloatBuffer(16);// utility buffer for transferring matrices
    private static int[] vbo;
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int mvLoc, projLoc, globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mAmbLoc, mDiffLoc, mSpecLoc, mShiLoc, nLoc;
    private static ModelReader myModel;
    private static int windowCreatedW, windowCreatedH;
    private static int program;
    private static Matrix4f mvMat;

    // 白光特性
    private static float[] initLightPosInArr = {5.0f, 2.0f, 2.0f};
    private static FloatBuffer lightPos = BufferUtils.createFloatBuffer(3);
    private static final float[] globalAmbient = {0.7f, 0.7f, 0.7f, 1.0f};
    private static final float[] lightAmbient = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};

    // 黃金材質特性
    private static final float[] matAmb = Materials.goldAmbient();
    private static final float[] matDif = Materials.goldDiffuse();
    private static final float[] matSpe = Materials.goldSpecular();
    private static final float matShi = Materials.goldShininess();
    private static Matrix4f invTrMat;
    private static Matrix4f mMat;


    public static void main(String[] args) {
        init();
        loop();
        GLFW.glfwTerminate();
        System.out.println("Program exit and freed glfw.");
    }

    private static void init() {
        setupWindow();
        setupCallbacks();
        setupProgram();
        setupUniLocs();
        setupCamera(windowCreatedW, windowCreatedH);
        setupTexture();
        setupVertices();
        setupLight(new Vector3f(initLightPosInArr));
    }

    private static void setupLight(Vector3f pos) {
        lightPos.put(pos.x());
        lightPos.put(pos.y());
        lightPos.put(pos.z());
        lightPos.flip();
    }

    private static void setupUniLocs() {
        mvLoc = glGetUniformLocation(program, "mv_matrix");
        projLoc = glGetUniformLocation(program, "proj_matrix");


        globalAmbLoc = glGetUniformLocation(program, "globalAmbient");
        ambLoc = glGetUniformLocation(program, "light.ambient");
        diffLoc = glGetUniformLocation(program, "light.diffuse");
        specLoc = glGetUniformLocation(program, "light.specular");
        posLoc = glGetUniformLocation(program, "light.position");
        mAmbLoc = glGetUniformLocation(program, "material.ambient");
        mDiffLoc = glGetUniformLocation(program, "material.diffuse");
        mSpecLoc = glGetUniformLocation(program, "material.specular");
        mShiLoc = glGetUniformLocation(program, "material.shininess");
        nLoc = glGetUniformLocation(program, "norm_matrix");
    }

    private static void setupProgram() {
        program = new ShaderProgramSetter(Path.of("src/main/java/chapter7/lighting_and_texture/shaders/VertexShader.glsl")
                , Path.of("src/main/java/chapter7/lighting_and_texture/shaders/FragmentShader.glsl"))
                .getProgram();
        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);
    }

    private static void setupCallbacks() {
        // 設定frameBuffer大小改變callback
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);
    }

    private static void setupWindow() {
        windowCreatedW = 800;
        windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "Texture + Lighting");
        windowHandle = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
    }

    private static void setupTexture() {
        int texture = new TextureReader("src/main/java/chapter7/lighting_and_texture/textures/Dolphin_HighPolyUV.png").getTexID();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    private static void setupCamera(int winW, int winH) {
        cameraX = 0f;
        cameraY = 0f;
        cameraZ = 4f;
        // 一開始要先呼叫，才能以長、寬構建透視矩陣
        createProjMat(winW, winH);
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_CULL_FACE | GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);

            mMat = new Matrix4f().translate(0, 0, 0);
            Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ).rotateY(toRadians(-80f)).scale(3f);
            mvMat = mMat.mul(vMat);



            invTrMat = new Matrix4f(mMat).invert().transpose();
            setupLight(new Vector3f(lightPos).rotateZ((float) glfwGetTime()));
            putUniforms();


            // #0 頂點座標
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            // #1 紋理座標
            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            // #2 法向量
            glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);

            glDrawArrays(GL_TRIANGLES, 0, myModel.getNumOfVectors());

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    private static void putUniforms() {
        glUniformMatrix4fv(mvLoc, false, mvMat.get(vals));
        glUniformMatrix4fv(projLoc, false, pMat.get(vals));
        glUniform4fv(nLoc, invTrMat.get(vals));
        glUniform4fv(globalAmbLoc, globalAmbient);
        glUniform4fv(ambLoc, lightAmbient);
        glUniform4fv(diffLoc, lightDiffuse);
        glUniform4fv(specLoc, lightSpecular);
        glUniform3fv(posLoc, lightPos);
        glUniform4fv(mAmbLoc, matAmb);
        glUniform4fv(mDiffLoc, matDif);
        glUniform4fv(mSpecLoc, matSpe);
        glUniform1f(mShiLoc, matShi);
    }


    private static void setupVertices() {
        myModel = new ModelReader("src/main/java/chapter7/lighting_and_texture/models/dolphinHighPoly.obj");

        int[] vao = new int[1];
        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);
// TODO: 2023/4/15 將model reader新增buffer模式
        vbo = new int[3];
        glGenBuffers(vbo);
        // #0 頂點座標
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, myModel.getPvalue(), GL_STATIC_DRAW);
        // #1 紋理座標
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, myModel.getTvalue(), GL_STATIC_DRAW);
        // #2 法向量
        glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        glBufferData(GL_ARRAY_BUFFER, myModel.getNvalue(), GL_STATIC_DRAW);

    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }
}
