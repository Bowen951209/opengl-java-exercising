package chapter4;


import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import utilities.Color;
import utilities.GLFWWindow;
import utilities.ShaderProgram;

import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.glfw.GLFW.*;

public class Program4_3 {
    private static long windowHandle;
    private static int program;

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
    private static float cubeX;
    private static float cubeY;
    private static float cubeZ;
    private static float pyrX;
    private static float pyrY;
    private static float pyrZ;


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

        program = new ShaderProgram(Path.of("src/main/java/chapter4/Shaders/for4_3and_4_4/VertexShader.glsl")
                , Path.of("src/main/java/chapter4/Shaders/for4_3and_4_4/FragmentShader.glsl"))
                .getID();

        cameraX = 0f; cameraY = 0f; cameraZ = 8f;
        cubeX = 0.0f; cubeY = -2.0f; cubeZ = 0.0f;
        pyrX = 2.0f; pyrY = 4.0f; pyrZ = 0.0f;
        setupVertices();
        glUseProgram(program);
        System.out.println("Using ProgramID: " + program);
    }

    private static void loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 獲取mv矩陣和投影矩陣的統一變量
            int mvLoc = glGetUniformLocation(program, "mv_matrix");
            int projLoc = glGetUniformLocation(program, "proj_matrix");


            // 只計算一次視圖，用於2個對象
            Matrix4f vMat = new Matrix4f().translate(-cameraX, -cameraY, -cameraZ);



            //繪製立方體，使用0號緩衝區
            Matrix4f mMat = new Matrix4f().translate(cubeX, cubeY, cubeZ);
            Matrix4f mvMat = vMat.mul(mMat);
            glUniformMatrix4fv(mvLoc, false, mvMat.get(vals));
            glUniformMatrix4fv(projLoc, false, pMat.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glFrontFace(GL_CW); // 立方體頂點的纏繞順序為順時針方向
            glDrawArrays(GL_TRIANGLES, 0, 36);


            // 繪製四角錐，使用1號緩衝區
            mMat = new Matrix4f().translate(pyrX, pyrY, pyrZ);
            mvMat = vMat.mul(mMat);
            glUniformMatrix4fv(mvLoc, false, mvMat.get(vals));
            glUniformMatrix4fv(projLoc, false, pMat.get(vals));

            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glFrontFace(GL_CCW); // 四角錐頂點的纏繞順序為順時針方向
            glDrawArrays(GL_TRIANGLES, 0, 18);



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
            -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
                    1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
                    1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
                    -1.0f, -1.0f, -1.0f,  1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,
                    1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f
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
