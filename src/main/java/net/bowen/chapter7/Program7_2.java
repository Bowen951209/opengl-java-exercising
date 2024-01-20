package net.bowen.chapter7;


import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Color;
import net.bowen.engine.util.Material;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import net.bowen.engine.readers.ModelReader;
import net.bowen.engine.readers.TextureReader;

import java.nio.FloatBuffer;
import java.nio.file.Path;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;


public class Program7_2 {
    private static long windowHandle;

    private static float cameraX, cameraY, cameraZ;

    private static Matrix4f pMat;
    private static final FloatBuffer vals = BufferUtils.createFloatBuffer(16);// utility buffer for transferring matrices
    private static int[] vbo;
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int projLoc, mvLoc, nLoc;

    private static final Vector3f currentLightPos = new Vector3f(); // 在模型和視覺空間中的光照位置

    // 初始化光照位置
    private static final Vector3f initialLightLoc = new Vector3f(5.0f, 2.0f, 2.0f);

    // 白光特性
    private static final float[] globalAmbient = {0.7f, 0.7f, 0.7f, 1.0f};
    private static final float[] lightAmbient = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};

    private static final float matShi = Material.GOLD_SHININESS;

    private static final FloatBuffer lightPos = BufferUtils.createFloatBuffer(3);

    private static int blinnPhongProgram;
    private static final ModelReader DOLPHIN = new ModelReader("assets/models/dolphinHighPoly.obj");
    private static TextureReader dolphinTex;
    private static int globalAmbLoc, ambLoc, diffLoc, mShiLoc, specLoc, posLoc;


    public static void main(String[] args) {
        init();
        loop();
        GLFW.glfwTerminate();
        System.out.println("App exit and freed glfw.");
    }

    private static void init() {
        final int windowCreatedW = 800, windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "Texture2D + Light");
        windowHandle = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        createProjMat(windowCreatedW, windowCreatedH);
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);

        blinnPhongProgram = new ShaderProgram(Path.of("assets/shaders/program7_2/VertexShader.glsl")
                , Path.of("assets/shaders/program7_2/FragmentShader.glsl"))
                .getID();
        dolphinTex = new Texture2D(0, "assets/textures/imageTextures/Dolphin_HighPolyUV.png");

        cameraX = 0f;
        cameraY = 0f;
        cameraZ = 2f;
        setupVertices();

        glUseProgram(blinnPhongProgram);
        System.out.println("Using ProgramID: " + blinnPhongProgram);
        getAllUniformsLoc(blinnPhongProgram);
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_CULL_FACE);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, dolphinTex.getTexID());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glGenerateMipmap(GL_TEXTURE_2D);

            Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ);
            Matrix4f mMat = new Matrix4f().rotateX(toRadians(35f)).rotateY(toRadians(-70f));
            Matrix4f mvMat = vMat.mul(mMat);

            // 構建m矩陣的逆轉置矩陣，以變換法向量
            Matrix4f invTrMat = mMat.invert().transpose();

            glUniformMatrix4fv(projLoc, false, pMat.get(vals));
            glUniformMatrix4fv(mvLoc, false, mvMat.get(vals));
            glUniformMatrix4fv(nLoc, false, invTrMat.get(vals));

            // 基於當前光源位置，初始化光照
            currentLightPos.set(initialLightLoc);
            currentLightPos.rotateZ(toRadians((float) (glfwGetTime() * 25f)));
            installLight();

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // vbo[0]: 頂點
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);// vbo[1]: 紋理座標
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);// vbo[2]: 法向量
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);

            glDrawArrays(GL_TRIANGLES, 0, DOLPHIN.getNumOfVertices());

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    private static void installLight() {
        lightPos.put(currentLightPos.x());
        lightPos.put(currentLightPos.y());
        lightPos.put(currentLightPos.z());
        lightPos.flip();

        // 在著色器中為光源材質統一變量賦值
        glUniform4fv(globalAmbLoc, globalAmbient);
        glUniform4fv(ambLoc, lightAmbient);
        glUniform4fv(diffLoc, lightDiffuse);
        glUniform4fv(specLoc, lightSpecular);
        glUniform3fv(posLoc, lightPos);
        glUniform1f(mShiLoc, matShi);
    }


    private static void setupVertices() {

        int[] vao = new int[1];
        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);

        vbo = new int[4];
        glGenBuffers(vbo);
        // put the vertices into buffer #0
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, DOLPHIN.getPvalue(), GL_STATIC_DRAW);
        // put tex coords into buffer #1
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, DOLPHIN.getTvalue(), GL_STATIC_DRAW);
        // put the normals into buffer #2
        glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        glBufferData(GL_ARRAY_BUFFER, DOLPHIN.getNvalue(), GL_STATIC_DRAW);

    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

    private static void getAllUniformsLoc(int program) {
        mvLoc = glGetUniformLocation(program, "mv_matrix");
        projLoc = glGetUniformLocation(program, "proj_matrix");
        nLoc = glGetUniformLocation(program, "norm_matrix");

        globalAmbLoc = glGetUniformLocation(blinnPhongProgram, "globalAmbient");
        ambLoc = glGetUniformLocation(blinnPhongProgram, "light.ambient");
        diffLoc = glGetUniformLocation(blinnPhongProgram, "light.diffuse");
        specLoc = glGetUniformLocation(blinnPhongProgram, "light.specular");
        posLoc = glGetUniformLocation(blinnPhongProgram, "light.position");
        mShiLoc = glGetUniformLocation(blinnPhongProgram, "material.shininess");
    }
}
