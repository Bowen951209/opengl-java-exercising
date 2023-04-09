package chapter7.program7_1;


import chapter6.Torus;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import utilities.*;

import java.nio.file.Path;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;


public class Program7_1 {
    private static long windowHandle;

    private static float cameraX, cameraY, cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static int[] vbo;
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int projLoc, mvLoc,nLoc, brickTexture;

    private static final Vector3f currentLightPos = new Vector3f(); // 在模型和視覺空間中的光照位置

    // 初始化光照位置
    private static final Vector3f initialLightLoc = new Vector3f(5.0f, 2.0f, 2.0f);

    // 白光特性
    private static final float[] globalAmbient = { 0.7f, 0.7f, 0.7f, 1.0f };
    private static final float[] lightAmbient = { 0.0f, 0.0f, 0.0f, 1.0f };
    private static final float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static final float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };

    // 黃金材質特性
    private static final float[] matAmb = Materials.goldAmbient();
    private static final float[] matDif = Materials.goldDiffuse();
    private static final float[] matSpe = Materials.goldSpecular();
    private static final float matShi = Materials.goldShininess();

    private static final float[] lightPos = new float[3];
    private static Torus myTorus;
    private static int program;


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

        program = new ShaderProgramSetter(Path.of("src/main/java/chapter7/program7_1/shaders/vertShader.glsl")
                , Path.of("src/main/java/chapter7/program7_1/shaders/fragShader.glsl"))
                .getProgram();

        cameraX = 0f; cameraY = 0f; cameraZ = 4f;
        setupVertices();
        brickTexture = new TextureReader("src/main/java/chapter5/textures/brick.jpg").getTexID();

        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);

        mvLoc = glGetUniformLocation(program, "mv_matrix");
        projLoc = glGetUniformLocation(program, "proj_matrix");
        nLoc = glGetUniformLocation(program, "norm_matrix");
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


            Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ);
            float torLocZ = 0f;
            float torLocY = 0f;
            float torLocX = 0f;
            Matrix4f mMat = new Matrix4f().translate(torLocX, torLocY, torLocZ).rotateX(toRadians(35f));
            Matrix4f mvMat = vMat.mul(mMat);

            // 構建m矩陣的逆轉置矩陣，以變換法向量
            Matrix4f invTrMat = mMat.invert().transpose();


            glUniformMatrix4fv(projLoc, false, pMat.get(vals));
            glUniformMatrix4fv(mvLoc, false, mvMat.get(vals));
            glUniformMatrix4fv(nLoc, false, invTrMat.get(vals));

            // 基於當前光源位置，初始化光照
            currentLightPos.set(initialLightLoc);
            installLight();

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, brickTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glGenerateMipmap(GL_TEXTURE_2D);

            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);

            glDrawElements(GL_TRIANGLES, myTorus.getNumIndices(), GL_UNSIGNED_INT, 0);

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
        int globalAmbLoc = glGetUniformLocation(program, "globalAmbient");
        int ambLoc = glGetUniformLocation(program, "light.ambient");
        int diffLoc = glGetUniformLocation(program, "light.diffuse");
        int specLoc = glGetUniformLocation(program, "light.specular");
        int posLoc = glGetUniformLocation(program, "light.position");
        int mAmbLoc = glGetUniformLocation(program, "material.ambient");
        int mDiffLoc = glGetUniformLocation(program, "material.diffuse");
        int mSpecLoc = glGetUniformLocation(program, "material.specular");
        int mShiLoc = glGetUniformLocation(program, "material.shininess");

        // 在著色器中為光源材質統一變量賦值
        glProgramUniform4fv(program, globalAmbLoc, globalAmbient);
        glProgramUniform4fv(program, ambLoc, lightAmbient);
        glProgramUniform4fv(program, diffLoc, lightDiffuse);
        glProgramUniform4fv(program, specLoc, lightSpecular);
        glProgramUniform3fv(program, posLoc, lightPos);
        glProgramUniform4fv(program, mAmbLoc, matAmb);
        glProgramUniform4fv(program, mDiffLoc, matDif);
        glProgramUniform4fv(program, mSpecLoc, matSpe);
        glProgramUniform1f(program, mShiLoc, matShi);
    }


    private static void setupVertices() {
        myTorus = new Torus(.5f, .2f, 48);

        int numTorusVertices = myTorus.getNumVertices();
        Vector3f[ ] vertices = myTorus.getVertices();
        Vector2f[ ] texCoords = myTorus.getTexCoords();
        Vector3f[ ] normals = myTorus.getNormals();
        int[] indices = myTorus.getIndicesInArray();
        float[ ] pvalues = new float[vertices.length*3];
        float[ ] tvalues = new float[texCoords.length*2];
        float[ ] nvalues = new float[normals.length*3];
        for (int i=0; i<numTorusVertices; i++)
        {	 pvalues[i*3] = vertices[i].x;	 	 // vertex position
            pvalues[i*3+1] = vertices[i].y;
            pvalues[i*3+2] =  vertices[i].z;

            tvalues[i*2] =  texCoords[i].x;	 	 // texture coordinates
            tvalues[i*2+1] =  texCoords[i].y;

            nvalues[i*3] =  normals[i].x;	 	 // normal vector
            nvalues[i*3+1] =  normals[i].y;
            nvalues[i*3+2] =  normals[i].z;
        }



        int[] vao = new int[1];
        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);

        vbo = new int[4];
        glGenBuffers(vbo);
        // put the vertices into buffer #0
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER,  pvalues, GL_STATIC_DRAW);
        // put the texture coordinates into buffer #1
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, tvalues, GL_STATIC_DRAW);
        // put the normals into buffer #2
        glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
        glBufferData(GL_ARRAY_BUFFER, nvalues, GL_STATIC_DRAW);
        // indices into buffer #3
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }


}
