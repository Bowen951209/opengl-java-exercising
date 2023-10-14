package chapter6;


import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import engine.util.Color;
import engine.GLFWWindow;
import engine.ShaderProgram;
import engine.sceneComponents.models.Sphere;
import engine.readers.TextureReader;

import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program6_1 {
    private static long windowHandle;

    private static float cameraX;
    private static float cameraY;
    private static float cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static final int[] vbo = new int[3];
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int projLoc;
    private static int mvLoc;
    private static int brickTexture;
    private static Sphere mySphere;


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
        brickTexture = new TextureReader("assets/textures/imageTextures/brick0.jpg").getTexID();
        
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
            mvStack.rotateX(currentTime); // 旋轉

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
            glDrawArrays(GL_TRIANGLES, 0, mySphere.getNumIndices()); // 繪製


            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }


    private static void setupVertices() {

        mySphere = new Sphere(100);
        int[ ] indices = mySphere.getIndices();
        Vector3f[ ] vert = mySphere.getVertices();
        Vector2f[ ] tex = mySphere.getTexCoords();
        Vector3f[ ] norm = mySphere.getNormals();
        float[ ] pvalues = new float[indices.length*3]; // vertex positions
        float[ ] tvalues = new float[indices.length*2]; // texture coordinates
        float[ ] nvalues = new float[indices.length*3]; // normal vectors
        for (int i=0; i<indices.length; i++)
        {	 pvalues[i*3] = (vert[indices[i]]).x;
            pvalues[i*3+1] = (vert[indices[i]]).y;
            pvalues[i*3+2] = (vert[indices[i]]).z;
            tvalues[i*2] = (tex[indices[i]]).x;
            tvalues[i*2+1] = (tex[indices[i]]).y;
            nvalues[i*3] = (norm[indices[i]]).x;
            nvalues[i*3+1]=(norm[indices[i]]).y;
            nvalues[i*3+2]=(norm[indices[i]]).z;
        }
        int[] vao = new int[1];
        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);
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


    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }


}
