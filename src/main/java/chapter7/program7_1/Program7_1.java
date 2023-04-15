package chapter7.program7_1;


import chapter6.Torus;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import utilities.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;


public class Program7_1 {
    private static long windowHandle;

    private static float cameraX, cameraY, cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    static int[] vbo;
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

    // 黃金材質特性
    private static final float[] matAmb = Materials.goldAmbient();
    private static final float[] matDif = Materials.goldDiffuse();
    private static final float[] matSpe = Materials.goldSpecular();
    private static final float matShi = Materials.goldShininess();

    private static final float[] lightPos = new float[3];
    private static Torus myTorus;
    static int gouraudProgram;
    static int phongProgram;
    static int blinnPhongProgram;
    static int currentProgram;


    private static ModelReader dolphin, stanfordBunny, stanfordDragon;
    static String usingModel = "stanford-dragon";
    private static IntBuffer indices;
    private static FloatBuffer pvalues, nvalues;

    public static void main(String[] args) {
        init();
        // 迴圈
        loop();
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("Program exit and freed glfw.");
    }

    private static void init() {
        final int windowCreatedW = 800, windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "第7章");
        windowHandle = glfwWindow.getWindowHandle();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        createProjMat(windowCreatedW, windowCreatedH);
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);


        gouraudProgram = new ShaderProgramSetter(Path.of("src/main/java/chapter7/program7_1/shaders/gouraud/vertShader.glsl")
                , Path.of("src/main/java/chapter7/program7_1/shaders/gouraud/fragShader.glsl"))
                .getProgram();
        phongProgram = new ShaderProgramSetter(Path.of("src/main/java/chapter7/program7_1/shaders/phong/vertShader.glsl")
                , Path.of("src/main/java/chapter7/program7_1/shaders/phong/fragShader.glsl"))
                .getProgram();
        blinnPhongProgram = new ShaderProgramSetter(Path.of("src/main/java/chapter7/program7_1/shaders/blinnPhong/vertShader.glsl")
                , Path.of("src/main/java/chapter7/program7_1/shaders/blinnPhong/fragShader.glsl"))
                .getProgram();

        cameraX = 0f;
        cameraY = 0f;
        cameraZ = 2f;
        setupVertices();

        currentProgram = gouraudProgram;
        System.out.println("Using ProgramID: " + currentProgram);
        System.out.println("Press 1, 2 or 3 to change program!");

        getAllUniformsLoc(currentProgram);
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(currentProgram);

            Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ);
            float torLocZ = 0f;
            float torLocY = 0f;
            float torLocX = 0f;
            Matrix4f mMat = new Matrix4f().translate(torLocX, torLocY, torLocZ);
            switch (usingModel) {
                case "torus" -> mMat.rotateX(toRadians(35f));
                case "dolphin" -> mMat.rotateZ(toRadians(20f)).rotateY(toRadians(-80f));
                case "stanford-bunny" -> mMat.scale(10f).translate(0f, -.1f, 0f);
                case "stanford-dragon" -> mMat.scale(10f).rotateY(toRadians(-90f)).rotateZ(toRadians(90f));
            }
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

            glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);// vbo[2]: 法向量
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);


            switch (usingModel) {
                case "torus" -> glDrawElements(GL_TRIANGLES, myTorus.getNumIndices(), GL_UNSIGNED_INT, 0);
                case "dolphin" -> glDrawArrays(GL_TRIANGLES, 0, dolphin.getNumOfVectors());
                case "stanford-bunny" -> glDrawArrays(GL_TRIANGLES, 0, stanfordBunny.getNumOfVectors());
                case "stanford-dragon" -> glDrawArrays(GL_TRIANGLES, 0, stanfordDragon.getNumOfVectors());
            }


            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    private static void installLight() {
        // save the light position in a float array
        lightPos[0] = currentLightPos.x();
        lightPos[1] = currentLightPos.y();
        lightPos[2] = currentLightPos.z();

        // 在著色器中獲取光源位置和材質屬性
        int globalAmbLoc = glGetUniformLocation(currentProgram, "globalAmbient");
        int ambLoc = glGetUniformLocation(currentProgram, "light.ambient");
        int diffLoc = glGetUniformLocation(currentProgram, "light.diffuse");
        int specLoc = glGetUniformLocation(currentProgram, "light.specular");
        int posLoc = glGetUniformLocation(currentProgram, "light.position");
        int mAmbLoc = glGetUniformLocation(currentProgram, "material.ambient");
        int mDiffLoc = glGetUniformLocation(currentProgram, "material.diffuse");
        int mSpecLoc = glGetUniformLocation(currentProgram, "material.specular");
        int mShiLoc = glGetUniformLocation(currentProgram, "material.shininess");

        // 在著色器中為光源材質統一變量賦值
        glProgramUniform4fv(currentProgram, globalAmbLoc, globalAmbient);
        glProgramUniform4fv(currentProgram, ambLoc, lightAmbient);
        glProgramUniform4fv(currentProgram, diffLoc, lightDiffuse);
        glProgramUniform4fv(currentProgram, specLoc, lightSpecular);
        glProgramUniform3fv(currentProgram, posLoc, lightPos);
        glProgramUniform4fv(currentProgram, mAmbLoc, matAmb);
        glProgramUniform4fv(currentProgram, mDiffLoc, matDif);
        glProgramUniform4fv(currentProgram, mSpecLoc, matSpe);
        glProgramUniform1f(currentProgram, mShiLoc, matShi);
    }


    private static void setupVertices() {
        System.out.println("Loading models...");
        myTorus = new Torus(.5f, .2f, 48, true);
        dolphin = new ModelReader("src/main/java/chapter7/program7_1/models/dolphinHighPoly.obj");
        stanfordBunny = new ModelReader("src/main/java/chapter7/program7_1/models/stanford-bunny.obj");
        stanfordDragon = new ModelReader("src/main/java/chapter7/program7_1/models/stanford-dragon.obj");

        int[] vao = new int[1];
        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);

        vbo = new int[4];
        glGenBuffers(vbo);
        glfwSetKeyCallback(windowHandle, new KeyPressCallBack());
        int numTorusVertices = myTorus.getNumVertices();
        Vector3f[] vertices = myTorus.getVertices();
        Vector3f[] normals = myTorus.getNormals();
        indices = myTorus.getIndicesInBuffer();
        pvalues = BufferUtils.createFloatBuffer(vertices.length * 3);
        nvalues = BufferUtils.createFloatBuffer(normals.length * 3);
        for (int i = 0; i < numTorusVertices; i++) {
            pvalues.put(vertices[i].x());         // vertex position
            pvalues.put(vertices[i].y());
            pvalues.put(vertices[i].z());

            nvalues.put(normals[i].x());         // normal vector
            nvalues.put(normals[i].y());
            nvalues.put(normals[i].z());
        }
        pvalues.flip(); // 此行非常必要!
        nvalues.flip();
        indices.flip();
        storeInBuffer(vbo);

        System.out.println("Done");
    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

    private static void getAllUniformsLoc(int program) {
        mvLoc = glGetUniformLocation(program, "mv_matrix");
        projLoc = glGetUniformLocation(program, "proj_matrix");
        nLoc = glGetUniformLocation(program, "norm_matrix");
    }
    public static void storeInBuffer(int[] vbo) {

        switch (usingModel) {
            case "torus" -> {
                // put the vertices into buffer #0
                glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
                glBufferData(GL_ARRAY_BUFFER, pvalues, GL_STATIC_DRAW);
                // put the normals into buffer #2
                glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
                glBufferData(GL_ARRAY_BUFFER, nvalues, GL_STATIC_DRAW);
                // indices into buffer #3
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
            }
            case "dolphin" -> {
                // put the vertices into buffer #0
                glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
                glBufferData(GL_ARRAY_BUFFER, dolphin.getPvalue(), GL_STATIC_DRAW);
                // put the normals into buffer #2
                glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
                glBufferData(GL_ARRAY_BUFFER, dolphin.getNvalue(), GL_STATIC_DRAW);
            }
            case "stanford-bunny" -> {
                // put the vertices into buffer #0
                glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
                glBufferData(GL_ARRAY_BUFFER, stanfordBunny.getPvalue(), GL_STATIC_DRAW);
                // put the normals into buffer #2
                glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
                glBufferData(GL_ARRAY_BUFFER, stanfordBunny.getNvalue(), GL_STATIC_DRAW);
            }
            case "stanford-dragon" -> {
                // put the vertices into buffer #0
                glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
                glBufferData(GL_ARRAY_BUFFER, stanfordDragon.getPvalue(), GL_STATIC_DRAW);
                // put the normals into buffer #2
                glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
                glBufferData(GL_ARRAY_BUFFER, stanfordDragon.getNvalue(), GL_STATIC_DRAW);
            }
        }
    }
}
