package net.bowen.chapter6;


import net.bowen.engine.GLFWWindow;
import net.bowen.engine.ShaderProgram;
import net.bowen.engine.sceneComponents.models.Torus;
import net.bowen.engine.sceneComponents.textures.Texture2D;
import net.bowen.engine.util.Color;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;

import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;


public class Program6_2 {
    private static long windowHandle;

    private static float cameraX;
    private static float cameraY;
    private static float cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static int[] vbo;
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int projLoc;
    private static int mvLoc;
    private static int brickTexture;
    private static Torus myTorus;


    public static void main(String[] args) {
        init();
        // 迴圈
        loop();
        // 釋出
        GLFW.glfwTerminate();
        System.out.println("App exit and freed glfw.");


    }

    private static void init() {
        final int windowCreatedW = 800, windowCreatedH = 600;
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "第6章");
        windowHandle = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        // 一開始要先呼叫，才能以長、寬構建透視矩陣
        createProjMat(windowCreatedW, windowCreatedH);

        // 設定frameBuffer大小改變callback
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);

        int program = new ShaderProgram(Path.of("assets/shaders/program6_1&2/VertexShader.glsl")
                , Path.of("assets/shaders/program6_1&2/FragmentShader.glsl"))
                .getID();

        cameraX = 0f; cameraY = 0f; cameraZ = 4f;
        setupVertices();
        brickTexture = new Texture2D(0,  "assets/textures/imageTextures/brick0.jpg").getTexID();

        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);

        // 獲取mv矩陣和投影矩陣的統一變量
        mvLoc = glGetUniformLocation(program, "mv_matrix");
        projLoc = glGetUniformLocation(program, "proj_matrix");
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            float currentTime = (float) glfwGetTime();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_CULL_FACE);


            glUniformMatrix4fv(projLoc, false, pMat.get(vals));

            // 將視圖矩陣壓入栈
            Matrix4fStack mvStack = new Matrix4fStack(4);
            mvStack.pushMatrix();
            mvStack.translate(-cameraX, -cameraY, -cameraZ);

            mvStack.pushMatrix();
            mvStack.translate(0f, 0f, 0f); // 位置
            mvStack.pushMatrix();
            mvStack.rotateX(currentTime).scale(2f);
            glUniformMatrix4fv(mvLoc, false, mvStack.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, brickTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glGenerateMipmap(GL_TEXTURE_2D);


            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);


            glDrawElements(GL_TRIANGLES, myTorus.getNumIndices(), GL_UNSIGNED_INT, 0);
//            glDrawElements(GL_TRIANGLES, IntBuffer.wrap(myTorus.getIndicesInArray()));


            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }


    private static void setupVertices() {
        myTorus = new Torus(.5f, .2f, 48, false);

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
