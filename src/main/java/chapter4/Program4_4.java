package chapter4;


import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import engine.Color;
import engine.GLFWWindow;
import engine.ShaderProgram;

import java.nio.file.Path;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class Program4_4 {
    private static long windowHandle;

    private static float cameraX;
    private static float cameraY;
    private static float cameraZ;

    private static Matrix4f pMat;
    private static final float[] vals = new float[16];// utility buffer for transferring matrices
    private static final int[] vbo = new int[2];
    private static final GLFWFramebufferSizeCallbackI resizeGlViewportAndResetAspect = (long window, int w, int h) -> {
        System.out.println("GLFW Window Resized to: " + w + "*" + h);
        glViewport(0, 0, w, h);
        createProjMat(w, h);
    };
    private static int projLoc;
    private static int mvLoc;


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
        GLFWWindow glfwWindow = new GLFWWindow(windowCreatedW, windowCreatedH, "第4章");
        windowHandle = glfwWindow.getID();
        glfwWindow.setClearColor(new Color(0f, 0f, 0f, 0f));
        // 一開始要先呼叫，才能以長、寬構建透視矩陣
        createProjMat(windowCreatedW, windowCreatedH);

        // 設定frameBuffer大小改變callback
        glfwSetFramebufferSizeCallback(windowHandle, resizeGlViewportAndResetAspect);

        int program = new ShaderProgram(Path.of("src/main/java/chapter4/Shaders/for4_3and_4_4/VertexShader.glsl")
                , Path.of("src/main/java/chapter4/Shaders/for4_3and_4_4/FragmentShader.glsl"))
                .getID();

        cameraX = 0f;
        cameraY = 0f;
        cameraZ = 8f;
        setupVertices();
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
            Matrix4fStack mvStack = new Matrix4fStack(5);
            mvStack.pushMatrix();
            mvStack.translate(-cameraX, -cameraY, -cameraZ);

            // ------------------四角錐 : 太陽-------------------------------------------
            mvStack.pushMatrix();
            mvStack.translate(0f, 0f, 0f); // 太陽位置
            mvStack.pushMatrix();
            mvStack.rotateX(currentTime); // 太陽旋轉

            glUniformMatrix4fv(mvLoc, false, mvStack.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glDrawArrays(GL_TRIANGLES, 0, 18); // 繪製太陽

            mvStack.popMatrix(); // 從栈中移除太陽自轉


            // ------------------大立方體 : 地球-------------------------------------------
            mvStack.pushMatrix();
            mvStack.translate(sin(currentTime) * 4f, 0f, cos(currentTime) * 4f); // 地球繞太陽移動
            mvStack.pushMatrix();
            mvStack.rotateY(currentTime); // 地球旋轉

            glUniformMatrix4fv(mvLoc, false, mvStack.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glDrawArrays(GL_TRIANGLES, 0, 36); // 繪製地球
            mvStack.popMatrix(); // 移除栈中的地球自旋轉


            // ------------------小立方體 : 月球-------------------------------------------
            mvStack.pushMatrix();
            mvStack.translate(0f, sin(currentTime) * 2f, cos(currentTime) * 2f); // 月球繞地球移動
            mvStack.rotateZ(currentTime); // 月球旋轉
            mvStack.scale(.5f); // 讓月球小一點

            glUniformMatrix4fv(mvLoc, false, mvStack.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glDrawArrays(GL_TRIANGLES, 0, 36); // 繪製月球

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }


    private static void setupVertices() {
        float[] cubePositions = { // 36個頂點，12個三角形; 2*2*2 正方體
                -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f,
        };

        float[] pyramidPositions = { // 四角椎
                -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f
        };

        int vao = glGenVertexArrays(); // vao: Vertex Array Object
        glBindVertexArray(vao);


        vbo[0] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, cubePositions, GL_STATIC_DRAW);

        vbo[1] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, pyramidPositions, GL_STATIC_DRAW);
    }

    private static void createProjMat(int w, int h) {
        float aspect = (float) w / (float) h;
        pMat = new Matrix4f().perspective(1.0472f, aspect, .1f, 1000f); // 1.0472 = 60度
    }

}
